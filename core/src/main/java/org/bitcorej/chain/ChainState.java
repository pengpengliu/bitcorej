package org.bitcorej.chain;

import java.util.List;

public interface ChainState {
    KeyPair generateKeyPair(String secret);
    KeyPair generateKeyPair();

    byte[] signRawTransaction(byte[] rawTx, List<String> keys);
}