package org.bitcorej.chain.vrsc;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class VRSCNetParameters extends AbstractBitcoinNetParams {

    public VRSCNetParameters() {
        super();
        addressHeader = 60;
        p2shHeader = 85;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 188;
    }

    private static VRSCNetParameters instance;
    public static synchronized VRSCNetParameters get() {
        if (instance == null) {
            instance = new VRSCNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}