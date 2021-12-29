package org.bitcorej.chain.gleec2;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcorej.chain.zcash.ZcashNetParams;

public class GLEEC2NetParams extends AbstractBitcoinNetParams {

    public GLEEC2NetParams() {
        super();
        addressHeader = 0x3c;
        p2shHeader = 0x55;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
    }

    private static GLEEC2NetParams instance;
    public static synchronized GLEEC2NetParams get() {
        if (instance == null) {
            instance = new GLEEC2NetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
