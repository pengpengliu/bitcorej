package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.chain.eos.EOSKey;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

import java.util.ArrayList;

public class ExampleEOSWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        // 5Jh9Y5MmknurQYSiLP4zeoYRjK37gBQhWkKrK9gidcSFkrkUmVQ
        PrivateKey ownerKey = wallet.derivedKey("m / 48' / 4' / 0' / 0' / 0'", Network.TEST).getPrivKey();
        // 5KUZpcfAEtKMmZxQZbCp3HYJA29py744AbzKynWoXeKf2anQGFy
        PrivateKey activeKey = wallet.derivedKey("m / 48' / 4' / 1' / 0' / 0'", Network.TEST).getPrivKey();
        System.out.println(ownerKey.toString());
        System.out.println(activeKey.toString());

        ChainState eos = new ChainStateProxy("eos", "test");

        String rawTxHex = "{\"expiration\":\"2019-03-26T07:42:53\",\"ref_block_num\":20309199,\"ref_block_prefix\":2040915149,\"max_net_usage_words\":0,\"max_cpu_usage_ms\":0,\"delay_sec\":0,\"context_free_actions\":[],\"actions\":[{\"code\":\"eosio.token\",\"action\":\"transfer\",\"args\":{\"from\":\"teos4oneroot\",\"to\":\"devhotwal111\",\"quantity\":\"1.0000 EOS\",\"memo\":\"000006\"}}],\"transaction_extensions\":[],\"signatures\":[],\"context_free_data\":[]}";
        String requestTx = "{\"from\":[{\"address\":\"teos4oneroot\"}],\"to\":[{\"address\":\"devhotwal111\",\"amount\":\"1\",\"memo\":\"000006\"}]}";
        if (eos.validateTx(rawTxHex, requestTx)) {
            ArrayList<String> keys = new ArrayList<>();
            keys.add("5KUZpcfAEtKMmZxQZbCp3HYJA29py744AbzKynWoXeKf2anQGFy");
            String signedTx = eos.signRawTransaction(rawTxHex, keys);
            System.out.println(signedTx);
        } else {
            System.out.println("Invalid transaction!");
        }
    }
}