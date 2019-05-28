package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

import java.util.Collections;
import java.util.List;

public class ExampleONTWallet {
    public static void main(String[] args) throws Exception {
        ChainState ont = new ChainStateProxy("ont");
        System.out.println(ont.generateKeyPair());

        List<String> keys = Collections.singletonList("5327cb705c73c0e5b57559dfd999bf6512ac13efede0d53682872265a17c9fc1");

        String signedTx;
        signedTx = ont.signRawTransaction("{\"sender\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"recvAddr\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"amount\":3,\"payer\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"gasPrice\":500,\"gasLimit\":20000}", keys);
        System.out.println(signedTx);

        ChainState ong = new ChainStateProxy("ong");

        signedTx = ong.signRawTransaction("{\"sender\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"recvAddr\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"amount\":1000000000,\"payer\":\"AVrADswcAbdUTLBcYjTLrGenNZxMiXWkKy\",\"gasPrice\":500,\"gasLimit\":20000}", keys);
        System.out.println(signedTx);
    }
}
