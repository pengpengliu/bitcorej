package org.bitcorej.chain;

import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

import java.util.ArrayList;

public interface ChainState {
    String getAddress(PrivateKey privKey);
    String getAddress(PublicKey pubKey);

    // HashMap<String, Object> decodeRawTransaction(byte[] rawTx);
    byte[] signRawTransaction(byte[] rawTx, ArrayList<PrivateKey> keys);
}