package org.bitcorej.chain.iq;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class IQNetParameters extends AbstractBitcoinNetParams {
    public IQNetParameters() {
        super();
        addressHeader = 58;
        p2shHeader = 16;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 204;
    }

    private static IQNetParameters instance;
    public static synchronized IQNetParameters get() {
        if (instance == null) {
            instance = new IQNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
