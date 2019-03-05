package org.bitcorej.utils;

public class NumericUtil {
    public static String bytesToHex(byte[] input) {
        StringBuilder stringBuilder = new StringBuilder();
        if (input.length == 0) {
            return "";
        }

        for (byte anInput : input) {
            stringBuilder.append(String.format("%02x", anInput));
        }

        return stringBuilder.toString();
    }
}
