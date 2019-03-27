package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

public class ExampleStellarWallet {
    public static void main(String[] args) throws Exception {
        ChainState xlm = new ChainStateProxy("xlm");
        // GDU523D2BWA36LXQSKGYDAIIKCM3ZBMEWOB7P2UJZQRMY43WWQFRXJXN
        // SC6PQUBPWO6BKJJKZAAJARUENJKLTCNGG6CF4UO2OVKNBF5ZK37BNM2U
        System.out.println(xlm.generateKeyPair("SC6PQUBPWO6BKJJKZAAJARUENJKLTCNGG6CF4UO2OVKNBF5ZK37BNM2U"));

        String rawTxHex = "{\"from\":\"0x3ffc930c83848cbd72735e1d63bbff46a0d7a560\",\"to\":\"0x3ffc930c83848cbd72735e1d63bbff46a0d7a560\",\"value\":\"0x16345785d8a0000\",\"gas\":\"0x5208\",\"gasPrice\":\"0x3b9aca00\",\"nonce\":\"0x3\",\"data\":\"0x\"}";
        String requestTx = "{\"from\":[{\"address\":\"rUZ2jxaykFPpW3e3yXoHT5fnMyu5T7pZAY\"}],\"to\":[{\"amount\":\"0.1\",\"memo\":\"000006\",\"address\":\"rMvQZnjKXqJaEap1wqgBu8CwKRfY3nQZ3p\"}]}";

    }
}
