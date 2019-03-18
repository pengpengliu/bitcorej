package org.bitcorej.chain.stellar;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;

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
    public String signRawTransaction(String rawTx, List<String> keys) {
        return null;
    }
}
