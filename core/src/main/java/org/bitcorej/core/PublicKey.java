package org.bitcorej.core;

import org.bitcoinj.core.ECKey;

import java.math.BigInteger;

public class PublicKey {
    private byte[] raw;
    private Network network;

    public PublicKey(byte[] raw, Network network) {
        this.raw = raw;
        this.network = network;
    }

    public PublicKey(String hex, Network network) {
        this.raw = new BigInteger(hex, 16).toByteArray();
        this.network = network;
    }

    public String toAddress() {
        ECKey key = ECKey.fromPublicOnly(raw);
        return key.toAddress(this.network.getNetworkParameters()).toString();
    }

    public BigInteger toBigInteger() {
        return new BigInteger(this.raw);
    }

    @Override
    public String toString() {
        return ECKey.fromPublicOnly(raw).getPublicKeyAsHex();
    }
}
