package org.bitcorej.examples;

import org.bitcorej.chain.cosmos.CosmosStateProvider;
import org.bitcorej.core.*;

import java.util.Collections;
import java.util.List;

public class ExampleCosmosWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("evolve royal matter rapid mouse exclude reopen addict best rail young employ antique project rally smoke opinion neutral off average prevent guitar sphere quote");

        HDPrivateKey xprv = wallet.derivedKey("m/44'/118'/0'", Network.MAIN);
        System.out.println(xprv);

        PrivateKey privKey = xprv.derived(0).derived(0).getPrivKey();
        PublicKey pubKey = privKey.toPublicKey();
        System.out.println("Private key is: " + privKey.toString());
        System.out.println("Public key is: " + pubKey.toString());

        CosmosStateProvider cosmos = new CosmosStateProvider();
        System.out.println(cosmos.generateKeyPair(privKey.toString()));

        List<String> keys = Collections.singletonList("CEA7CC48BB46C9AA90911E00F327898FBC7FA35BA8C0827B66DDEA31FCB5FCD5");
        String signedTx = cosmos.signRawTransaction("{\"chain_id\":\"gaia-13003\",\"from\":\"cosmos1rtllrpf9njr539qfs50een06myxh8fh7p484r8\",\"account_number\":1210,\"sequence\":2,\"fees\":{\"denom\":\"muon\",\"amount\":\"10\"},\"gas\":\"200000\",\"memo\":\"\",\"msg\":{\"to\":\"cosmos1n90agkevn8ntlcxjtyv3tqm9ypm3vz2e7t6dqt\",\"coins\":[{\"denom\":\"muon\",\"amount\":\"1000\"}]}}", keys);
        System.out.println(signedTx);
    }
}
