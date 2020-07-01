package org.bitcorej.chain.pmeer;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class PMEERNetParameters extends AbstractBitcoinNetParams {

    public PMEERNetParameters() {
        super();
        addressHeader = 0x0f0f;
        p2shHeader = 0x0f12;
        acceptableAddressCodes = new int[]{addressHeader, p2shHeader};
    }

    private static PMEERNetParameters instance;

    public static synchronized PMEERNetParameters get() {
        if (instance == null) {
            instance = new PMEERNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}