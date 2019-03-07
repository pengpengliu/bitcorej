package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.*;
import org.bitcorej.utils.NumericUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class ExampleBitcoinWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        HDPrivateKey tprv = wallet.derivedKey("m/44'/1'/0'", Network.TEST);
        System.out.println(tprv);

        PrivateKey privKey = tprv.derived(0).derived(0).getPrivKey();
        PublicKey pubKey = privKey.toPublicKey();
        System.out.println("Private key is: " + privKey.toString());
        System.out.println("WIF is: " + privKey.toWIF());
        System.out.println("Public key is: " + pubKey.toString());

        ChainState btc = new ChainStateProxy("btc", "test");
        System.out.println("Address is: " + btc.createAddress(pubKey));

        String rawTxHex = "01000000017335aed204417492dadd3fdecabcd2f80c35cc374a47f4162a06f93176308aaf010000006a47304402204d666ca4c7234536b9aadfd0eb0ec3b2dca61acc2f57a6b0c9672661b760990602202ffe635d91effa77247e0d46212c84981be2bd654778929e7bd5736e3e4da60701210254dec37f0858dd993798f8b31ba912eb3cee803ac4209596cc79c804a2f3c201ffffffff0210270000000000001976a91415c5e0965754cd540c719aac3e52d36b2d9a815288ac28cfb500000000001976a91415c5e0965754cd540c719aac3e52d36b2d9a815288ac00000000";
        byte[] rawTxBytes = new BigInteger(rawTxHex, 16).toByteArray();
        ArrayList<PrivateKey> keys = new ArrayList<>();
        keys.add(privKey);
        byte[] signedTx = btc.signRawTransaction(rawTxBytes, keys);
        System.out.println(NumericUtil.bytesToHex(signedTx));

        // Create a 2-of-3 multisig address.
        String multisigAddress = btc.createAddress(
            Arrays.asList(
                tprv.derived(0).derived(0).getPubKey(),
                tprv.derived(0).derived(1).getPubKey(),
                tprv.derived(0).derived(2).getPubKey()
            )
        );
        System.out.println("2-of-3 multisig address: " + multisigAddress);
    }
}
