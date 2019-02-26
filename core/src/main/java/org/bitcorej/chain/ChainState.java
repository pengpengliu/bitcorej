package org.bitcorej.chain;

import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

public interface ChainState {
    String getAddress(PrivateKey privKey);
    String getAddress(PublicKey pubKey);
}
