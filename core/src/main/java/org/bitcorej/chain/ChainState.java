package org.bitcorej.chain;

import java.util.List;

public interface ChainState {
    KeyPair generateKeyPair(String secret);
    KeyPair generateKeyPair();

    // Boolean validateTx(List from, List to, String rawTx);

    String signRawTransaction(String rawTx, List<String> keys);
}