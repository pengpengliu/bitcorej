package org.bitcorej.chain.gxc;

import java.util.Arrays;


public class GxcPublicKey {
    private static final int CHECK_BYTE_LEN = 4;

    private final long mCheck;
    private final CurveParam mCurveParam;
    private final byte[] mData;

    public static class IllegalGxcPubkeyFormatException extends IllegalArgumentException {
        public IllegalGxcPubkeyFormatException(String pubkeyStr) {
            super("invalid gxc public key : " + pubkeyStr);
        }
    }

    public GxcPublicKey(byte[] data) {
        this(data, EcTools.getCurveParam(CurveParam.SECP256_K1));
    }

    public GxcPublicKey(byte[] data, CurveParam curveParam) {
        mData = Arrays.copyOf(data, 33);
        mCurveParam = curveParam;

        mCheck = BitUtils.uint32ToLong(Ripemd160.from(mData, 0, mData.length).bytes(), 0);
    }

    public GxcPublicKey(String base58Str) {

        RefValue<CurveParam> curveParamRefValue = new RefValue<>();
        RefValue<Long> checksumRef = new RefValue<>();
        mData = GxcEcUtil.parseKeyBase58(base58Str, curveParamRefValue, checksumRef);
        mCurveParam = curveParamRefValue.data;
        mCheck = checksumRef.data;
    }

    public byte[] getBytes() {
        return mData;
    }

    public String getBytesAsHexStr() {
        return HexUtils.toHex(mData);
    }


    @Override
    public String toString() {

        byte[] postfixBytes = CurveParam.SECP256_K1 == mCurveParam.getCurveParamType() ? new byte[0] : "R1".getBytes();
        byte[] toDigest = new byte[mData.length + postfixBytes.length];
        System.arraycopy(mData, 0, toDigest, 0, mData.length);

        if (postfixBytes.length > 0) {
            System.arraycopy(postfixBytes, 0, toDigest, mData.length, postfixBytes.length);
        }

        byte[] digest = Ripemd160.from(toDigest).bytes();
        byte[] result = new byte[CHECK_BYTE_LEN + mData.length];

        System.arraycopy(mData, 0, result, 0, mData.length);
        System.arraycopy(digest, 0, result, mData.length, CHECK_BYTE_LEN);

        return GxcEcUtil.GXC_PREFIX + Base58.encode(result);
    }

    @Override
    public int hashCode() {
        return (int) (mCheck & 0xFFFFFFFFL);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (null == other || getClass() != other.getClass())
            return false;

        return BitUtils.areEqual(this.mData, ((GxcPublicKey) other).mData);
    }

    public boolean verify(byte[] messhash, byte[] signature) {
        EcSignature ecSignature = EcSignature.decoding(true, signature);
        return EcDsa.isSignerOf(mCurveParam, messhash, ecSignature.recId, ecSignature, getBytes());
    }

    public boolean verify(byte[] messhash, EcSignature ecSignature) {
        return EcDsa.isSignerOf(mCurveParam, messhash, ecSignature.recId, ecSignature, getBytes());
    }
}
