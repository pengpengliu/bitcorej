package org.bitcorej.chain.naka;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class RawTransaction {

    private BigInteger nonce;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private String to;
    private BigInteger value;
    private String data;
    private String token;
    private String exchanger;
    private BigInteger exchangeRate;

    protected RawTransaction(BigInteger nonce, BigInteger gasPrice,
                             BigInteger gasLimit, String to, BigInteger value, String data,
                             String token, String exchanger, BigInteger exchangeRate) {
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.to = to;
        this.value = value;

        if (data != null) {
            this.data = Numeric.cleanHexPrefix(data);
        }

        this.token = token;
        this.exchanger = exchanger;
        this.exchangeRate = exchangeRate;
    }

    public static RawTransaction createContractTransaction(BigInteger nonce,
                                                           BigInteger gasPrice, BigInteger gasLimit, BigInteger value,
                                                           String init, String token, String exchanger,
                                                           BigInteger exchangeRate) {
        return new RawTransaction(nonce, gasPrice, gasLimit, "", value, init,
                token, exchanger, exchangeRate);
    }

    public static RawTransaction createEtherTransaction(BigInteger nonce,
                                                        BigInteger gasPrice, BigInteger gasLimit, String to,
                                                        BigInteger value, String token, String exchanger,
                                                        BigInteger exchangeRate) {
        return new RawTransaction(nonce, gasPrice, gasLimit, to, value, "",
                token, exchanger, exchangeRate);
    }

    public static RawTransaction createTransaction(BigInteger nonce,
                                                   BigInteger gasPrice, BigInteger gasLimit, String to, String data,
                                                   String token, String exchanger, BigInteger exchangeRate) {
        return createTransaction(nonce, gasPrice, gasLimit, to, BigInteger.ZERO,
                data, token, exchanger, exchangeRate);
    }

    public static RawTransaction createTransaction(BigInteger nonce,
                                                   BigInteger gasPrice, BigInteger gasLimit, String to,
                                                   BigInteger value, String data, String token, String exchanger,
                                                   BigInteger exchangeRate) {
        return new RawTransaction(nonce, gasPrice, gasLimit, to, value, data,
                token, exchanger, exchangeRate);
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public String getData() {
        return data;
    }

    public String getToken() {
        return token;
    }

    public String getExchanger() {
        return exchanger;
    }

    public BigInteger getExchangeRate() {
        return exchangeRate;
    }
}
