package org.bitcorej.chain.ltc;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class LitecoinNetParameters extends AbstractBitcoinNetParams {

    public LitecoinNetParameters() {
        super();
        id = "org.litecoin.production";
        packetMagic = 0xFBC0B6DBL;
        addressHeader = 48;
        p2shHeader = 0x32;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0xb0;
        bip32HeaderPub = 0x0488B21E;
        bip32HeaderPriv = 0x0488ADE4;
    }

    private static LitecoinNetParameters instance;
    public static synchronized LitecoinNetParameters get() {
        if (instance == null) {
            instance = new LitecoinNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "litecoin main";
    }
}
