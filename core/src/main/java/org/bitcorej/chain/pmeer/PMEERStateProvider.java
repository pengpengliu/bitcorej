package org.bitcorej.chain.pmeer;

import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.vsys.HashUtil;
import org.bitcorej.core.Network;
import org.bitcorej.utils.BitUtils;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;

public class PMEERStateProvider extends BitcoinStateProvider {
    public PMEERStateProvider(Network network) {
        super(network);
        super.params = PMEERNetParameters.get();
        super.network = network;
    }

    public String calcTAddress(byte[] pubKeyHash) {
        byte[] version = ByteBuffer.allocate(4).putInt(this.params.getP2SHHeader()).array();
        byte[] hash160 = ByteUtil.trimLeadingZeroes(BitUtils.concatenate(version, pubKeyHash));
        byte[] hash = HashUtil.doubleHashB(hash160);
        byte[] checksum = Arrays.copyOfRange(hash, 0, 4);
        return Base58.encode(BitUtils.concatenate(hash160, checksum));
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        byte[] pubKeyHash = blake2b256hash160(ecKey.getPubKey());
        return new KeyPair(ecKey.getPrivateKeyAsHex(), this.calcTAddress(pubKeyHash));
    }

    public String generateP2PKHScript(String address) {
        byte[] decoded  = Base58.decode(address);
        byte[] versionAndDataBytes = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
        byte[] bytes = new byte[versionAndDataBytes.length - 2];
        System.arraycopy(versionAndDataBytes, 2, bytes, 0, versionAndDataBytes.length - 2);
        Script script = new ScriptBuilder()
                .op(OP_DUP)
                .op(OP_HASH160)
                .data(bytes)
                .op(OP_EQUALVERIFY)
                .op(OP_CHECKSIG)
                .build();
        return NumericUtil.bytesToHex(script.getProgram());
    }

    public static byte[] blake2b256hash160(byte[] input) {
        byte[] sha256 = HashUtil.hashB(input, 0, input.length);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
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

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject rawTxJSON = new JSONObject(rawTx);
        Transaction tx = buildTransaction(rawTx);
        for (TransactionInput input: tx.getInputs()) {
            Script scriptPubKey = new Script(input.getScriptBytes());
            if (!scriptPubKey.isSentToAddress()) {
                return null;
            }
        }
        JSONObject packedTx = new JSONObject();
        packedTx.put("txid", calTxId(tx));
        packedTx.put("raw", bitcoinSerialize(tx, keys));

        if (rawTxJSON.has("destinations")) {
            packedTx.put("destinations", rawTxJSON.getJSONArray("destinations"));
        }
        return packedTx.toString();
    }

    private byte[] calcSignatureHash(Transaction tx, int index) {
        UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
        try {
            // version
            Utils.uint32ToByteStreamLE(1L | 1L << 16, stream);
            // inputs
            stream.write(new VarInt(tx.getInputs().size()).encode());
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);
                stream.write(input.getOutpoint().getHash().getBytes());
                Utils.uint32ToByteStreamLE(input.getOutpoint().getIndex(), stream);
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
            stream.write(new byte[1 * tx.getInputs().size()]);
            stream.write(new byte[2 * tx.getOutputs().size()]);
            stream.write(new byte[4 + 4]);  // 4 bytes lock time + 4 bytes expiry
            byte[] prefixHash = HashUtil.hashB(stream.toByteArray());

            // calc witnesses and redemScripts
            stream = new UnsafeByteArrayOutputStream();
            // version
            Utils.uint32ToByteStreamLE(1L | 3L << 16, stream);
            // inputs
            stream.write(new VarInt(tx.getInputs().size()).encode());
            for (int i = 0; i < tx.getInputs().size(); i++) {
                if (i == index) {
                    TransactionInput input = tx.getInput(i);
                    stream.write(input.getScriptBytes().length);
                    stream.write(input.getScriptBytes());
                } else {
                    stream.write(new byte[1]);
                }
            }
            byte[] witnessHash = HashUtil.hashB(stream.toByteArray());
            // calc sign hash
            stream = new UnsafeByteArrayOutputStream();
            // Sighash type 1 = all
            Utils.uint32ToByteStreamLE(1L, stream);
            stream.write(prefixHash);
            stream.write(witnessHash);
            return HashUtil.hashB(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String calTxId(Transaction tx) {
        UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
        try {
            // version
            Utils.uint32ToByteStreamLE(1L | 1L << 16, stream);
            // inputs
            stream.write(new VarInt(tx.getInputs().size()).encode());
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);
                stream.write(NumericUtil.reverseBytes(input.getOutpoint().getHash().getBytes()));
                Utils.uint32ToByteStreamLE(input.getOutpoint().getIndex(), stream);
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
            stream.write(new byte[4 + 4]);
            byte[] serialized = stream.toByteArray();
            return NumericUtil.bytesToHex(Utils.reverseBytes(HashUtil.doubleHashB(serialized)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String bitcoinSerialize(Transaction tx, List<String> keys) {
        UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
        try {
            // version
            Utils.uint32ToByteStreamLE(1, stream);
            // inputs
            stream.write(new VarInt(tx.getInputs().size()).encode());
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);
                stream.write(input.getOutpoint().getHash().getBytes());
                Utils.uint32ToByteStreamLE(input.getOutpoint().getIndex(), stream);
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
            stream.write(new byte[4 + 4]);
            byte[] time = new byte[4];
            Utils.uint32ToByteArrayLE(new Date().getTime() / 1000, time, 0);
            stream.write(time);
            // signs
            stream.write(new VarInt(tx.getInputs().size()).encode());
            for (int i = 0; i < tx.getInputs().size(); i++) {
                TransactionInput input = tx.getInput(i);

                Script scriptPubKey = new Script(input.getScriptBytes());

                byte[] hashForSignature = calcSignatureHash(tx, i);
                ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));
                ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.wrap(hashForSignature));
                byte hashType = 0x01;
                byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType});
                sig = ByteUtil.concat(new byte[]{ (byte)sig.length }, sig);
                sig = ByteUtil.concat(sig, new byte[]{ (byte)ecKey.getPubKey().length });
                sig = ByteUtil.concat(sig, ecKey.getPubKey());
                stream.write(new VarInt(sig.length).encode());
                stream.write(sig);
            }
            return NumericUtil.bytesToHex(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
