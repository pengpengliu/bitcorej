package org.bitcorej.chain.grs;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.core.Utils.uint32ToByteStreamLE;

public class GRSStateProvider extends BitcoinStateProvider {
    public GRSStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(GRSNetParameters.get());
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = GRSNetParameters.get();
                break;
        }
        super.network = network;
    }

    protected String selectPrivateKeys(Script script, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            String legacyAddress = this.generateKeyPair(keys.get(i)).getPublic();
            if (NumericUtil.bytesToHex(script.getProgram()).equals(generateP2PKHScript(legacyAddress))) {
                return keys.get(i);
            }
        }
        return null;
    }

    public String generateP2PKHScript(String address) {
        byte[] versionAndDataBytes = Base58.decodeChecked(address);
        byte[] bytes = Arrays.copyOfRange(versionAndDataBytes, 1, versionAndDataBytes.length);
        return "76a914" + NumericUtil.bytesToHex(bytes) + "88ac";
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        // A stringified buffer is:
        byte[] payload = ecKey.getPubKeyHash();
        byte[] addressBytes = new byte[]{(byte) this.params.getAddressHeader()};
        addressBytes = ByteUtil.concat(addressBytes, payload);
        byte[] checksum = Groestl.digest(addressBytes, 0, payload.length + 1);
        addressBytes = ByteUtil.concat(addressBytes, new byte[]{checksum[0], checksum[1], checksum[2], checksum[3]});
        String address = Base58.encode(addressBytes);
        return new KeyPair(ecKey.getPrivateKeyAsHex(), address);
    }

    protected org.bitcoinj.core.Transaction buildTransaction(String json) {
        org.bitcoinj.core.Transaction tx = super.buildTransaction(json);
        tx.setVersion(2);
        return tx;
    }

    public Sha256Hash hashForSignature(org.bitcoinj.core.Transaction rawTx, int inputIndex, Script redeemScript,
                                       org.bitcoinj.core.Transaction.SigHash type, boolean anyoneCanPay) {
        int sigHash = TransactionSignature.calcSigHashValue(type, anyoneCanPay);
        byte sigHashType = (byte)sigHash;
        byte[] connectedScript = redeemScript.getProgram();
        try {
            org.bitcoinj.core.Transaction tx = this.params.getDefaultSerializer().makeTransaction(rawTx.bitcoinSerialize());

            for (int i = 0; i < tx.getInputs().size(); i++) {
                tx.getInputs().get(i).clearScriptBytes();
            }

            connectedScript = Script.removeAllInstancesOfOp(connectedScript, ScriptOpCodes.OP_CODESEPARATOR);

            // Set the input to the script of its output. Bitcoin Core does this but the step has no obvious purpose as
            // the signature covers the hash of the prevout transaction which obviously includes the output script
            // already. Perhaps it felt safer to him in some way, or is another leftover from how the code was written.
            TransactionInput input = tx.getInputs().get(inputIndex);
            // input.setScriptBytes(connectedScript);
            input.setScriptSig(new Script(connectedScript));

            if ((sigHashType & 0x1f) == org.bitcoinj.core.Transaction.SigHash.NONE.value) {
                // SIGHASH_NONE means no outputs are signed at all - the signature is effectively for a "blank cheque".
                // tx.outputs = new ArrayList<TransactionOutput>(0);
                tx.clearOutputs();
                // The signature isn't broken by new versions of the transaction issued by other parties.
                for (int i = 0; i < tx.getInputs().size(); i++)
                    if (i != inputIndex)
                        tx.getInput(i).setSequenceNumber(0);
            } else if ((sigHashType & 0x1f) == org.bitcoinj.core.Transaction.SigHash.SINGLE.value) {
                // SIGHASH_SINGLE means only sign the output at the same index as the input (ie, my output).
                if (inputIndex >= tx.getOutputs().size()) {
                    // The input index is beyond the number of outputs, it's a buggy signature made by a broken
                    // Bitcoin implementation. Bitcoin Core also contains a bug in handling this case:
                    // any transaction output that is signed in this case will result in both the signed output
                    // and any future outputs to this public key being steal-able by anyone who has
                    // the resulting signature and the public key (both of which are part of the signed tx input).

                    // Bitcoin Core's bug is that SignatureHash was supposed to return a hash and on this codepath it
                    // actually returns the constant "1" to indicate an error, which is never checked for. Oops.
                    return Sha256Hash.wrap("0100000000000000000000000000000000000000000000000000000000000000");
                }
                // In SIGHASH_SINGLE the outputs after the matching input index are deleted, and the outputs before
                // that position are "nulled out". Unintuitively, the value in a "null" transaction is set to -1.
                // tx.outputs = new ArrayList<TransactionOutput>(tx.getOutputs().subList(0, inputIndex + 1));
                tx.clearOutputs();
                for (int i = 0; i < inputIndex; i++)
                    tx.addOutput(new TransactionOutput(tx.getParams(), tx, Coin.NEGATIVE_SATOSHI, new byte[] {}));
                // tx.outputs.set(i, new TransactionOutput(tx.params, tx, Coin.NEGATIVE_SATOSHI, new byte[] {}));
                // The signature isn't broken by new versions of the transaction issued by other parties.
                for (int i = 0; i < tx.getInputs().size(); i++)
                    if (i != inputIndex)
                        tx.getInput(i).setSequenceNumber(0);
            }

            if ((sigHashType & org.bitcoinj.core.Transaction.SigHash.ANYONECANPAY.value) == org.bitcoinj.core.Transaction.SigHash.ANYONECANPAY.value) {
                // SIGHASH_ANYONECANPAY means the signature in the input is not broken by changes/additions/removals
                // of other inputs. For example, this is useful for building assurance contracts.
                // tx.inputs = new ArrayList<TransactionInput>();
                // tx.inputs.add(input);
                tx.clearOutputs();
                tx.addInput(input);
            }

            ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream();
            bitcoinSerializeToStream(tx, bos);
            // We also have to write a hash type (sigHashType is actually an unsigned char)
            uint32ToByteStreamLE(0x000000ff & sigHashType, bos);
            // Note that this is NOT reversed to ensure it will be signed correctly. If it were to be printed out
            // however then we would expect that it is IS reversed.
            Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash(bos.toByteArray()));
            bos.close();

            return hash;
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    void bitcoinSerializeToStream(org.bitcoinj.core.Transaction rawTx, OutputStream stream) throws IOException {
        uint32ToByteStreamLE(rawTx.getVersion(), stream);
        stream.write(new VarInt(rawTx.getInputs().size()).encode());
        for (TransactionInput in : rawTx.getInputs())
            in.bitcoinSerialize(stream);
        stream.write(new VarInt(rawTx.getOutputs().size()).encode());
        for (TransactionOutput out : rawTx.getOutputs())
            out.bitcoinSerialize(stream);
        uint32ToByteStreamLE(rawTx.getLockTime(), stream);
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject rawTxJSON = new JSONObject(rawTx);
        Transaction tx = buildTransaction(rawTx);
        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);
            Script scriptPubKey = new Script(input.getScriptBytes());

            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));

            Sha256Hash hash = hashForSignature(tx, i, new Script(input.getScriptBytes()), Transaction.SigHash.ALL, false);
            ECKey.ECDSASignature ecSig = ecKey.sign(hash);
            TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);

            if (scriptPubKey.isSentToRawPubKey()) {
                input.setScriptSig(ScriptBuilder.createInputScript(txSig));
            } else {
                if (!scriptPubKey.isSentToAddress()) {
                    return null;
                }
                input.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
            }
        }

        try {
            JSONObject packedTx = new JSONObject();
            ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream();
            bitcoinSerializeToStream(tx, bos);
            packedTx.put("txid", Sha256Hash.wrapReversed(Sha256Hash.hash(bos.toByteArray())).toString());
            packedTx.put("raw", NumericUtil.bytesToHex(bos.toByteArray()));

            if (rawTxJSON.has("destinations")) {
                packedTx.put("destinations", rawTxJSON.getJSONArray("destinations"));
            }
            return packedTx.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }
}
