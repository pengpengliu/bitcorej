package org.bitcorej.core;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;

import java.util.List;

public class HDKeychain {

    private DeterministicKeyChain keychain;

    public HDKeychain(String seedPhrase) throws Exception {
        DeterministicSeed seed = new DeterministicSeed(seedPhrase, null, "", 1409478661L);
        this.keychain = DeterministicKeyChain.builder().seed(seed).build();
    }

    public HDPrivateKey derivedKey(String path) {
        path = path.replace("m", "M").replace("'", "H");
        List<ChildNumber> keyPath = HDUtils.parsePath(path);
        DeterministicKey key = keychain.getKeyByPath(keyPath, true);
        return new HDPrivateKey(key.serializePrivB58(MainNetParams.get()));
    }

}
