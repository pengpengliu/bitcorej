package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

import java.util.ArrayList;

public class ExampleStellarWallet {
    public static void main(String[] args) throws Exception {
        ChainState xlm = new ChainStateProxy("xlm", "test");
        // GDU523D2BWA36LXQSKGYDAIIKCM3ZBMEWOB7P2UJZQRMY43WWQFRXJXN
        // SC6PQUBPWO6BKJJKZAAJARUENJKLTCNGG6CF4UO2OVKNBF5ZK37BNM2U
        System.out.println(xlm.generateKeyPair("SC6PQUBPWO6BKJJKZAAJARUENJKLTCNGG6CF4UO2OVKNBF5ZK37BNM2U"));

        String key = "SC6PQUBPWO6BKJJKZAAJARUENJKLTCNGG6CF4UO2OVKNBF5ZK37BNM2U";
        String rawTxHex = "{\"source\":\"GDU523D2BWA36LXQSKGYDAIIKCM3ZBMEWOB7P2UJZQRMY43WWQFRXJXN\",\"to\":\"GBQ7R6X3AJQZALLEXCH6UX3UKOX733P2VGTXDJQLM5ULYXLLJHS4RLZY\",\"amount\":\"0.1\",\"memo\":\"000006\",\"asset\":\"XLM\",\"fee\":\"100\",\"sequence\":\"1951542944989186\",\"SingerList\":[\"GDU523D2BWA36LXQSKGYDAIIKCM3ZBMEWOB7P2UJZQRMY43WWQFRXJXN\"]}";
        String requestTx = "{\"from\":[{\"address\":\"GDU523D2BWA36LXQSKGYDAIIKCM3ZBMEWOB7P2UJZQRMY43WWQFRXJXN\"}],\"to\":[{\"amount\":\"0.1\",\"memo\":\"000006\",\"address\":\"GBQ7R6X3AJQZALLEXCH6UX3UKOX733P2VGTXDJQLM5ULYXLLJHS4RLZY\"}]}";

        if (xlm.validateTx(rawTxHex, requestTx)) {
            ArrayList<String> keys = new ArrayList<>();
            keys.add(key);
            String signedTx = xlm.signRawTransaction(rawTxHex, keys);
            System.out.println(signedTx);
        } else {
            System.out.println("Invalid transaction!");
        }
    }
}
