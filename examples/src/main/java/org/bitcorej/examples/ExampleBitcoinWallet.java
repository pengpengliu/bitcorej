package org.bitcorej.examples;

import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bch.BCHStateProvider;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.*;

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

        BitcoinStateProvider btc = new BitcoinStateProvider(Network.TEST);
        BitcoinStateProvider bch = new BCHStateProvider(Network.TEST);

        KeyPair keyPair = btc.generateKeyPair(privKey.toString());

        System.out.println(keyPair);
        System.out.println(btc.calcSegWitAddress(keyPair.getPublic()));
        // 1DJwaDEZTYkW2eASD4wX3vv46QHs4LcAbf
        // 3QFWV5YAp68uPEStXmGTXUFLuzo8AHCmkr
        // System.out.println(btc.calcSegWitAddress("1DJwaDEZTYkW2eASD4wX3vv46QHs4LcAbf"));

//        String rawTxHex = "{\"txid\":\"b1a77ffb20e14ce7532b3512eaddae483fda845e151a325fbd9f67866b15d513\",\"version\":1,\"inputs\":[{\"txid\":\"8d92624079c3862d8f14364834761953eca53aa2ccd34f29e907c0759a80a68f\",\"vout\":0,\"sequence\":4294967295,\"output\":{\"amount\":\"0.10050414\",\"script\":\"76a9147b70f5931cb13d4a3fb60cdb42544ecb6109086688ac\"}}],\"outputs\":[{\"amount\":\"0.0001\",\"script\":\"76a914d6ed6281b5e52c30aceb25f1547c34ff47c9913c88ac\"},{\"amount\":\"0.10020414\",\"script\":\"76a914d6ed6281b5e52c30aceb25f1547c34ff47c9913c88ac\"}],\"nLockTime\":0}";
        String rawTxHex = "{\"txid\":\"c39008572a71a05c3bcea3c53b88e407705a05a5ef386fe3364cd9b28e540238\",\"inputs\":[{\"txid\":\"a1800195d30867da451c34be987ca5d50251d49a46a96fe0bfc168ce76dd5ad4\",\"vout\":1,\"output\":{\"amount\":\"0.520057040000000000\",\"script\":\"76a9147b70f5931cb13d4a3fb60cdb42544ecb6109086688ac\"}}],\"outputs\":[{\"amount\":\"0.1\",\"script\":\"76a9147cef4757ea9bca8c3b652a7ea9a36fb5b131cfa288ac\"},{\"amount\":\"0.420005440000000000\",\"script\":\"76a9147b70f5931cb13d4a3fb60cdb42544ecb6109086688ac\"}]}";
        ArrayList<String> keys = new ArrayList<>();
        keys.add(privKey.toString());
        String signedTx = bch.signRawTransaction(rawTxHex, keys);
        System.out.println(signedTx);
    }
}
