package org.bitcorej.chain.dogecoin;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class DogeTestNetParams extends AbstractBitcoinNetParams {

    public DogeTestNetParams() {
        super();
        addressHeader = 0x71;
        p2shHeader = 0xc4;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0xf1;
        bip32HeaderPub = 0x043587cf;
        bip32HeaderPriv = 0x04358394;
    }

    private static DogeTestNetParams instance;
    public static synchronized DogeTestNetParams get() {
        if (instance == null) {
            instance = new DogeTestNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "test";
    }

}