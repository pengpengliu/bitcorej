package org.bitcorej.chain.zcash;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class ZcashNetParams extends AbstractBitcoinNetParams {

    public ZcashNetParams() {
        super();
        addressHeader = 0x1CB8;
        p2shHeader = 0x1CBD;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0x80;
        bip32HeaderPub = 0x0488B21E;
        bip32HeaderPriv = 0x0488ADE4;
    }

    private static ZcashNetParams instance;
    public static synchronized ZcashNetParams get() {
        if (instance == null) {
            instance = new ZcashNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}