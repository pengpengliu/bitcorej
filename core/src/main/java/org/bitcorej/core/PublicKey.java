package org.bitcorej.core;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;

import java.math.BigInteger;

public class PublicKey {
    private byte[] raw;

    public PublicKey(byte[] raw) {
        this.raw = raw;
    }

    public String toAddress() {
        ECKey key = ECKey.fromPublicOnly(raw);
        return key.toAddress(MainNetParams.get()).toString();
    }

    @Override
    public String toString() {
        return new BigInteger(raw).toString(16);
    }
}
