package org.bitcorej.chain.zen;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class ZENNetParams extends AbstractBitcoinNetParams {
    public ZENNetParams() {
        super();
        addressHeader = 0x2089;
        p2shHeader = 0x2096;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
    }

    private static ZENNetParams instance;
    public static synchronized ZENNetParams get() {
        if (instance == null) {
            instance = new ZENNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
