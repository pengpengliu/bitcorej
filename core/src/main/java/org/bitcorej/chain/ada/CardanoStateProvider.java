package org.bitcorej.chain.ada;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.UTXOState;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.utils.NumericUtil;

import com.raugfer.crypto.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CardanoStateProvider implements ChainState, UTXOState {
    protected static final BigDecimal DECIMALS = new BigDecimal(10).pow(6);
    protected final static BigDecimal DUST_THRESHOLD = new BigDecimal(10000);

    @Override
    public KeyPair generateKeyPair(String secret) {
        String publickey = wallet.publickey_from_privatekey(secret, "cardano", false);
        String address = wallet.address_from_publickey(publickey, "cardano", false);
        return new KeyPair(secret, address);
    }

    @Override
    public KeyPair generateKeyPair() {
        byte[] privateKey = Curve25519.getInstance(Curve25519.BEST).generateKeyPair().getPrivateKey();
        return generateKeyPair(NumericUtil.bytesToHex(privateKey));
    }

    @Override
    public Boolean validateTx(String rawTx, String requestTx) {
        return null;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    protected String selectPrivateKeys(String address, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (address.equals(generateKeyPair(key).getPublic())) {
                return key;
            }
        }
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject rawTxJSON = new JSONObject(rawTx);
        String bytes = rawTxJSON.getString("bytes");
        JSONArray addresses = rawTxJSON.getJSONArray("addresses");

        dict fields = transaction.transaction_decode(NumericUtil.hexToBytes(bytes), "cardano", false);
        byte[] txn = transaction.transaction_encode(fields, "cardano", false);
        dict[] inputs = fields.get("inputs");
        // Signing
        dict[] witnesses = new dict[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            String privatekey = selectPrivateKeys(addresses.getString(i), keys);
            String publickey = wallet.publickey_from_privatekey(privatekey, "cardano", false);
            byte[] signature = signing.signature_create(privatekey, txn, null, "cardano", false);
            dict witness = new dict();
            witness.put("publickey", publickey);
            witness.put("chaincode", binint.b2h(new byte[32]));
            witness.put("signature", signature);
            witnesses[i] = witness;
        }
        fields.put("witnesses", witnesses);
        byte[] signedtxn = transaction.transaction_encode(fields, "cardano", false);
        String txid = transaction.txnid(txn, "cardano", false);
        JSONObject packedTx = new JSONObject();
        packedTx.put("txid", txid);
        packedTx.put("signedTx", Base64.getEncoder().encodeToString(signedtxn));
        return packedTx.toString();
    }

    @Override
    public String toWIF(String privateKeyHex) {
        return null;
    }

    @Override
    public String calcRedeemScript(String segWitAddress) {
        return null;
    }

    @Override
    public String calcWitnessScript(String segWitAddress) {
        return null;
    }

    @Override
    public String calcSegWitAddress(String legacyAddress) {
        return null;
    }

    @Override
    public String generateP2PKHScript(String address) {
        return null;
    }

    @Override
    public String signSegWitTransaction(String rawTx, List<String> keys) {
        return null;
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee) {
        return encodeTransaction(utxos, recipients, changeAddress, fee, DECIMALS);
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, BigDecimal decimals) {
        // Inputs
        dict[] inputs = new dict[utxos.size()];
        JSONArray addresses = new JSONArray();
        BigDecimal totalInputAmount = new BigDecimal(0);
        for (int i = 0; i < utxos.size(); i++) {
            UnspentOutput utxo = utxos.get(i);
            dict input = new dict();
            input.put("txnid", utxo.getTxId());
            input.put("index", BigInteger.valueOf(utxo.getVout()));
            inputs[i] = input;
            addresses.put(utxo.getAddress());

            BigDecimal amount = utxo.getAmount();
            totalInputAmount = totalInputAmount.add(amount);
        }

        // Outputs
        BigDecimal totalOutputAmount = new BigDecimal(0);
        List<dict> outputs = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++) {
            Recipient recipient = recipients.get(i);
            dict output = new dict();
            output.put("amount", recipient.getAmount().multiply(decimals).toBigInteger());
            output.put("address", recipient.getAddress());
            outputs.add(output);
            BigDecimal amount = recipient.getAmount();
            totalOutputAmount = totalOutputAmount.add(amount);
        }
        if (totalInputAmount.compareTo(totalOutputAmount) < 1) {
            throw new RuntimeException("INSUFFICIENT FUNDS");
        }

        BigDecimal changeAmount = totalInputAmount.subtract(totalOutputAmount.add(fee));
        if (changeAmount.compareTo(DUST_THRESHOLD.divide(decimals)) > -1) {
            dict output = new dict();
            output.put("amount", changeAmount.multiply(decimals).toBigInteger());
            output.put("address", changeAddress);
            outputs.add(output);
        }
        dict fields = new dict();
        fields.put("inputs", inputs);
        fields.put("outputs", outputs.toArray(new dict[0]));
        byte[] txn = transaction.transaction_encode(fields, "cardano", false);
        JSONObject packedTx = new JSONObject();
        packedTx.put("addresses", addresses);
        packedTx.put("bytes", NumericUtil.bytesToHex(txn));
        return packedTx.toString();
    }
}
