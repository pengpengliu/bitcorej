package org.bitcorej.chain.bhd;

import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class BHDStateProvider extends BitcoinStateProvider {

    private static final EnumSet<Script.VerifyFlag> MINIMUM_VERIFY_FLAGS = EnumSet.of(Script.VerifyFlag.P2SH,
            Script.VerifyFlag.NULLDUMMY);

    public BHDStateProvider(Network network) {
        super(network);
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        return new KeyPair(ecKey.getPrivateKeyAsHex(), this.calcSegWitAddress(ecKey.toAddress(this.params).toString()));
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject rawTxJSON = new JSONObject(rawTx);
        Transaction tx = buildTransaction(rawTx);
        tx.setVersion(2);
        try {
            byte[] hashPrevouts;
            byte[] hashOutputs;
            byte[] hashSequence;

            UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();

            for (TransactionInput input : tx.getInputs()) {
                input.getOutpoint().bitcoinSerialize(stream);
            }
            hashPrevouts = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash outputs
            stream = new UnsafeByteArrayOutputStream();

            for (TransactionOutput output : tx.getOutputs()) {
                output.bitcoinSerialize(stream);
            }
            hashOutputs = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash sequence
            stream = new UnsafeByteArrayOutputStream();

            for (TransactionInput input : tx.getInputs()) {
                Utils.uint32ToByteStreamLE(input.getSequenceNumber(), stream);
            }
            hashSequence = Sha256Hash.hashTwice(stream.toByteArray());

            // calc witnesses and redemScripts
            List<byte[]> witnesses = new ArrayList<>();
            List<String> redeemScripts = new ArrayList<>();
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);

                Script scriptPubKey = new Script(input.getScriptBytes());
                ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));

                String redeemScript = String.format("0014%s", NumericUtil.bytesToHex(ecKey.getPubKeyHash()));
                redeemScripts.add(redeemScript);

                // Hash for witnessV0
                stream = new UnsafeByteArrayOutputStream();
                input.getOutpoint().bitcoinSerialize(stream);
                byte[] outpoint = stream.toByteArray();

                // calc scriptCode
                byte[] scriptCode = NumericUtil.hexToBytes(String.format("0x1976a914%s88ac", NumericUtil.bytesToHex(ecKey.getPubKeyHash())));

                // before sign
                stream = new UnsafeByteArrayOutputStream();
                // Version
                Utils.uint32ToByteStreamLE(2L, stream);
                // Input prevouts/nSequence (none/all, depending on flags)
                stream.write(hashPrevouts);
                stream.write(hashSequence);
                // The input being signed (replacing the scriptSig with scriptCode + amount)
                // The prevout may already be contained in hashPrevout, and the nSequence
                // may already be contain in hashSequence.
                stream.write(outpoint);
                stream.write(scriptCode);
                long amount = Objects.requireNonNull(input.getValue()).getValue();
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(amount), stream);
                Utils.uint32ToByteStreamLE(input.getSequenceNumber(), stream);
                // Outputs (none/one/all, depending on flags)
                stream.write(hashOutputs);
                // Locktime
                Utils.uint32ToByteStreamLE(tx.getLockTime(), stream);
                // Sighash type 1 = all
                Utils.uint32ToByteStreamLE(1L, stream);

                // BHD
                stream.write(new byte[]{5});
                stream.write("btchd".getBytes());

                byte[] hashPreimage = stream.toByteArray();
                byte[] sigHash = Sha256Hash.hashTwice(hashPreimage);
                ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.wrap(sigHash));
                byte hashType = 0x01;
                // witnesses
                byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType});

                witnesses.add(sig);
            }

            // the second stream is used to calc the traditional txhash

            UnsafeByteArrayOutputStream[] serialStreams = new UnsafeByteArrayOutputStream[]{
                    new UnsafeByteArrayOutputStream(), new UnsafeByteArrayOutputStream()
            };
            for (int idx = 0; idx < 2; idx++) {
                stream = serialStreams[idx];
                Utils.uint32ToByteStreamLE(2L, stream); // version
                if (idx == 0) {
                    stream.write(0x00); // maker
                    stream.write(0x01); // flag
                }
                // inputs
                stream.write(new VarInt(tx.getInputs().size()).encode());
                for (int i = 0; i < tx.getInputs().size(); i++) {
                    TransactionInput input = tx.getInput(i);
                    stream.write(NumericUtil.reverseBytes(input.getOutpoint().getHash().getBytes()));
                    Utils.uint32ToByteStreamLE(input.getOutpoint().getIndex(), stream);

                    // the length of byte array that follows, and this length is used by OP_PUSHDATA1
                    stream.write(0x17);
                    // the length of byte array that follows, and this length is used by cutting array
                    stream.write(0x16);
                    stream.write(NumericUtil.hexToBytes(redeemScripts.get(i)));
                    Utils.uint32ToByteStreamLE(input.getSequenceNumber(), stream);
                }
                // outputs
                stream.write(new VarInt(tx.getOutputs().size()).encode());
                for (int i = 0; i < tx.getOutputs().size(); i++) {
                    TransactionOutput output = tx.getOutput(i);
                    Utils.uint64ToByteStreamLE(BigInteger.valueOf(output.getValue().value), stream);
                    stream.write(new VarInt(output.getScriptBytes().length).encode());
                    stream.write(output.getScriptBytes());
                }

                // the first stream is used to calc the segwit hash
                if (idx == 0) {
                    for (int i = 0; i < witnesses.size(); i++) {
                        TransactionInput input = tx.getInput(i);
                        Script scriptPubKey = new Script(input.getScriptBytes());
                        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));
                        byte[] wit = witnesses.get(i);
                        stream.write(new VarInt(2).encode());
                        stream.write(new VarInt(wit.length).encode());
                        stream.write(wit);
                        stream.write(new VarInt(ecKey.getPubKey().length).encode());
                        stream.write(ecKey.getPubKey());
                    }
                }
                Utils.uint32ToByteStreamLE(tx.getLockTime(), stream);
            }

            // cal txid
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);
                input.setScriptSig(new ScriptBuilder().data(NumericUtil.hexToBytes(redeemScripts.get(i))).build());
            }
            String txid = tx.getHashAsString();

            byte[] signed = serialStreams[0].toByteArray();
            String signedHex = NumericUtil.bytesToHex(signed);
            JSONObject packedTx = new JSONObject();
            packedTx.put("txid", txid);
            packedTx.put("raw", signedHex);

            if (rawTxJSON.has("destinations")) {
                packedTx.put("destinations", rawTxJSON.getJSONArray("destinations"));
            }
            return packedTx.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}