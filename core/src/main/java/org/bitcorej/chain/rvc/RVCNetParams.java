package org.bitcorej.chain.rvc;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class RVCNetParams extends AbstractBitcoinNetParams {

    public RVCNetParams() {
        super();
        addressHeader = 60;
        p2shHeader = 122;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128;
    }

    private static RVCNetParams instance;
    public static synchronized RVCNetParams get() {
        if (instance == null) {
            instance = new RVCNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }

    @Override
    public boolean hasMaxMoney() {
        return false;
    }
}

