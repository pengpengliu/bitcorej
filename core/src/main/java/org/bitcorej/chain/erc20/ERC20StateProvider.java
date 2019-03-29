package org.bitcorej.chain.erc20;

import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.ethereum.EthereumStateProvider;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ERC20StateProvider extends EthereumStateProvider {
    private String address;
    private int decimals;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        JSONObject jsonObject = new JSONObject(rawTx);

        Transaction tx = new Transaction();

        BigInteger value = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("value")), 16);
        if (value.compareTo(new BigInteger("0")) != 0) {
            throw new RuntimeException("Invalid value");
        }

        if (!jsonObject.getString("to").equals(this.address)) {
            throw new RuntimeException("Invalid contract address");
        }

        String data = jsonObject.getString("data");
        Pattern pattern = Pattern.compile("^0xa9059cbb000000000000000000000000[a-fA-F0-9]{40}[a-fA-F0-9]{64}$");
        Matcher m = pattern.matcher(data);
        if (!m.matches()) {
            throw new RuntimeException("Invalid data");
        }

        String addressTo = "0x" + data.substring(34, 34 + 40);
        String amountHex = data.substring(34 + 40, 34 + 40 + 64);

        BigDecimal amount = new BigDecimal(NumericUtil.hexToBigInteger(amountHex)).divide(new BigDecimal(10).pow(this.decimals));

        List<Transaction.Input> from = new ArrayList<>();
        from.add(tx.new Input(jsonObject.getString("from"), amount));
        tx.setFrom(from);

        List<Transaction.Output> to = new ArrayList<>();
        to.add(tx.new Output(addressTo, amount, ""));
        tx.setTo(to);

        BigInteger gasPrice = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("gasPrice")), 16);
        BigInteger gasLimit = new BigInteger(NumericUtil.cleanHexPrefix(jsonObject.getString("gas")), 16);

        BigInteger fee = gasPrice.multiply(gasLimit);

        tx.setFee(new BigDecimal(fee).divide(new BigDecimal(10).pow(this.decimals)));
        return tx;
    }
}
