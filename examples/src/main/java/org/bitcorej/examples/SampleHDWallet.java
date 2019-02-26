package org.bitcorej.examples;

import org.bitcorej.core.HDPrivateKey;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

public class SampleHDWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet();

        HDPrivateKey xpriv = wallet.derivedKey("m/44'/0'/0'");
        System.out.println("Account extended private Key is: " + xpriv);

        PrivateKey privKey = wallet.derivedKey("m/44'/0'/0'/0/0").getPrivKey();
        System.out.println("Derived private key is: " + privKey);

        PublicKey pubKey = wallet.derivedKey("m/44'/0'/0'/0/0").getPubKey();
        System.out.println("Public key from private key is: " + privKey.toPublicKey());
        System.out.println("Public key is: " + pubKey);
    }
}
