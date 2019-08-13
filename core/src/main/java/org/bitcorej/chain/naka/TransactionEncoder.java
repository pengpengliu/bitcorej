package org.bitcorej.chain.naka;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
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

    private static final Long CHAIN_ID_INC = Long.valueOf(35);

    public static byte[] signMessage(
            RawTransaction rawTransaction, Long chainId, Credentials credentials) {
        byte[] encodedTransaction = encode(rawTransaction, chainId);
        Sign.SignatureData signatureData = Sign.signMessage(
                encodedTransaction, credentials.getEcKeyPair());
        Sign.SignatureData eip155SignatureData = createEip155SignatureData(
                encodedTransaction, signatureData,
                credentials.getEcKeyPair().getPublicKey(), chainId);
        return encode(rawTransaction, eip155SignatureData);
    }

    // Modifies V value based on EIP155 specs
    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md
    // https://github.com/ethereum/go-ethereum/blob/938cf4528ab5acbb6013be79a0548956713807a8/crypto/secp256k1/libsecp256k1/src/ecdsa_impl.h#L294
    public static Sign.SignatureData createEip155SignatureData(
            byte[] encodedTransaction, Sign.SignatureData sigData,
            BigInteger pubKey, Long chainId) {

        // Get recovery param so we know what to increment V by
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            BigInteger k = Sign.recoverFromSignature(
                    i,
                    new ECDSASignature(new BigInteger(1, sigData.getR()),
                            new BigInteger(1, sigData.getS())),
                    Hash.sha3(encodedTransaction));

            if (k != null && k.equals(pubKey)) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct EIP155 signature.");
        }

        // Modify V for replay attack protection
        final Long modifiedV = (chainId * 2) + CHAIN_ID_INC + recId;
        final byte[] v = ByteBuffer.allocate(Long.BYTES).putLong(modifiedV.longValue()).array();

        return new Sign.SignatureData(v, sigData.getR(), sigData.getS());
    }

    public static byte[] encode(RawTransaction rawTransaction) {
        return encode(rawTransaction, (Sign.SignatureData) null);
    }

    public static byte[] encode(RawTransaction rawTransaction, Long chainId) {
        Sign.SignatureData signatureData = new Sign.SignatureData(
                ByteBuffer.allocate(Long.BYTES).putLong(chainId.longValue()).array(), new byte[] {}, new byte[] {});
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

        String token = rawTransaction.getToken();
        if (token != null && token.length() > 0) {
            result.add(RlpString.create(Numeric.hexStringToByteArray(token)));
        } else {
            result.add(RlpString.create(""));
        }

        String exchanger = rawTransaction.getExchanger();
        if (exchanger != null && exchanger.length() > 0) {
            result.add(RlpString.create(Numeric.hexStringToByteArray(exchanger)));
        } else {
            result.add(RlpString.create(""));
        }

        BigInteger exchangeRate = rawTransaction.getExchangeRate();
        if (exchangeRate != null && exchangeRate.compareTo(BigInteger.ZERO) > 0) {
            result.add(RlpString.create(rawTransaction.getValue()));
        } else {
            result.add(RlpString.create(BigInteger.ZERO));
        }

        if (signatureData != null) {
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getV())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }
}