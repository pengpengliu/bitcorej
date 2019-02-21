package org.bitcorej.core;

import org.web3j.crypto.MnemonicUtils;

import java.security.SecureRandom;


public class Mnemonic {

    public static String generateMnemonic() {
        byte[] entropy = new byte[16];
        new SecureRandom().nextBytes(entropy);
        return MnemonicUtils.generateMnemonic(entropy);
    }

}