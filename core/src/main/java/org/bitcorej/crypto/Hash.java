package org.bitcorej.crypto;

import org.bitcorej.utils.NumericUtil;

import java.security.MessageDigest;

public class Hash {
    public static String sha256(String hexInput) {
        byte[] bytes = NumericUtil.hexToBytes(hexInput);
        byte[] result = sha256(bytes);
        return NumericUtil.bytesToHex(result);
    }

    public static byte[] sha256(byte[] input) {
        return sha256(input, 0, input.length);
    }

    private static byte[] sha256(byte[] input, int offset, int length) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input, offset, length);
            return md.digest();
        } catch (Exception ex) {
            throw new RuntimeException("WALLET_SHA256");
        }
    }
}
