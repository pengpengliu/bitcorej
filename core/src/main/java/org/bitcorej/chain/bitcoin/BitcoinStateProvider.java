package org.bitcorej.chain.bitcoin;

import org.bitcorej.chain.ChainState;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

public class BitcoinStateProvider implements ChainState {
    @Override
    public String getAddress(PrivateKey privKey) {
        return privKey.toPublicKey().toAddress();
    }

    @Override
    public String getAddress(PublicKey pubKey) {
        return pubKey.toAddress();
    }
}
