package org.bitcorej.chain.rvn;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcorej.chain.qtum.QTUMNetParams;

public class RVNNetParams extends AbstractBitcoinNetParams {

    public RVNNetParams() {
        super();
        addressHeader = 0x3c;
        p2shHeader = 0x7a;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0x80;
    }

    private static RVNNetParams instance;
    public static synchronized RVNNetParams get() {
        if (instance == null) {
            instance = new RVNNetParams();
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
