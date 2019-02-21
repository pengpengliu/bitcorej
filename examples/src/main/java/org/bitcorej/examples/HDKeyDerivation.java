package org.bitcorej.examples;

import org.bitcorej.core.HDKeychain;
import org.bitcorej.core.HDPrivateKey;

public class HDKeyDerivation {
    public static void main(String[] args) throws Exception {
        HDKeychain keychain = new HDKeychain("say tongue select oil blossom pond parent orphan crater sadness position coin");
        HDPrivateKey xpriv = keychain.derivedKey("m/44'/60'/0'");
        System.out.println(xpriv.toString());
        System.out.println(xpriv.derived(0).derived(0).getPrivKey().toString(16));
    }
}
