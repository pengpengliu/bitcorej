package org.bitcorej.chain.ask;

import org.bitcorej.chain.ethereum.EthereumStateProvider;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

public class ASKStateProvider extends EthereumStateProvider {
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

        TransactionEncoder.encode(tx);

        JSONObject packed = new JSONObject();
        packed.put("raw", Numeric.prependHexPrefix(signedTx));
        return packed.toString();
    }
}
