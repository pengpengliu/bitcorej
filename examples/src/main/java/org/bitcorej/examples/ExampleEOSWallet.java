package org.bitcorej.examples;

import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

public class ExampleEOSWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey ownerKey = wallet.derivedKey("m / 48' / 4' / 0' / 0' / 0'", Network.TEST).getPrivKey();
        PrivateKey activeKey = wallet.derivedKey("m / 48' / 4' / 1' / 0' / 0'", Network.TEST).getPrivKey();
        System.out.println(ownerKey.toString());
        System.out.println(activeKey.toString());
    }
}