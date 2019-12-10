package org.bitcorej.chain.nrg;

import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class NRGStateProvider extends BitcoinStateProvider {
    public NRGStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(NRGNetParameters.get());
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = NRGNetParameters.get();
                break;
        }

        super.network = network;
    }
}
