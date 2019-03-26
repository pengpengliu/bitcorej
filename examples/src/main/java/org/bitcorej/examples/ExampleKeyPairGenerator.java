package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;


public class ExampleKeyPairGenerator {
    public static void main(String[] args) throws Exception {
        ChainState btc = new ChainStateProxy("btc", "test");
        System.out.println(btc.generateKeyPair());

        ChainState eth = new ChainStateProxy("eth");
        System.out.println(eth.generateKeyPair());

        ChainState eos = new ChainStateProxy("eos", "main");
        System.out.println(eos.generateKeyPair());
    }
}
