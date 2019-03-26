package org.bitcorej.chain;

import java.util.List;

public interface ChainState {
    KeyPair generateKeyPair(String secret);
    KeyPair generateKeyPair();

    Boolean validateTx(String rawTx, String requestTx);
    Transaction decodeRawTransaction(String rawTx);
    String signRawTransaction(String rawTx, List<String> keys);
}