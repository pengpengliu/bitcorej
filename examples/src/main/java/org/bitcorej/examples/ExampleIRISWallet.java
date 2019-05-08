package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

import java.util.Collections;
import java.util.List;

public class ExampleIRISWallet {
    public static void main(String[] args) throws Exception {
        ChainState iris = new ChainStateProxy("iris");
        System.out.println(iris.generateKeyPair("93618AC7165AA8D4735179A60525819DEB0DD356E9C136FAD91E5B90394FB4DA"));

        List<String> keys = Collections.singletonList("15ca31edea232edd1a168b5ac2e48fb7af5564d0e42921a28751edef5e6aab6c");
    }
}
