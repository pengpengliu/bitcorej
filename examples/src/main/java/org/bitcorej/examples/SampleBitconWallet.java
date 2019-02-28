package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.*;

public class SampleBitconWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        HDPrivateKey xprv = wallet.derivedKey("m/44'/0'/0'", Network.MAIN);
        HDPrivateKey tprv = wallet.derivedKey("m/44'/1'/0'", Network.TEST);
        System.out.println(xprv);
        System.out.println(tprv);

        PrivateKey privKey = xprv.derived(0).derived(0).getPrivKey();
        PublicKey pubKey = privKey.toPublicKey();
        System.out.println(privKey.toWIF());
        System.out.println(pubKey);

        ChainState btc = new ChainStateProxy("btc", "regtest");
        System.out.println("Address is: " + btc.getAddress(pubKey));
    }
}
