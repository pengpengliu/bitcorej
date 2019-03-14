package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.core.HDWallet;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;

public class ExampleEOSWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        PrivateKey ownerKey = wallet.derivedKey("m / 48' / 4' / 0' / 0' / 0'", Network.TEST).getPrivKey();
        PrivateKey activeKey = wallet.derivedKey("m / 48' / 4' / 1' / 0' / 0'", Network.TEST).getPrivKey();
        System.out.println(ownerKey.toString());
        System.out.println(activeKey.toString());

        ChainState eos = new ChainStateProxy("eos");
        System.out.println(eos.generatePublicKey(ownerKey));
        System.out.println(eos.generatePublicKey(activeKey));
    }
}
//{
//    "code": "eosio.token",
//    "action": "transfer",
//    "args": {
//        "from": "teos4oneroot",
//        "to": "devhotwal111",
//        "quantity": "1.0000 EOS",
//        "memo": "000006"
//    }
//}

