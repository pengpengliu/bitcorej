package org.bitcorej.chain.ltc;

import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class LitecoinStateProvider extends BitcoinStateProvider {

    public LitecoinStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                super.params = LitecoinNetParameters.get();
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = LitecoinNetParameters.get();
                break;
        }

        super.network = network;
    }
}
