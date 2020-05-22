package org.bitcorej.chain.rvn;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class RVNStateProvider extends BitcoinStateProvider {
    public RVNStateProvider(Network network) {
        super(network);
        super.params = RVNNetParams.get();
    }
}
