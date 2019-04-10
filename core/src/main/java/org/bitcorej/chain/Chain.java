package org.bitcorej.chain;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcorej.chain.ltc.LitecoinNetParameters;
import org.bitcorej.core.Network;

public enum Chain {
    BTC, LTC;

    public NetworkParameters getNetworkParameters(Network network) {
        switch (this) {
            case BTC:
                if (network == Network.MAIN) {
                    return MainNetParams.get();
                }
            case LTC:
                if (network == Network.MAIN) {
                    return LitecoinNetParameters.get();
                }
            default:
                return TestNet3Params.get();
        }
    }
}
