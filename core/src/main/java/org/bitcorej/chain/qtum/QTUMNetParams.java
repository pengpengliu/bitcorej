package org.bitcorej.chain.qtum;


import org.bitcoinj.params.AbstractBitcoinNetParams;

public class QTUMNetParams extends AbstractBitcoinNetParams {

    public QTUMNetParams() {
        super();
        packetMagic = 0xf9beb4d9;
        addressHeader = 0x3a;
        p2shHeader = 0x32;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 0x80;
        bip32HeaderPub = 0x0488b21e;
        bip32HeaderPriv = 0x0488ade4;
    }

    private static QTUMNetParams instance;
    public static synchronized QTUMNetParams get() {
        if (instance == null) {
            instance = new QTUMNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }
}
