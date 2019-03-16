package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.*;
import org.bitcorej.utils.NumericUtil;

import java.math.BigInteger;
import java.util.ArrayList;

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

        // 01000000017335aed204417492dadd3fdecabcd2f80c35cc374a47f4162a06f93176308aaf010000006a47304402204d666ca4c7234536b9aadfd0eb0ec3b2dca61acc2f57a6b0c9672661b760990602202ffe635d91effa77247e0d46212c84981be2bd654778929e7bd5736e3e4da60701210254dec37f0858dd993798f8b31ba912eb3cee803ac4209596cc79c804a2f3c201ffffffff0210270000000000001976a91415c5e0965754cd540c719aac3e52d36b2d9a815288ac28cfb500000000001976a91415c5e0965754cd540c719aac3e52d36b2d9a815288ac00000000
        // 0100000001a80cd17dff8f78911d0c4ade3752ce7e130beff0d3af57fd826e7c9ae872d31a010000006b483045022100c38fce706f7a94f578cac84f64090402b8edc35edeb8319c886fc19b433931c002206256a81b2beabb2bbbc4f3074e6872d28a67b3b565584ff56fafbbac1c7ed61e01210254dec37f0858dd993798f8b31ba912eb3cee803ac4209596cc79c804a2f3c201ffffffff0110270000000000001976a91415c5e0965754cd540c719aac3e52d36b2d9a815288ac00000000
        String rawTxHex = "010000000186e5fd21ec81bff6dae076274c3ed1809b106c4b98c466a3505dff9ce52f249e0100000000ffffffff0210270000000000001976a91415c5e0965754cd540c719aac3e52d36b2d9a815288ac6ae19900000000001976a91415c5e0965754cd540c719aac3e52d36b2d9a815288ac00000000";
        byte[] rawTxBytes = new BigInteger(rawTxHex, 16).toByteArray();
        ArrayList<String> keys = new ArrayList<>();
        keys.add(privKey.toString());
        byte[] signedTx = btc.signRawTransaction(rawTxBytes, keys);
        System.out.println(NumericUtil.bytesToHex(signedTx));
    }
}
