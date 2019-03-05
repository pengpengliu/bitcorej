package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

public class ExampleEthereumWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey privKey = wallet.derivedKey("m/44'/60'/0'", Network.MAIN).derived(0).derived(0).getPrivKey();
        // 7e68e45bd102a84bdc401ecdd1759efffdfbbe88889a9c1051354689a1f2e8c1
        System.out.println(privKey);
        // 0x02a7554174e12b2b9fd9b2f83ac2ac7eb9c375cc38357fbf77deb277f37d2c625f
        System.out.println(privKey.toPublicKey());

        ChainState eth = new ChainStateProxy("eth", "main");
        System.out.println(eth.getAddress(privKey));
    }
}
