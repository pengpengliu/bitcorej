package org.bitcorej.chain.ethereum;

import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EthereumStateProvider implements ChainState {

    private static final BigDecimal DECIMALS = new BigDecimal(10).pow(18);
    private static final BigDecimal MAX_FEE = new BigDecimal("21000").multiply(new BigDecimal("0.00000002")); // 20 GWei

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        String address = Numeric.prependHexPrefix(Keys.getAddress(Sign.publicKeyFromPrivate(ecKey.getPrivKey())));
        return new KeyPair(ecKey.getPrivateKeyAsHex(), address);
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    @Override
    public Boolean validateTx(String rawTx, String tx) {
        Transaction decodedTx = this.decodeRawTransaction(rawTx);
        return decodedTx.equals(new Transaction(tx)) && decodedTx.getFee().compareTo(MAX_FEE) < 0;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        JSONObject jsonObject = new JSONObject(rawTx);

        Transaction tx = new Transaction();

        BigDecimal amount = new BigDecimal(new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("value")), 16)).divide(DECIMALS);

        List<Transaction.Input> from = new ArrayList<>();
        from.add(tx.new Input(jsonObject.getString("from"), amount));
        tx.setFrom(from);

        List<Transaction.Output> to = new ArrayList<>();
        to.add(tx.new Output(jsonObject.getString("to"), amount, ""));
        tx.setTo(to);

        BigInteger gasPrice = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("gasPrice")), 16);
        BigInteger gasLimit = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("gas")), 16);

        BigInteger fee = gasPrice.multiply(gasLimit);

        tx.setFee(new BigDecimal(fee).divide(DECIMALS));
        return tx;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject jsonObject = new JSONObject(rawTx);
        BigInteger nonce = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("nonce")), 16);
        BigInteger gasPrice = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("gasPrice")), 16);
        BigInteger gasLimit = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("gas")), 16);
        BigInteger value = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("value")), 16);
        String to = jsonObject.getString("to");
        String data = jsonObject.getString("data");
        RawTransaction tx = RawTransaction.createTransaction(nonce,
                gasPrice, gasLimit, to, value, data);

        String signedTx = NumericUtil.bytesToHex(TransactionEncoder.signMessage(tx, Credentials.create(keys.get(0))));

        JSONObject packed = new JSONObject();
        packed.put("raw", Numeric.prependHexPrefix(signedTx));
        return packed.toString();
    }
}
