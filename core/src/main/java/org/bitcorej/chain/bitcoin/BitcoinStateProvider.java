package org.bitcorej.chain.bitcoin;

import com.google.common.math.LongMath;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.Networks;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.UTXOState;
import org.bitcorej.core.Network;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BitcoinStateProvider implements ChainState, UTXOState {
    protected static final BigDecimal DECIMALS = new BigDecimal(10).pow(8);
    protected final static BigDecimal DUST_THRESHOLD = new BigDecimal(2730);

    protected Network network;
    protected NetworkParameters params;

    public BitcoinStateProvider(Network network) {
        switch (network) {
            case MAIN:
                params = MainNetParams.get();
                break;
            case TEST:
                params = TestNet3Params.get();
                break;
            case REGTEST:
                params = RegTestParams.get();
                break;
        }

        this.network = network;
    }

    public void setParams(NetworkParameters params) {
        this.params = params;
        Networks.register(this.params);
    }

    public String calcSegWitAddress(String legacyAddress) {
        byte[] pubKeyHash = Address.fromBase58(this.params, legacyAddress).getHash160();
        String redeemScript = String.format("0x0014%s", NumericUtil.bytesToHex(pubKeyHash));
        return Address.fromP2SHHash(this.params, Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript))).toBase58();
    }

    public String calcRedeemScript(String segWitAddress) {
        byte[] pubKeyHash = Address.fromBase58(this.params, segWitAddress).getHash160();
        String redeemScript = String.format("0014%s", NumericUtil.bytesToHex(pubKeyHash));
        return redeemScript;
    }

    public String calcWitnessScript(String segWitAddress) {
        byte[] pubKeyHash = Address.fromBase58(this.params, segWitAddress).getHash160();
        byte[] scriptCode = NumericUtil.hexToBytes(String.format("0x1976a914%s88ac", NumericUtil.bytesToHex(pubKeyHash)));
        return NumericUtil.bytesToHex(scriptCode);
    }

    public String generateP2PKHScript(String address) {
        return NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(this.params, address)).getProgram());
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));

        return new KeyPair(ecKey.getPrivateKeyAsHex(), ecKey.toAddress(this.params).toString());
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    @Override
    public Boolean validateTx(String rawTx, String tx) {
        return null;
    }

    @Override
    public org.bitcorej.chain.Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee) {
        return encodeTransaction(utxos, recipients, changeAddress, fee, DECIMALS);
    }

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
            String scriptPubKey = generateP2PKHScript(utxo.getAddress());
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

            String script = generateP2PKHScript(recipient.getAddress());
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

    protected Transaction buildTransaction(String json) {
        JSONObject jsonObject = new JSONObject(json);
        Transaction tx = new Transaction(this.params);

        JSONArray inputs = jsonObject.getJSONArray("inputs");
        for (int i = 0; i < inputs.length(); i++) {
            JSONObject input = inputs.getJSONObject(i);
            String amountStr = input.getJSONObject("output").getString("amount");
            long amount = new BigDecimal(amountStr).multiply(DECIMALS).longValue();
            Coin coin = Coin.valueOf(amount);
            TransactionInput txInput = new TransactionInput(this.params, tx, new Script(NumericUtil.hexToBytes(input.getJSONObject("output").getString("script"))).getProgram(), new TransactionOutPoint(params, input.getLong("vout"), Sha256Hash.wrap(input.getString("txid"))), coin);
            tx.addInput(txInput);
        }
        JSONArray outputs = jsonObject.getJSONArray("outputs");
        for (int i = 0; i < outputs.length(); i++) {
            JSONObject output = outputs.getJSONObject(i);
            Coin coin = Coin.valueOf(new BigDecimal(output.getString("amount")).multiply(BigDecimal.valueOf(LongMath.pow(10, 8))).longValue());
            tx.addOutput(new TransactionOutput(this.params, tx, coin, NumericUtil.hexToBytes(output.getString("script"))));
        }
        return tx;
    }

    public String toWIF(String privateKeyHex) {
        return ECKey.fromPrivate(NumericUtil.hexToBytes(privateKeyHex)).getPrivateKeyAsWiF(this.params);
    }

    protected String selectPrivateKeys(Script script, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            String address = script.getToAddress(this.params).toString();
            String legacyAddress = this.generateKeyPair(keys.get(i)).getPublic();
            String segWitAddress = this.calcSegWitAddress(legacyAddress);
            if (address.equals(legacyAddress) || address.equals(segWitAddress)) {
                return keys.get(i);
            }
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

            Sha256Hash hash = tx.hashForSignature(i, new Script(input.getScriptBytes()), Transaction.SigHash.ALL, false);
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

        JSONObject packedTx = new JSONObject();
        packedTx.put("txid", tx.getHashAsString());
        packedTx.put("raw", NumericUtil.bytesToHex(tx.bitcoinSerialize()));

        if (rawTxJSON.has("destinations")) {
            packedTx.put("destinations", rawTxJSON.getJSONArray("destinations"));
        }

        return packedTx.toString();
    }

    @Override
    public String signSegWitTransaction(String rawTx, List<String> keys) {
        JSONObject rawTxJSON = new JSONObject(rawTx);
        Transaction tx = buildTransaction(rawTx);
        for (TransactionInput input: tx.getInputs()) {
            Script scriptPubKey = new Script(input.getScriptBytes());
            if (!scriptPubKey.isPayToScriptHash()) {
                return null;
            }
        }
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
                Utils.uint32ToByteStreamLE(1L, stream);
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
                Utils.uint32ToByteStreamLE(1L, stream); // version
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
