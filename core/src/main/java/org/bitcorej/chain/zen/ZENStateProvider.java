package org.bitcorej.chain.zen;

import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.chain.zcash.ZcashStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.BitUtils;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
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

    public String generateP2PKHScript(Recipient recipient) {
        byte[] versionAndDataBytes = Base58.decodeChecked(recipient.getAddress());
        byte[] bytes = new byte[versionAndDataBytes.length - 2];
        System.arraycopy(versionAndDataBytes, 2, bytes, 0, versionAndDataBytes.length - 2);
        byte[] blockHash = recipient.getBip115BlockHash(); // NumericUtil.hexToBytes("00000001cf4e27ce1dd8028408ed0a48edd445ba388170c9468ba0d42fff3052");
        long blockHeight = recipient.getBip115BlockHeight(); // 142091;
        Script script = new ScriptBuilder()
                .op(OP_DUP)
                .op(OP_HASH160)
                .data(bytes)
                .op(OP_EQUALVERIFY)
                .op(OP_CHECKSIG)
                .data(blockHash)
                .number(blockHeight)
                .op(0xb4)  // OP_CHECKBLOCKATHEIGHT
                .build();
        return NumericUtil.bytesToHex(script.getProgram());
    }

    public String generateP2PKHScript(UnspentOutput utxo) {
        byte[] versionAndDataBytes = Base58.decodeChecked(utxo.getAddress());
        byte[] bytes = new byte[versionAndDataBytes.length - 2];
        System.arraycopy(versionAndDataBytes, 2, bytes, 0, versionAndDataBytes.length - 2);
        byte[] blockHash = utxo.getBip115BlockHash(); // NumericUtil.hexToBytes("00000001cf4e27ce1dd8028408ed0a48edd445ba388170c9468ba0d42fff3052");
        long blockHeight = utxo.getBip115BlockHeight(); // 142091;
        Script script = new ScriptBuilder()
                .op(OP_DUP)
                .op(OP_HASH160)
                .data(bytes)
                .op(OP_EQUALVERIFY)
                .op(OP_CHECKSIG)
                .data(blockHash)
                .number(blockHeight)
                .op(0xb4)  // OP_CHECKBLOCKATHEIGHT
                .build();
        return NumericUtil.bytesToHex(script.getProgram());
    }

    @Override
    protected String selectPrivateKeys(Script script, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            String lhs = NumericUtil.bytesToHex(script.getProgram()).substring(0, 46);
            String rhs = generateP2PKHScript(this.generateKeyPair(keys.get(i)).getPublic()).substring(0, 46);
            if (lhs.equals(rhs)) {
                return keys.get(i);
            }
        }
        return null;
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, BigDecimal decimals) {
        JSONObject encodedTx = new JSONObject();

        BigDecimal totalInputAmount = new BigDecimal(0);
        JSONArray encodedInputs = new JSONArray();
        for (int i = 0; i < utxos.size(); i++) {
            UnspentOutput utxo = utxos.get(i);
            JSONObject encodedInput = new JSONObject();
            encodedInput.put("txid", utxo.getTxId());
            encodedInput.put("vout", utxo.getVout());
            JSONObject output = new JSONObject();
            String scriptPubKey = generateP2PKHScript(utxo);
            output.put("script", scriptPubKey);
            BigDecimal amount = utxo.getAmount();
            output.put("amount", amount.toString());
            totalInputAmount = totalInputAmount.add(amount);
            encodedInput.put("output", output);
            encodedInputs.put(encodedInput);
        }

        BigDecimal totalOutputAmount = new BigDecimal(0);
        JSONArray encodedOutputs = new JSONArray();
        JSONArray destinations = new JSONArray();
        for (int i = 0; i < recipients.size(); i++) {
            Recipient recipient = recipients.get(i);
            JSONObject encodedOutput = new JSONObject();
            BigDecimal amount = recipient.getAmount();
            encodedOutput.put("amount", amount.toString());
            totalOutputAmount = totalOutputAmount.add(amount);

            String script = recipient.getBip115BlockHash() != null ? generateP2PKHScript(recipient) : generateP2PKHScript(recipient.getAddress());
            encodedOutput.put("script", script);
            encodedOutputs.put(encodedOutput);
            destinations.put(recipient.toString());
        }

        if (totalInputAmount.compareTo(totalOutputAmount) < 1) {
            throw new RuntimeException("INSUFFICIENT FUNDS");
        }

        BigDecimal changeAmount = totalInputAmount.subtract(totalOutputAmount.add(fee));
        if (changeAmount.compareTo(DUST_THRESHOLD.divide(decimals)) > -1) {
            JSONObject encodedOutput = new JSONObject();
            encodedOutput.put("amount", changeAmount.toString());
            String script = generateP2PKHScript(changeAddress);
            // String script = NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(Address.getParametersFromAddress(changeAddress), changeAddress)).getProgram());
            encodedOutput.put("script", script);
            encodedOutputs.put(encodedOutput);
            destinations.put(changeAddress + " " + changeAmount);
        }
        encodedTx.put("version", 1);
        encodedTx.put("inputs", encodedInputs);
        encodedTx.put("outputs", encodedOutputs);

        encodedTx.put("destinations", destinations);

        encodedTx.put("nLockTime", 0);
        return encodedTx.toString();
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
