package org.bitcorej.chain;

import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

import java.util.List;

public interface ChainState {
    String createAddress(PrivateKey privKey);
    String createAddress(PublicKey pubKey);

    String createAddress(List<PublicKey> publicKeys);

    // HashMap<String, Object> decodeRawTransaction(byte[] rawTx);
    byte[] signRawTransaction(byte[] rawTx, List<PrivateKey> keys);
}