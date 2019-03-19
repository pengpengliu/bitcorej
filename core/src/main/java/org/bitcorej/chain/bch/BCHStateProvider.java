package org.bitcorej.chain.bch;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class BCHStateProvider extends BitcoinStateProvider {
    public BCHStateProvider(Network network) {
        super(network);
    }
}
