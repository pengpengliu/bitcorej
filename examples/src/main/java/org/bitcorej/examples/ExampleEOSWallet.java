package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

import java.util.ArrayList;

public class ExampleEOSWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey ownerKey = wallet.derivedKey("m / 48' / 4' / 0' / 0' / 0'", Network.TEST).getPrivKey();
        PrivateKey activeKey = wallet.derivedKey("m / 48' / 4' / 1' / 0' / 0'", Network.TEST).getPrivKey();
        System.out.println(ownerKey.toString());
        System.out.println(activeKey.toString());

        ChainState eos = new ChainStateProxy("eos", "test");
        String rawTx = "{\"expiration\":\"2019-03-14T15:20:30.500\",\"ref_block_num\":18149908,\"ref_block_prefix\":2589238659,\"max_net_usage_words\":0,\"max_cpu_usage_ms\":0,\"delay_sec\":0,\"context_free_actions\":[],\"actions\":[{\"account\":\"eosio.token\",\"name\":\"transfer\",\"authorization\":[{\"actor\":\"teos4oneroot\",\"permission\":\"active\"}],\"data\":\"9029bd6a5282a9ca1042888667dab64a102700000000000004454f530000000006303030303036\"}],\"transaction_extensions\":[],\"signatures\":[\"SIG_K1_Khn918pY1NHmnbF41bsqFE7sPYrniZPtTns68qUo3m92jp6gbegkpRHYSp9RH95T3u82XUvjZLM33AP83ZqiGApBo7JnBF\"],\"context_free_data\":[]}";
        ArrayList<String> keys = new ArrayList<>();
        keys.add(activeKey.toString());
        String signedTx = eos.signRawTransaction(rawTx, keys);
        System.out.println(signedTx);
    }
}