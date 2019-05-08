package org.bitcorej.chain.dogecoin;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class DogecoinStateProvider extends BitcoinStateProvider {

    public DogecoinStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(DogeNetParams.get());
                break;
            case TEST:
                super.params = DogeTestNetParams.get();
                break;
            default:
                super.params = DogeNetParams.get();
                break;
        }

        super.network = network;
    }
}
