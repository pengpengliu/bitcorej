package org.bitcorej.chain.cosmos;

import org.bitcorej.utils.ByteUtil;

import java.math.BigInteger;

public class Amino {
    public static byte[] MarshalBinary(byte[] prefixBytes, byte[] message) {
        prefixBytes = ByteUtil.concat(prefixBytes, BigInteger.valueOf(message.length).toByteArray());
        prefixBytes = ByteUtil.concat(prefixBytes, message);
        return prefixBytes;
    }
}
