package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

import java.util.Collections;
import java.util.List;

public class ExampleONTWallet {
    public static void main(String[] args) throws Exception {
        ChainState nas = new ChainStateProxy("ont");
        System.out.println(nas.generateKeyPair("5327cb705c73c0e5b57559dfd999bf6512ac13efede0d53682872265a17c9fc1"));

        List<String> keys = Collections.singletonList("5327cb705c73c0e5b57559dfd999bf6512ac13efede0d53682872265a17c9fc1");

        String signedTx = nas.signRawTransaction("{\"sender\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"recvAddr\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"amount\":3,\"payer\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"gasPrice\":500,\"gasLimit\":20000}", keys);
        System.out.println(signedTx);
    }
}
