package org.bitcorej.chain.btcv;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcorej.chain.divi.DIVINetParameters;

public class BTCVNetParameters extends AbstractBitcoinNetParams {

    public BTCVNetParameters() {
        super();
        addressHeader = 78;
        p2shHeader = 60;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128;
    }

    private static BTCVNetParameters instance;
    public static synchronized BTCVNetParameters get() {
        if (instance == null) {
            instance = new BTCVNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}