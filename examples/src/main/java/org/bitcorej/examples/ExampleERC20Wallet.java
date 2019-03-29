package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

import java.util.ArrayList;

public class ExampleERC20Wallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey privKey = wallet.derivedKey("m/44'/60'/0'", Network.MAIN).derived(0).derived(0).getPrivKey();
        ChainState rnt = new ChainStateProxy("eth", "0x7da8470794fd52463956d8deed55edec9fa6c662", "18");

        System.out.println(rnt.generateKeyPair(privKey.toString()));
        String rawTxHex = "{\"nonce\":\"0x4b5\",\"gas\":\"0xEA60\",\"gasPrice\":\"0x3b9aca00\",\"from\":\"0x3ffc930c83848cbd72735e1d63bbff46a0d7a560\",\"to\":\"0x7da8470794fd52463956d8deed55edec9fa6c662\",\"value\":\"0x0\",\"data\":\"0xa9059cbb0000000000000000000000003ffc930c83848cbd72735e1d63bbff46a0d7a560000000000000000000000000000000000000000000000000016345785d8a0000\"}";

        String requestTx = "{\"from\":[{\"address\":\"0x3ffc930c83848cbd72735e1d63bbff46a0d7a560\"}],\"to\":[{\"address\":\"0x3ffc930c83848cbd72735e1d63bbff46a0d7a560\",\"amount\":\"0.1\"}]}";
        if (rnt.validateTx(rawTxHex, requestTx)) {
            ArrayList<String> keys = new ArrayList<>();
            keys.add(privKey.toString());
            String signedTx = rnt.signRawTransaction(rawTxHex, keys);
            System.out.println(signedTx);
        } else {
            System.out.println("Invalid transaction!");
        }
    }
}

