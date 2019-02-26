package org.bitcorej.core;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;

public class HDPrivateKey {
    private DeterministicKey key;

    public HDPrivateKey(String serialized) {
        this.key = DeterministicKey.deserializeB58(serialized, MainNetParams.get());
    }

    public HDPrivateKey derived(int index) {
        DeterministicKey derived = HDKeyDerivation.deriveChildKey(this.key, new ChildNumber(index, false));
        return new HDPrivateKey(derived.serializePrivB58(MainNetParams.get()));
    }

    public PrivateKey getPrivKey() {
        return new PrivateKey(this.key.getPrivKeyBytes());
    }

    public PublicKey getPubKey() {
        return new PublicKey(this.key.getPubKey());
    }

    @Override
    public String toString() {
        return key.serializePrivB58(MainNetParams.get());
    }
}
