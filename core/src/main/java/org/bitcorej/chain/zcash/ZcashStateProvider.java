package org.bitcorej.chain.zcash;

import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class ZcashStateProvider extends BitcoinStateProvider {

    public ZcashStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                super.params = ZcashNetParams.get();
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = ZcashNetParams.get();
                break;
        }

        super.network = network;
    }
}