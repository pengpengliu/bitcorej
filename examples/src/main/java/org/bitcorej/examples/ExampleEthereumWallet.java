package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.utils.NumericUtil;

import java.util.ArrayList;

public class ExampleEthereumWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey privKey = wallet.derivedKey("m/44'/60'/0'", Network.MAIN).derived(0).derived(0).getPrivKey();
        ChainState eth = new ChainStateProxy("eth");

        System.out.println(eth.generateKeyPair(privKey.toString()));
        String rawTxHex = "f86a8204b3843b9aca0082ea609453e7e00ffb9258cc52f331a4198d2e8f28b5711680b844a9059cbb0000000000000000000000003ffc930c83848cbd72735e1d63bbff46a0d7a56000000000000000000000000000000000000000000000000000000000000000641c8080";
        ArrayList<String> keys = new ArrayList<>();
        keys.add(privKey.toString());
        String signedTx = eth.signRawTransaction(rawTxHex, keys);
        System.out.println(signedTx);
    }
}
