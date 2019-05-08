package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

import java.util.Collections;
import java.util.List;

public class ExampleDogecoinWallet {
    public static void main(String[] args) throws Exception {
        ChainState doge = new ChainStateProxy("doge", "test");
        System.out.println(doge.generateKeyPair("15ca31edea232edd1a168b5ac2e48fb7af5564d0e42921a28751edef5e6aab6c"));

        List<String> keys = Collections.singletonList("15ca31edea232edd1a168b5ac2e48fb7af5564d0e42921a28751edef5e6aab6c");

    }
}
