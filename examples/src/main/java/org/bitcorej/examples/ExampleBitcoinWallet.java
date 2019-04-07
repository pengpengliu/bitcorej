package org.bitcorej.examples;

import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bch.BCHStateProvider;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.core.*;

import java.math.BigDecimal;
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
        String rawTxHex = "{\"txid\":\"636debaccfc614e4cb9043d96f3d4237af0554022da811a8faaf154cfe1393af\",\"version\":1,\"inputs\":[{\"txid\":\"05184c8a8c5449f5df9d1e881ae3d1bb5fba00d6d1febf4f6fb078914571a5df\",\"vout\":0,\"sequence\":4294967295,\"output\":{\"amount\":\"0.01\",\"script\":\"76a9147b70f5931cb13d4a3fb60cdb42544ecb6109086688ac\"}},{\"txid\":\"05184c8a8c5449f5df9d1e881ae3d1bb5fba00d6d1febf4f6fb078914571a5df\",\"vout\":1,\"sequence\":4294967295,\"output\":{\"amount\":\"0.08953875\",\"script\":\"76a9147b70f5931cb13d4a3fb60cdb42544ecb6109086688ac\"}}],\"outputs\":[{\"amount\":\"0.01\",\"script\":\"76a9147b70f5931cb13d4a3fb60cdb42544ecb6109086688ac\"},{\"amount\":\"0.08933875\",\"script\":\"76a9147b70f5931cb13d4a3fb60cdb42544ecb6109086688ac\"}],\"nLockTime\":0}";
        ArrayList<String> keys = new ArrayList<>();
        keys.add(privKey.toString());
        String signedTx = btc.signRawTransaction(rawTxHex, keys);
        System.out.println(signedTx);

        // offline
        ArrayList<UnspentOutput> utxos = new ArrayList<>();
        UnspentOutput utxo1 = new UnspentOutput(
                "05184c8a8c5449f5df9d1e881ae3d1bb5fba00d6d1febf4f6fb078914571a5df",
                0,"mrmejCPpCUBroPn2XqPU2LpZ9jaDrmQZQr", new BigDecimal("0.01"));
        UnspentOutput utxo2 = new UnspentOutput(
                "05184c8a8c5449f5df9d1e881ae3d1bb5fba00d6d1febf4f6fb078914571a5df",
                1,"mrmejCPpCUBroPn2XqPU2LpZ9jaDrmQZQr", new BigDecimal("0.08953875"));
        utxos.add(utxo1);
        utxos.add(utxo2);
        ArrayList<Recipient> recipients = new ArrayList<>();
        recipients.add(new Recipient("mrmejCPpCUBroPn2XqPU2LpZ9jaDrmQZQr", new BigDecimal("0.01")));
        String encodedTx = btc.encodeTransaction(utxos, recipients, "mrmejCPpCUBroPn2XqPU2LpZ9jaDrmQZQr", new BigDecimal("0.0002"));
        System.out.println(encodedTx);

        System.out.println(btc.signRawTransaction(encodedTx, keys));
    }
}
