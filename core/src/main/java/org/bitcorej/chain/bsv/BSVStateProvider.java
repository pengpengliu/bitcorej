package org.bitcorej.chain.bsv;

import org.bitcorej.chain.bch.BCHStateProvider;
import org.bitcorej.core.Network;

public class BSVStateProvider extends BCHStateProvider {
    public BSVStateProvider(Network network) {
        super(network);
    }
}