package org.bitcorej.chain.btcv;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class BTCVStateProvider extends BitcoinStateProvider {
    public BTCVStateProvider(Network network) {
        super(network);
        super.params = BTCVNetParameters.get();
    }
}
