package org.bitcorej.chain.zcash;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class ZcashTestNetParams extends AbstractBitcoinNetParams {

    public ZcashTestNetParams() {
        super();
        addressHeader = 0x1d25;
        p2shHeader = 0x1cba;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0xef;
        bip32HeaderPub = 0x043587cf;
        bip32HeaderPriv = 0x04358394;
    }

    private static ZcashTestNetParams instance;
    public static synchronized ZcashTestNetParams get() {
        if (instance == null) {
            instance = new ZcashTestNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "test";
    }
}
