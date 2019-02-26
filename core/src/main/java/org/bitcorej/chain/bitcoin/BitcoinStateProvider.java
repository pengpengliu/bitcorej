package org.bitcorej.chain.bitcoin;

import org.bitcorej.chain.ChainState;
import org.bitcorej.core.PrivateKey;

public class BitcoinStateProvider implements ChainState {
    @Override
    public String getAddress(PrivateKey privKey) {
        return privKey.toPublicKey().toAddress();
    }
}
