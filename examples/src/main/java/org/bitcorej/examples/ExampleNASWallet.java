package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

import java.util.Collections;
import java.util.List;

public class ExampleNASWallet {
    public static void main(String[] args) throws Exception {
        ChainState nas = new ChainStateProxy("nas");
        System.out.println(nas.generateKeyPair("5327cb705c73c0e5b57559dfd999bf6512ac13efede0d53682872265a17c9fc1"));

        List<String> keys = Collections.singletonList("5327cb705c73c0e5b57559dfd999bf6512ac13efede0d53682872265a17c9fc1");

        String signedTx = nas.signRawTransaction("{\"chainID\":\"1001\",\"from\":\"n1V8YrfSHuTPsqsnSaexjoypRatwFygaNGU\",\"to\":\"n1V8YrfSHuTPsqsnSaexjoypRatwFygaNGU\",\"nonce\":3,\"value\":\"20000000000000000000\",\"gasPrice\":\"1000000\",\"gasLimit\":\"200000\"}", keys);
        System.out.println(signedTx);
    }
}
