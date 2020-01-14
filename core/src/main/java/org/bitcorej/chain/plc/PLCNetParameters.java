package org.bitcorej.chain.plc;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class PLCNetParameters extends AbstractBitcoinNetParams {

    public PLCNetParameters() {
        super();
        id = "plc";
        addressHeader = 0x02D0A8;
        p2shHeader = 0x02D0A9;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0x02D0B0;
    }

    private static PLCNetParameters instance;
    public static synchronized PLCNetParameters get() {
        if (instance == null) {
            instance = new PLCNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "plc main";
    }
}
