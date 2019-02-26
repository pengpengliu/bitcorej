package org.bitcorej.core;

import org.bitcoinj.core.ECKey;

import java.math.BigInteger;

public class PrivateKey {
    private byte[] raw;

    public PrivateKey(byte[] raw) {
        this.raw = raw;
    }

    public PublicKey toPublicKey() {
        return new PublicKey(ECKey.publicKeyFromPrivate(new BigInteger(raw), true));
    }

    @Override
    public String toString() {
        return new BigInteger(raw).toString(16);
    }
}
