package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.utils.NumericUtil;

import java.math.BigInteger;
import java.util.ArrayList;

public class ExampleEthereumWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey privKey = wallet.derivedKey("m/44'/60'/0'", Network.MAIN).derived(0).derived(0).getPrivKey();
        // 7e68e45bd102a84bdc401ecdd1759efffdfbbe88889a9c1051354689a1f2e8c1
        System.out.println(privKey);
        // 0x02a7554174e12b2b9fd9b2f83ac2ac7eb9c375cc38357fbf77deb277f37d2c625f
        System.out.println(privKey.toPublicKey());

        ChainState eth = new ChainStateProxy("eth");
        System.out.println(eth.createAddress(privKey));
        String rawTxHex = "f86a8204b3843b9aca0082ea609453e7e00ffb9258cc52f331a4198d2e8f28b5711680b844a9059cbb0000000000000000000000003ffc930c83848cbd72735e1d63bbff46a0d7a56000000000000000000000000000000000000000000000000000000000000000641c8080";
        byte[] rawTxBytes = new BigInteger(rawTxHex, 16).toByteArray();
        ArrayList<PrivateKey> keys = new ArrayList<>();
        keys.add(privKey);
        byte[] signedTx = eth.signRawTransaction(rawTxBytes, keys);
        System.out.println(NumericUtil.bytesToHex(signedTx));
    }
}
