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

        String rawTx = "{\"expiration\":\"2019-03-22T03:11:44\",\"ref_block_num\":19594346,\"ref_block_prefix\":1109991391,\"max_net_usage_words\":0,\"max_cpu_usage_ms\":0,\"delay_sec\":0,\"context_free_actions\":[],\"actions\":[{\"account\":\"eosio.token\",\"name\":\"transfer\",\"authorization\":[{\"actor\":\"teos4oneroot\",\"permission\":\"active\"}],\"data\":\"9029bd6a5282a9ca1042888667dab64a102700000000000004454f530000000006303030303036\"}],\"transaction_extensions\":[],\"signatures\":[],\"context_free_data\":[]}";
        ArrayList<String> keys = new ArrayList<>();
        keys.add("5KUZpcfAEtKMmZxQZbCp3HYJA29py744AbzKynWoXeKf2anQGFy");
        String signedTx = eos.signRawTransaction(rawTx, keys);
        System.out.println(signedTx);

        // SIG_K1_KcRph4n4CNnTFWqdZK7WmmJENfE3SUDEscLQaA7mrFYiRJYuwszpEYwx55ZCsHRtip6t2bfxqJa1faM59kWPGrc7XNik2w
        // SIG_K1_KciNeaMRn7GzNhVTL8PfW9xJQ37o1sBWUAQGpMD3tTJKQSpGhCpk2kzssRSMmemc7nth1wfVMczXySNjTMmArgH8XhaUNJ
    }
}