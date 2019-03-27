package org.bitcorej.chain.bsv;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class BSVStateProvider extends BitcoinStateProvider {
    public BSVStateProvider(Network network) {
        super(network);
    }
}