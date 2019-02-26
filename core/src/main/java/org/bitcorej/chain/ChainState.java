package org.bitcorej.chain;

import org.bitcorej.core.PrivateKey;

public interface ChainState {
    String getAddress(PrivateKey privKey);
}
