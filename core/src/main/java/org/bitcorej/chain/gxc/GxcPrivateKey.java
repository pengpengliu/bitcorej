package org.bitcorej.chain.gxc;

import java.math.BigInteger;
import java.security.SecureRandom;


public class GxcPrivateKey {

    private final BigInteger mPrivateKey;
    private final GxcPublicKey mPublicKey;

    private final CurveParam mCurveParam;

    private static final SecureRandom mSecRandom;

    static {
        mSecRandom = new SecureRandom();
    }

    public static SecureRandom getSecuRandom() {
        return mSecRandom;
    }

    public GxcPrivateKey() {
        this(CurveParam.SECP256_K1);
    }

    public GxcPrivateKey(int curveParamType) {
        mCurveParam = EcTools.getCurveParam(curveParamType);

        mPrivateKey = getOrCreatePrivKeyBigInteger(null);
        mPublicKey = new GxcPublicKey(findPubKey(mPrivateKey), mCurveParam);
    }

    public GxcPrivateKey(String wif) {
        RefValue<CurveParam> curveParamRef = new RefValue<>();

        byte[] keyBytes = GxcEcUtil.parseKeyBase58(wif, curveParamRef, null);
        if ((null == keyBytes) || (keyBytes.length < 5)) {
            throw new IllegalArgumentException("Invalid wif length");
        }

        mCurveParam = curveParamRef.data;

        mPrivateKey = getOrCreatePrivKeyBigInteger(keyBytes);
        mPublicKey = new GxcPublicKey(findPubKey(mPrivateKey), mCurveParam);
    }

    public GxcPrivateKey(byte[] keyBytes) {
        if ((null == keyBytes) || (keyBytes.length < 5)) {
            throw new IllegalArgumentException("Invalid wif length");
        }

        mCurveParam = EcTools.getCurveParam(CurveParam.SECP256_K1);

        mPrivateKey = getOrCreatePrivKeyBigInteger(keyBytes);
        mPublicKey = new GxcPublicKey(findPubKey(mPrivateKey), mCurveParam);
    }

    public void clear() {
        mPrivateKey.multiply(BigInteger.ZERO);
    }

    private byte[] findPubKey(BigInteger bnum) {
        EcPoint Q = EcTools.multiply(mCurveParam.G(), bnum);// Secp256k1Param.G, bnum);

        // Q를 curve 상에서, compressed point 로 변환하자. ( 압축을 위해 )

        Q = new EcPoint(Q.getCurve(), Q.getX(), Q.getY(), true);

        return Q.getEncoded();
    }

    public GxcPublicKey getPublicKey() {
        return mPublicKey;
    }

    public String toWif() {
        byte[] rawPrivKey = getBytes();
        byte[] resultWIFBytes = new byte[1 + 32 + 4];

        resultWIFBytes[0] = (byte) 0x80;
        System.arraycopy(rawPrivKey, rawPrivKey.length > 32 ? 1 : 0, resultWIFBytes, 1, 32);

        Sha256 hash = Sha256.doubleHash(resultWIFBytes, 0, 33);

        System.arraycopy(hash.getBytes(), 0, resultWIFBytes, 33, 4);

        return Base58.encode(resultWIFBytes);
    }

    public CurveParam getCurveParam() {
        return mCurveParam;
    }

    /**
     * @brief signature hash
     * @note use Elliptic Curve Cryptography signture hash
     */
    public EcSignature sign(Sha256 digest) {
        EcSignature signature = null;
        do {
            signature = EcDsa.sign(digest, this);
        } while (!signature.isFCCanonical());
        return signature;
    }

    @Override
    public String toString() {
        return toWif();
    }

    public BigInteger getAsBigInteger() {
        return mPrivateKey;
    }


    public byte[] getBytes() {
        byte[] result = new byte[32];
        byte[] bytes = mPrivateKey.toByteArray();
        if (bytes.length <= result.length) {
            System.arraycopy(bytes, 0, result, result.length - bytes.length, bytes.length);
        } else {
            assert bytes.length == 33 && bytes[0] == 0;
            System.arraycopy(bytes, 1, result, 0, bytes.length - 1);
        }
        return result;
    }

    public byte[] getBytes(BigInteger value) {
        byte[] result = new byte[32];
        byte[] bytes = value.toByteArray();
        if (bytes.length <= result.length) {
            System.arraycopy(bytes, 0, result, result.length - bytes.length, bytes.length);
        } else {
            // This happens if the most significant bit is set and we have an
            // extra leading zero to avoid a negative BigInteger
            assert bytes.length == 33 && bytes[0] == 0;
            System.arraycopy(bytes, 1, result, 0, bytes.length - 1);
        }
        return result;
    }

    private BigInteger toUnsignedBigInteger(BigInteger value) {
        if (value.signum() < 0) {
            return new BigInteger(1, value.toByteArray());
        }

        return value;
    }

    private BigInteger toUnsignedBigInteger(byte[] value) {
        if (((value[0]) & 0x80) != 0) {
            return new BigInteger(1, value);
        }

        return new BigInteger(value);
    }

    private BigInteger getOrCreatePrivKeyBigInteger(byte[] value) {
        if (null != value) {
            if (((value[0]) & 0x80) != 0) {
                return new BigInteger(1, value);
            }

            return new BigInteger(value);
        }


        int nBitLength = mCurveParam.n().bitLength();// Secp256k1Param.n.bitLength();
        BigInteger d;
        do {
            // Make a BigInteger from bytes to ensure that Android and 'classic'
            // java make the same BigIntegers from the same random source with the
            // same seed. Using BigInteger(nBitLength, random)
            // produces different results on Android compared to 'classic' java.
            byte[] bytes = new byte[nBitLength / 8];
            mSecRandom.nextBytes(bytes);
            bytes[0] = (byte) (bytes[0] & 0x7F); // ensure positive number
            d = new BigInteger(bytes);
        }
        while (d.equals(BigInteger.ZERO) || (d.compareTo(mCurveParam.n()) >= 0));// Secp256k1Param.n) >= 0));

        return d;
    }
}