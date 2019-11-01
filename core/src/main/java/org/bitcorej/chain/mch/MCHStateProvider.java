package org.bitcorej.chain.mch;

import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.cent.CENTStateProvider;
import org.bitcorej.core.Network;


public class MCHStateProvider extends CENTStateProvider {

    public MCHStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(MCHNetParameters.get());
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = MCHNetParameters.get();
                break;
        }

        super.network = network;
    }

}
