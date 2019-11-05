package org.bitcorej.chain.vsys.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BytesHelper {

    public static byte[] toBytes(long x) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--,x >>= 8) {
            result[i] = (byte)(x & 0xFF);
        }
        return result;
    }

    public static byte[] toBytes(int x) {
        byte[] result = new byte[4];
        for (int i = 3; i >= 0; i--,x >>= 8) {
            result[i] = (byte)(x & 0xFF);
        }
        return result;
    }

    public static byte[] toBytes(short x) {
        byte[] result = new byte[2];
        result[0] = (byte) (x >> 8);
        result[1] = (byte) x;
        return result;
    }

    public static byte[] toBytes(Byte x) {
        return new byte[]{ x };
    }

    public static byte[] toBytes(String x) {
        return x.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toBytes(List<Byte> list) {
        byte[] bytes = new byte[list.size()];
        for (int i = 0 ; i < list.size(); i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }

    public static List<Byte> toList(byte[] bytes) {
        List<Byte> list = new ArrayList<Byte>();
        for (byte b : bytes) {
            list.add(b);
        }
        return list;
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString();
    }

    public static String toHex(List<Byte> bytes) {
        StringBuilder sb = new StringBuilder();
        for (Byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString();
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] concat(byte[][] arr) {
        byte[] result = new byte[0];
        for (byte[] bs : arr) {
            result = concat(result, bs);
        }
        return result;
    }
}
