package org.bitcorej.examples;

import org.bitcorej.core.Mnemonic;

public class RandomMnemonic {

    public static void main(String[] args) {
        System.out.println(Mnemonic.generateMnemonic());
    }
}
