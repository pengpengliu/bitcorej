package org.bitcorej.chain.ethereum;

import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

public class EthereumStateProvider implements ChainState {

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
