package org.bitcorej.chain.usdt;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class USDTStateProvider extends BitcoinStateProvider {
    public USDTStateProvider(Network network) {
        super(network);
    }
}
