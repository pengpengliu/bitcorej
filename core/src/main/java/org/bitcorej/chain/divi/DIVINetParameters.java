package org.bitcorej.chain.divi;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcorej.chain.dash.DashNetParameters;

public class DIVINetParameters extends AbstractBitcoinNetParams {

    public DIVINetParameters() {
        super();
        addressHeader = 30;
        p2shHeader = 13;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 212;
    }

    private static DIVINetParameters instance;
    public static synchronized DIVINetParameters get() {
        if (instance == null) {
            instance = new DIVINetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
