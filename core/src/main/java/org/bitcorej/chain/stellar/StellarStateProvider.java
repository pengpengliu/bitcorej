package org.bitcorej.chain.stellar;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;

import java.util.List;

public class StellarStateProvider implements ChainState {

    @Override
    public KeyPair generateKeyPair(String secret) {
        org.stellar.sdk.KeyPair pair = org.stellar.sdk.KeyPair.fromSecretSeed(secret);
        return new KeyPair(new String(pair.getSecretSeed()), pair.getAccountId());
    }

    @Override
    public KeyPair generateKeyPair() {
        org.stellar.sdk.KeyPair pair = org.stellar.sdk.KeyPair.random();
        return generateKeyPair(new String(pair.getSecretSeed()));
    }

    @Override
    public Boolean validateTx(String rawTx, String tx) {
        return null;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        return null;
    }
}
