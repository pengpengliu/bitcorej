package org.bitcorej.chain.grs;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class GRSNetParameters extends AbstractBitcoinNetParams {
    public GRSNetParameters() {
        super();
        addressHeader = 36;
        p2shHeader = 5;
//        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128;
    }

    private static GRSNetParameters instance;
    public static synchronized GRSNetParameters get() {
        if (instance == null) {
            instance = new GRSNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}