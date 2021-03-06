package org.bitcorej.chain.cent;

import org.bitcoinj.core.Coin;
import org.bitcoinj.params.AbstractBitcoinNetParams;

import static org.bitcoinj.core.Coin.COIN;

public class CENTNetParameters extends AbstractBitcoinNetParams {

    public CENTNetParameters() {
        super();
        addressHeader = 88;
        p2shHeader = 5;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 204;
        bip32HeaderPub = 0x0488B21E;
        bip32HeaderPriv = 0x0488ADE4;
    }

    private static CENTNetParameters instance;
    public static synchronized CENTNetParameters get() {
        if (instance == null) {
            instance = new CENTNetParameters();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "main";
    }

    @Override
    public Coin getMaxMoney() {
        return COIN.multiply(5000000000l);
    }
}

