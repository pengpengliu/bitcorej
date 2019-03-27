package org.bitcorej.chain.stellar;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.json.JSONObject;
import org.stellar.sdk.*;

import java.math.BigDecimal;
import java.util.List;

public class StellarStateProvider implements ChainState {
    private static final BigDecimal DECIMALS = new BigDecimal(10).pow(7);

    private static final BigDecimal MAX_FEE = new BigDecimal("500").multiply(DECIMALS);

    private org.bitcorej.core.Network network;

    public StellarStateProvider(org.bitcorej.core.Network network) {
        switch (network) {
            case MAIN:
                Network.usePublicNetwork();
                break;
            case TEST:
                Network.useTestNetwork();
                break;
        }

        this.network = network;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        org.stellar.sdk.KeyPair pair = org.stellar.sdk.KeyPair.fromSecretSeed(secret);
        return new KeyPair(new String(pair.getSecretSeed()), pair.getAccountId());
    }

    @Override
    public KeyPair generateKeyPair() {
        org.stellar.sdk.KeyPair pair = org.stellar.sdk.KeyPair.random();
        return generateKeyPair(new String(pair.getSecretSeed()));
    }

    @Override
    public Boolean validateTx(String rawTx, String tx) {
        Transaction decodedTx = this.decodeRawTransaction(rawTx);
        return decodedTx.equals(new Transaction(tx)) && decodedTx.getFee().compareTo(MAX_FEE) < 0;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        JSONObject jsonObject = new JSONObject(rawTx);
        if (!jsonObject.getString("asset").equals("XLM"))
            return null;
        Transaction tx = new Transaction();
        BigDecimal amount = new BigDecimal(jsonObject.getString("amount"));
        tx.addInput(tx.new Input(jsonObject.getString("source"), amount));
        tx.addOutput(tx.new Output(jsonObject.getString("to"), amount, jsonObject.getString("memo")));
        tx.setFee(new BigDecimal(jsonObject.getString("fee")).divide(DECIMALS));
        return tx;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject jsonObject = new JSONObject(rawTx);
        String to = jsonObject.getString("to");
        Long sequence = Long.parseLong(jsonObject.getString("sequence"));
        String memo = jsonObject.getString("memo");
        BigDecimal amount = new BigDecimal(jsonObject.getString("amount"));
        org.stellar.sdk.KeyPair source = org.stellar.sdk.KeyPair.fromSecretSeed(keys.get(0));
        org.stellar.sdk.KeyPair destination = org.stellar.sdk.KeyPair.fromAccountId(to);
        org.stellar.sdk.Transaction transaction = new org.stellar.sdk.Transaction.Builder(new Account(source, sequence))
                .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), amount.toString()).build())
                .addMemo(Memo.text(memo))
                .setTimeout(10 * 1000L)
                .build();
        transaction.sign(source);
        JSONObject packedTx = new JSONObject();
        packedTx.put("XDR", transaction.toEnvelopeXdrBase64());
        return packedTx.toString();
    }
}
