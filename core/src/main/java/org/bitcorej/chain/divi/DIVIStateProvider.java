package org.bitcorej.chain.divi;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class DIVIStateProvider extends BitcoinStateProvider {
    public DIVIStateProvider(Network network) {
        super(network);
        super.params = DIVINetParameters.get();
    }
}
