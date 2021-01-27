package org.bitcorej.chain.gleec;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class GLEECNetParams extends AbstractBitcoinNetParams {

    public GLEECNetParams() {
        super();
        addressHeader = 35;
        p2shHeader = 38;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 65;
        bip32HeaderPub = 0x0488B21E;
        bip32HeaderPriv = 0x0488ADE4;
    }

    private static GLEECNetParams instance;
    public static synchronized GLEECNetParams get() {
        if (instance == null) {
            instance = new GLEECNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
