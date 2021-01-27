package org.bitcorej.chain.sprk;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class SPRKNetParams extends AbstractBitcoinNetParams {

    public SPRKNetParams() {
        super();
        addressHeader = 0x3f;
        p2shHeader = 0x7d;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0xbf;
        bip32HeaderPub = 0x0488B21E;
        bip32HeaderPriv = 0x0488ADE4;
    }

    private static SPRKNetParams instance;
    public static synchronized SPRKNetParams get() {
        if (instance == null) {
            instance = new SPRKNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
