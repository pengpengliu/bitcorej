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
        String rawTx = "{\"TransactionType\":\"Payment\",\"Account\":\"rJY8zf7CcDqJ8TugRexpuQyYXiV9dacgPE\",\"Destination\":\"rNyRhGrndd5qjJa3nE9SsixP7F9e9h62qH\",\"Amount\":{\"currency\":\"XRP\",\"value\":\"100000000\",\"issuer\":\"rJY8zf7CcDqJ8TugRexpuQyYXiV9dacgPE\"},\"Fee\":\"12\",\"Sequence\":1,\"DestinationTag\":\"213312\",\"LastLedgerSequence\":\"13561714\"}";
        ArrayList<String> keys = new ArrayList<>();
        keys.add(key);
        String signedTx = xrp.signRawTransaction(rawTx, keys);
        System.out.println(signedTx);
    }
}
