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

    private static boolean hasHexPrefix(String input) {
        return input.length() > 1 && input.charAt(0) == '0' && input.charAt(1) == 'x';
    }

    public static String cleanHexPrefix(String input) {
        if (hasHexPrefix(input)) {
            return input.substring(2);
        } else {
            return input;
        }
    }

    public static byte[] hexToBytes(String input) {
        String cleanInput = cleanHexPrefix(input);

        int len = cleanInput.length();

        if (len == 0) {
            return new byte[]{};
        }

        byte[] data;
        int startIdx;
        if (len % 2 != 0) {
            data = new byte[(len / 2) + 1];
            data[0] = (byte) Character.digit(cleanInput.charAt(0), 16);
            startIdx = 1;
        } else {
            data = new byte[len / 2];
            startIdx = 0;
        }

        for (int i = startIdx; i < len; i += 2) {
            data[(i + 1) / 2] = (byte) ((Character.digit(cleanInput.charAt(i), 16) << 4)
                    + Character.digit(cleanInput.charAt(i + 1), 16));
        }
        return data;
    }
}
