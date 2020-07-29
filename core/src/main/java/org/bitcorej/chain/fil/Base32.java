package org.bitcorej.chain.fil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Base32 {
    private final static String alphabet = "abcdefghijklmnopqrstuvwxyz234567";

    public static String encode(byte[] bytes) {
        int length = bytes.length;
        int[] view = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            view[i] = bytes[i] & 0xFF;
        }
        int bits = 0;
        int value = 0;
        String output = "";
        for (int i = 0; i < length; i++) {
            value = (value << 8) | view[i];
            bits += 8;

            while (bits >= 5) {
                output += alphabet.toCharArray()[(value >>> (bits - 5)) & 31];
                bits -= 5;
            }
        }

        if (bits > 0) {
            output += alphabet.toCharArray()[(value << (5 - bits)) & 31];
        }
        return output;
    }
}
