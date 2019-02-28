package org.bitcorej.core;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

public enum Network {
    MAIN, TEST, REGTEST;

    public NetworkParameters getNetworkParameters() {
        NetworkParameters parameters;
        switch (this) {
            case MAIN:
                parameters = MainNetParams.get();
                break;
            case TEST:
                parameters = TestNet3Params.get();
                break;
            case REGTEST:
                parameters = RegTestParams.get();
                break;
            default:
                parameters = MainNetParams.get();
        }
        return parameters;
    }
}
