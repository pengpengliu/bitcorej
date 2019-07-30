package org.bitcorej.chain.gxc;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import org.bitcoinj.core.*;
import org.bitcorej.utils.NumericUtil;

import java.util.Arrays;

public class WIF {
    private static final int VERSION = 0x80;

    private ECKey ecKey = null;

    public WIF(String wif) {
        byte[] decoded = Base58.decode(wif);
        Preconditions.checkArgument(Math.abs(decoded[0]) == VERSION, "error version || valid privateKeyStr");
        byte[] priByte = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
        byte[] checkSum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
        byte[] calculateCheckSum = calculateChecksum(priByte);
        Preconditions.checkArgument(Arrays.deepEquals(new byte[][]{checkSum}, new byte[][]{calculateCheckSum}), "checkSum error");
        this.ecKey = ECKey.fromPrivate(Arrays.copyOfRange(priByte, 1, priByte.length));
    }

    public static WIF fromPrivateKey (String privateKey) {
        return new WIF(privateKeyToWIF(privateKey));
    }

    private static String privateKeyToWIF(String privateKey) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(privateKey));
        byte[] priKey = ecKey.getPrivKeyBytes();
        byte[] version = {(byte) VERSION};
        byte[] result = Bytes.concat(version, priKey);
        return Base58.encode(Bytes.concat(result, calculateChecksum(result)));
    }

    @Override
    public String toString() {
        return privateKeyToWIF(this.ecKey.getPrivateKeyAsHex());
    }

    public String toPrivateKeyAsHex() {
        return ecKey.getPrivateKeyAsHex();
    }

    private static byte[] calculateChecksum(byte[] data) {
        return Arrays.copyOfRange(Sha256Hash.hashTwice(data), 0, 4);
    }
}
