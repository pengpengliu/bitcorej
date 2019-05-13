package org.bitcorej.chain.qtum;

import org.bitcoinj.params.AbstractBitcoinNetParams;

public class QTUMTestNetParams extends AbstractBitcoinNetParams {

    public QTUMTestNetParams() {
        super();
        packetMagic = 0xf9beb4d9;
        addressHeader = 0x78;
        p2shHeader = 0x6e;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0xef;
        bip32HeaderPub = 0x043587cf;
        bip32HeaderPriv = 0x04358394;
    }

    private static QTUMTestNetParams instance;
    public static synchronized QTUMTestNetParams get() {
        if (instance == null) {
            instance = new QTUMTestNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "test";
    }
}