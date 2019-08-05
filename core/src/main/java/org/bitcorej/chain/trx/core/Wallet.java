package org.bitcorej.chain.trx.core;

public class Wallet {
    private static byte addressPreFixByte = Constant.ADD_PRE_FIX_BYTE_MAINNET;

    public static byte getAddressPreFixByte() {
        return addressPreFixByte;
    }
}
