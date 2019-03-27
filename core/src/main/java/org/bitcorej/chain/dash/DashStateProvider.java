package org.bitcorej.chain.dash;

import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class DashStateProvider extends BitcoinStateProvider {

    public DashStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                super.params = DashNetParameters.get();
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = DashNetParameters.get();
                break;
        }

        super.network = network;
    }
}
