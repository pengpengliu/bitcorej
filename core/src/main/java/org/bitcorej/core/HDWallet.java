package org.bitcorej.core;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.MnemonicUtils;

import java.security.SecureRandom;
import java.util.List;

public class HDWallet {
    private DeterministicKeyChain keychain;
    private String seedPhrase;

    public HDWallet() throws Exception {
        byte[] entropy = new byte[16];
        new SecureRandom().nextBytes(entropy);

        this.commonInit(MnemonicUtils.generateMnemonic(entropy));
    }

    public HDWallet(String seedPhrase) throws Exception {
        this.commonInit(seedPhrase);
    }

    private void commonInit(String seedPhrase) throws Exception {
        if(!MnemonicUtils.validateMnemonic(seedPhrase)) {
            throw new IllegalArgumentException("Invalid seed phrase");
        }
        this.seedPhrase = seedPhrase;

        DeterministicSeed seed = new DeterministicSeed(seedPhrase, null, "", 1409478661L);
        this.keychain = DeterministicKeyChain.builder().seed(seed).build();
    }

    public HDPrivateKey derivedKey(String path, Network network) {
        path = path.replace("m", "M").replace("'", "H");
        List<ChildNumber> keyPath = HDUtils.parsePath(path);
        DeterministicKey key = keychain.getKeyByPath(keyPath, true);
        return new HDPrivateKey(key.serializePrivB58(network.getNetworkParameters()), network);
    }
}
