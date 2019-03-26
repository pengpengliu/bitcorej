package org.bitcorej.chain.stellar;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;

import java.util.List;

public class StellarStateProvider implements ChainState {

    @Override
    public KeyPair generateKeyPair(String secret) {
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
        return null;
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
