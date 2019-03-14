package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

public class ExampleRippleWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey privKey = wallet.derivedKey("m/44'/144'/0'/0/0", Network.MAIN).getPrivKey();
        System.out.println(privKey.toString());
        System.out.println(privKey.toPublicKey().toString());

        ChainState xrp = new ChainStateProxy("xrp");
        System.out.println("Ripple address is: " + xrp.createAddress(privKey.toPublicKey()));
    }
}
