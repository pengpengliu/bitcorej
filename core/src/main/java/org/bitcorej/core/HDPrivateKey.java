package org.bitcorej.core;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

public class HDPrivateKey {
    private DeterministicKey key;
    private Network network;

    public HDPrivateKey(String serialized, Network network) {
        this.key = DeterministicKey.deserializeB58(serialized, network.getNetworkParameters());
        this.network = network;
    }

    public HDPrivateKey derived(int index) {
        DeterministicKey derived = HDKeyDerivation.deriveChildKey(this.key, new ChildNumber(index, false));
        return new HDPrivateKey(derived.serializePrivB58(network.getNetworkParameters()), network);
    }

    public PrivateKey getPrivKey() {
        return new PrivateKey(this.key.getPrivKeyBytes(), network);
    }

    public PublicKey getPubKey() {
        return new PublicKey(this.key.getPubKey(), network);
    }

    @Override
    public String toString() {
        return key.serializePrivB58(network.getNetworkParameters());
    }
}
