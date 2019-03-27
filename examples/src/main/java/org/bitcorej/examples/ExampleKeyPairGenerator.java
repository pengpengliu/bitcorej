package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;


public class ExampleKeyPairGenerator {
    public static void main(String[] args) throws Exception {
        ChainState btc = new ChainStateProxy("btc", "test");
        System.out.println(btc.generateKeyPair());

        ChainState eth = new ChainStateProxy("eth");
        System.out.println(eth.generateKeyPair());

        ChainState eos = new ChainStateProxy("eos", "test");
        System.out.println(eos.generateKeyPair());

        ChainState bch = new ChainStateProxy("bch", "test");
        System.out.println(bch.generateKeyPair());

        ChainState bsv = new ChainStateProxy("bsv", "test");
        System.out.println(bsv.generateKeyPair());

        ChainState usdt = new ChainStateProxy("usdt", "test");
        System.out.println(usdt.generateKeyPair());

        ChainState xlm = new ChainStateProxy("xlm", "test");
        System.out.println(xlm.generateKeyPair());

        ChainState ltc = new ChainStateProxy("ltc", "main");
        System.out.println(ltc.generateKeyPair());

        ChainState dash = new ChainStateProxy("dash", "main");
        System.out.println(dash.generateKeyPair());

//        ChainState zec = new ChainStateProxy("zec", "main");
//        System.out.println(zec.generateKeyPair());

        ChainState doge = new ChainStateProxy("doge", "main");
        System.out.println(doge.generateKeyPair());

        ChainState qtum = new ChainStateProxy("qtum", "main");
        System.out.println(qtum.generateKeyPair());
    }
}
