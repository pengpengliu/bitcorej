package org.bitcorej.chain.iq;

import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.cent.CENTNetParameters;
import org.bitcorej.core.Network;

public class IQStateProvider extends BitcoinStateProvider {
    public IQStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(IQNetParameters.get());
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = IQNetParameters.get();
                break;
        }
        super.network = network;
    }
}
