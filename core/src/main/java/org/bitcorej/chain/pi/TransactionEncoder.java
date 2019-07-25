package org.bitcorej.chain.pi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

/**
 * Create RLP encoded transaction, implementation as per p4 of the
 * <a href="http://gavwood.com/paper.pdf">yellow paper</a>.
 */
public class TransactionEncoder {

    //all transactions must belong to one chain/chainid
	/*
    public static byte[] signMessage(RawTransaction rawTransaction, Credentials credentials) {
        byte[] encodedTransaction = encode(rawTransaction);
        Sign.SignatureData signatureData = Sign.signMessage(
                encodedTransaction, credentials.getEcKeyPair());

        return encode(rawTransaction, signatureData);
    }
    */

    public static byte[] signMessage(
            RawTransaction rawTransaction, String chainId, Credentials credentials) {

        BigInteger biChainId = chainIdToBigInteger(chainId);

        return signMessage(rawTransaction, biChainId, credentials);
    }

    public static byte[] signMessage(
            RawTransaction rawTransaction, BigInteger chainId, Credentials credentials) {

        byte[] encodedTransaction = encode(rawTransaction, chainId);
        Sign.SignatureData signatureData = Sign.signMessage(
                encodedTransaction, credentials.getEcKeyPair());

        Sign.SignatureData eip155SignatureData = createEip155SignatureData(signatureData, chainId);
        return encode(rawTransaction, eip155SignatureData);
    }

    public static Sign.SignatureData createEip155SignatureData(
            Sign.SignatureData signatureData, BigInteger chainId) {


        BigInteger biV = new BigInteger(signatureData.getV());
        biV = biV.add(BigInteger.valueOf(8));
        biV = biV.add(chainId.multiply(BigInteger.valueOf(2)));

        return new Sign.SignatureData(
                biV.toByteArray(), signatureData.getR(), signatureData.getS());
    }

    public static byte[] encode(RawTransaction rawTransaction) {
        return encode(rawTransaction, (Sign.SignatureData)null);
    }

    public static byte[] encode(RawTransaction rawTransaction, String chainId) {

        BigInteger biChainId = chainIdToBigInteger(chainId);

        return encode(rawTransaction, biChainId);
    }

    public static byte[] encode(RawTransaction rawTransaction, BigInteger chainId) {

        byte[] v = new byte[] {};

        if (chainId != null) {
            v = Numeric.toBytesPadded(chainId, 32);
        }

        Sign.SignatureData signatureData = new Sign.SignatureData(
                v, new byte[] {}, new byte[] {});
        return encode(rawTransaction, signatureData);
    }

    private static byte[] encode(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    static List<RlpType> asRlpValues(
            RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();

        result.add(RlpString.create(rawTransaction.getNonce()));
        result.add(RlpString.create(rawTransaction.getGasPrice()));
        result.add(RlpString.create(rawTransaction.getGasLimit()));

        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String to = rawTransaction.getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(rawTransaction.getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
        result.add(RlpString.create(data));

        if (signatureData != null) {
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getV())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }

    public static BigInteger chainIdToBigInteger(String chainId) {

        byte[] byChainId = org.web3j.crypto.Hash.sha3(chainId.getBytes());
        String chainIdHash = Numeric.toHexString(byChainId);
        return Numeric.decodeQuantity(chainIdHash);
    }
}
