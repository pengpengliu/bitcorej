package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDPrivateKey;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.PrivateKey;

public class SampleBitconWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        // xprv9ysq3soFprH9bhJd4QXRBwtmgAXbyaeNqVZYaoCASE9GGNE8YBniWZWib6WLKeB5fGDsdn5occgCEdugmrndnayqktnuNAGEFLpZzG27sys
        // xpub6CsBTPL9fDqSpBP6AS4RZ5qWECN6P3NECiV9PBbmzZgF9AZH5j6y4MqCSPeKFcn6yXng1jdB18tL5rTAZibPZzBC7afvYG4rkBdpn2JjdGw
        HDPrivateKey xprv = wallet.derivedKey("m/44'/0'/0'");
        System.out.println("Account Extended Private Key is: " + xprv);

        PrivateKey privKey = xprv.derived(0).derived(0).getPrivKey();
        // KyqYNjoGNVdS5osHUiApLB4dwBxRTZs97WQch21jTpCyNDzisqWJ
        System.out.println("Private key is: " + privKey);
        // 024eda7ec4a20a5c24902063b85d60a53d52c4fcc8362594b5f258a4cc5458bc4f
        System.out.println("Public key is: " + privKey.toPublicKey());

        ChainState btc = new ChainStateProxy("btc");
        // 1DJwaDEZTYkW2eASD4wX3vv46QHs4LcAbf
        System.out.println("Address is: " + btc.getAddress(privKey));
    }
}
