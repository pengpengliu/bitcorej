package org.bitcorej.core;

import org.bitcoinj.core.ECKey;

import java.math.BigInteger;

public class PrivateKey {
    private byte[] raw;
    private Network network;

    public PrivateKey(byte[] raw, Network network) {
        this.raw = raw;
        this.network = network;
    }

    public PrivateKey(String hex, Network network) {
        this.raw = new BigInteger(hex, 16).toByteArray();
        this.network = network;
    }

    public PublicKey toPublicKey() {
        return new PublicKey(ECKey.publicKeyFromPrivate(new BigInteger(raw), true), network);
    }

    public BigInteger toBigInteger() {
        return new BigInteger(this.raw);
    }

    public String toWIF() {
        return ECKey.fromPrivate(raw).getPrivateKeyAsWiF(this.network.getNetworkParameters());
    }

    @Override
    public String toString() {
        return new BigInteger(raw).toString(16);
    }
}
