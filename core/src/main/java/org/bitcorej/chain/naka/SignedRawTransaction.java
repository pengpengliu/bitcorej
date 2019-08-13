package org.bitcorej.chain.naka;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SignatureException;

import org.web3j.crypto.Keys;

public class SignedRawTransaction extends RawTransaction {

    private static final long CHAIN_ID_INC = 35;
    private static final long LOWER_REAL_V = 27;

    private Sign.SignatureData signatureData;

    public SignedRawTransaction(BigInteger nonce, BigInteger gasPrice,
                                BigInteger gasLimit, String to, BigInteger value, String data,
                                String token, String exchanger, BigInteger exchangeRate,
                                Sign.SignatureData signatureData) {
        super(nonce, gasPrice, gasLimit, to, value, data, token, exchanger,
                exchangeRate);
        this.signatureData = signatureData;
    }

    public Sign.SignatureData getSignatureData() {
        return signatureData;
    }

    public Long getChainId() {
        final long v = new BigInteger(signatureData.getV()).longValue();
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) {
            return null;
        }
        return Long.valueOf((v - CHAIN_ID_INC) / 2);
    }

    public String getFrom() throws SignatureException {
        Long chainId = getChainId();
        byte[] encodedTransaction;
        if (null == chainId) {
            encodedTransaction = TransactionEncoder.encode(this);
        } else {
            encodedTransaction = TransactionEncoder.encode(this, chainId);
        }
        byte[] v = signatureData.getV();
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        Sign.SignatureData signatureDataV = new Sign.SignatureData(
                getRealV(v), r, s);
        BigInteger key = Sign.signedMessageToKey(
                encodedTransaction, signatureDataV);
        return "0x" + Keys.getAddress(key);
    }

    public void verify(String from) throws SignatureException {
        String actualFrom = getFrom();
        if (!actualFrom.equals(from)) {
            throw new SignatureException("from mismatch");
        }
    }

    private byte[] getRealV(byte[] v) {
        final long vLong = new BigInteger(signatureData.getV()).longValue();

        if (vLong == LOWER_REAL_V || vLong == (LOWER_REAL_V + 1)) {
            return v;
        }

        long realV = LOWER_REAL_V;
        int inc = 0;
        if (vLong % 2 == 0) {
            inc = 1;
        }
        Long rv = realV + inc;
        return ByteBuffer.allocate(Long.BYTES).putLong(rv.longValue()).array();
    }
}
