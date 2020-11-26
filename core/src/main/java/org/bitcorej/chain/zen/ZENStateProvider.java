package org.bitcorej.chain.zen;

import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.zcash.ZcashStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.BitUtils;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;

public class ZENStateProvider extends BitcoinStateProvider {

    public ZENStateProvider(Network network) {
        super(network);
        super.params = ZENNetParams.get();
        super.network = network;
    }

    public String calcTAddress(byte[] pubKeyHash) {
        byte[] version = ByteBuffer.allocate(4).putInt(this.params.getAddressHeader()).array();
        byte[] hash160 = ByteUtil.trimLeadingZeroes(BitUtils.concatenate(version, pubKeyHash));
        byte[] hash = Sha256Hash.hashTwice(hash160);
        byte[] checksum = Arrays.copyOfRange(hash, 0, 4);
        return Base58.encode(BitUtils.concatenate(hash160, checksum));
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        return new KeyPair(ecKey.getPrivateKeyAsHex(), this.calcTAddress(pubKeyHash));
    }

    public String generateP2PKHScript(String address) {
        byte[] versionAndDataBytes = Base58.decodeChecked(address);
        byte[] bytes = new byte[versionAndDataBytes.length - 2];
        System.arraycopy(versionAndDataBytes, 2, bytes, 0, versionAndDataBytes.length - 2);
        byte[] blockHash = NumericUtil.hexToBytes("00000001cf4e27ce1dd8028408ed0a48edd445ba388170c9468ba0d42fff3052");
        long blockHeight = 142091;
        Script script = new ScriptBuilder()
                .op(OP_DUP)
                .op(OP_HASH160)
                .data(bytes)
                .op(OP_EQUALVERIFY)
                .op(OP_CHECKSIG)
                .data(NumericUtil.reverseBytes(blockHash))
                .number(blockHeight)
                .op(0xb4)  // OP_CHECKBLOCKATHEIGHT
                .build();
        return NumericUtil.bytesToHex(script.getProgram());
    }

    @Override
    protected String selectPrivateKeys(Script script, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            if (NumericUtil.bytesToHex(script.getProgram()).equals(generateP2PKHScript(this.generateKeyPair(keys.get(i)).getPublic()))) {
                return keys.get(i);
            }
        }
        return null;
    }

    public byte[] serializeTx(Transaction tx, int index) {
        try {
            UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
            // Version
            Utils.uint32ToByteStreamLE(1L, stream);
            stream.write(tx.getInputs().size());
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);
                input.getOutpoint().bitcoinSerialize(stream);
                if (index >= 0 && index != i) {
                    stream.write(0);
                } else {
                    byte[] script = input.getScriptBytes();
                    stream.write(script.length);
                    stream.write(script);
                }
                Utils.uint32ToByteStreamLE(input.getSequenceNumber(), stream);
            }
            stream.write(tx.getOutputs().size());
            for (int i = 0; i < tx.getOutputs().size(); i++) {
                TransactionOutput output = tx.getOutput(i);
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(output.getValue().value), stream);
                byte[] script = output.getScriptBytes();
                stream.write(script.length);
                stream.write(script);
            }
            // Locktime
            Utils.uint32ToByteStreamLE(tx.getLockTime(), stream);
            return stream.toByteArray();
            // System.out.println(NumericUtil.bytesToHex(stream.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject rawTxJSON = new JSONObject(rawTx);
        Transaction tx = buildTransaction(rawTx);

        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);
            Script scriptPubKey = new Script(input.getScriptBytes());

            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));

            byte[] rawTxBytes = serializeTx(tx, i);
            byte[] toSign = ByteUtil.concat(rawTxBytes, new byte[]{ 0x01, 0x00, 0x00, 0x00 });
            ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.twiceOf(toSign));
            byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{ 0x01 });

            byte[] newScript = ByteUtil.concat(new VarInt(sig.length).encode(), sig);
            byte[] newPubkey = ByteUtil.concat(new VarInt(ecKey.getPubKey().length).encode(), ecKey.getPubKey());
            input.setScriptSig(new Script(ByteUtil.concat(newScript, newPubkey)));
        }
        String txid = tx.getHashAsString();
        JSONObject packedTx = new JSONObject();
        packedTx.put("txid", txid);
        packedTx.put("raw", NumericUtil.bytesToHex(serializeTx(tx, -1)));

        if (rawTxJSON.has("destinations")) {
            packedTx.put("destinations", rawTxJSON.getJSONArray("destinations"));
        }
        return packedTx.toString();
    }
}
