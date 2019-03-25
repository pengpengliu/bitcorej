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
        String rawTxHex = "{\"from\":\"0x3ffc930c83848cbd72735e1d63bbff46a0d7a560\",\"to\":\"0x3ffc930c83848cbd72735e1d63bbff46a0d7a560\",\"value\":\"0x16345785d8a0000\",\"gas\":\"0x5208\",\"gasPrice\":\"0x3b9aca00\",\"nonce\":\"0x3\",\"data\":\"0x\"}";
        ArrayList<String> keys = new ArrayList<>();
        keys.add(privKey.toString());
        String signedTx = eth.signRawTransaction(rawTxHex, keys);
        System.out.println(signedTx);
    }
}
