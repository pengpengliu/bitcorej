package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

public class ExampleBHDWallet {
    public static void main(String[] args) throws Exception {
        ChainState bhd = new ChainStateProxy("bhd", "main");
        System.out.println(bhd.generateKeyPair());
    }
}