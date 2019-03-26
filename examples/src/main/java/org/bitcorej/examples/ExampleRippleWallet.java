package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

import java.util.ArrayList;

public class ExampleRippleWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey privKey = wallet.derivedKey("m/44'/144'/0'/0/0", Network.MAIN).getPrivKey();
        System.out.println(privKey.toString());
        System.out.println(privKey.toPublicKey().toString());

        ChainState xrp = new ChainStateProxy("xrp");
        String key = "snan38LUUj4cB6sABLVyomkojWHQE"; //rUZ2jxaykFPpW3e3yXoHT5fnMyu5T7pZAY
        // rKcSBF74JTJjR2CKxuvHpa2F7YEL9dV9RB sstByu7xVfvPu6Wt2cPmJNWaw1XK4
        String rawTxHex = "{\"LastLedgerSequence\":18088901,\"TransactionType\":\"Payment\",\"Account\":\"rUZ2jxaykFPpW3e3yXoHT5fnMyu5T7pZAY\",\"Destination\":\"rMvQZnjKXqJaEap1wqgBu8CwKRfY3nQZ3p\",\"Amount\":{\"currency\":\"XRP\",\"value\":\"0.1\",\"issuer\":\"rUZ2jxaykFPpW3e3yXoHT5fnMyu5T7pZAY\"},\"Fee\":\"12\",\"Sequence\":2,\"DestinationTag\":\"000006\",\"SingerList\":[\"rUZ2jxaykFPpW3e3yXoHT5fnMyu5T7pZAY\"]}";
        String requestTx = "{\"from\":[{\"address\":\"rUZ2jxaykFPpW3e3yXoHT5fnMyu5T7pZAY\"}],\"to\":[{\"amount\":\"0.1\",\"memo\":\"000006\",\"address\":\"rMvQZnjKXqJaEap1wqgBu8CwKRfY3nQZ3p\"}]}";

        if (xrp.validateTx(rawTxHex, requestTx)) {
            ArrayList<String> keys = new ArrayList<>();
            keys.add(key);
            String signedTx = xrp.signRawTransaction(rawTxHex, keys);
            System.out.println(signedTx);
        } else {
            System.out.println("Invalid transaction!");
        }
    }
}
