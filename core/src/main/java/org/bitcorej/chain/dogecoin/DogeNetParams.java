package org.bitcorej.chain.dogecoin;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class DogeNetParams extends AbstractBitcoinNetParams {

    public DogeNetParams() {
        super();
        addressHeader = 0x1e;
        p2shHeader = 0x16;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0x9e;
        bip32HeaderPub = 0x02facafd;
        bip32HeaderPriv = 0x02fac398;
    }

    private static DogeNetParams instance;
    public static synchronized DogeNetParams get() {
        if (instance == null) {
            instance = new DogeNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}