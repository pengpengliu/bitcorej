package org.bitcorej.chain.dash;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class DashNetParameters extends AbstractBitcoinNetParams {

    public DashNetParameters() {
        super();
        id = "org.bitcoin.production";
        packetMagic = 0xbf0c6bbd;
        addressHeader = 76;
        p2shHeader = 16;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 204;
        bip32HeaderPub = 0x0488B21E;
        bip32HeaderPriv = 0x0488ADE4;
    }

    private static DashNetParameters instance;
    public static synchronized DashNetParameters get() {
        if (instance == null) {
            instance = new DashNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
