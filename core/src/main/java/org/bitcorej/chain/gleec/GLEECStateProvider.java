package org.bitcorej.chain.gleec;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class GLEECStateProvider extends BitcoinStateProvider {
    public GLEECStateProvider(Network network) {
        super(network);
        super.params = GLEECNetParams.get();
    }
}
