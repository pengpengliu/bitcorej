package org.bitcorej.chain.nrg;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class NRGNetParameters extends AbstractBitcoinNetParams {
    public NRGNetParameters() {
        super();
        addressHeader = 33;
        p2shHeader = 53;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
    }

    private static NRGNetParameters instance;
    public static synchronized NRGNetParameters get() {
        if (instance == null) {
            instance = new NRGNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
