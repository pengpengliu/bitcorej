package org.bitcorej.chain.mch;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class MCHNetParameters extends AbstractBitcoinNetParams {

    public MCHNetParameters() {
        super();
        id = "mch";
        addressHeader = 50;
        p2shHeader = 110;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 178;
    }

    private static MCHNetParameters instance;
    public static synchronized MCHNetParameters get() {
        if (instance == null) {
            instance = new MCHNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "mch main";
    }
}