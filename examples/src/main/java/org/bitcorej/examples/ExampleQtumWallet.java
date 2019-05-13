package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.core.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExampleQtumWallet {
    public static void main(String[] args) throws Exception {
        HDWallet wallet = new HDWallet("say tongue select oil blossom pond parent orphan crater sadness position coin");

        HDPrivateKey tprv = wallet.derivedKey("m/44'/1'/0'", Network.TEST);
        System.out.println(tprv);

        PrivateKey privKey = tprv.derived(0).derived(0).getPrivKey();

        ChainState qtum = new ChainStateProxy("qtum", "test");
        System.out.println(qtum.generateKeyPair(privKey.toString()));

        List<String> keys = Collections.singletonList(privKey.toString());

        ArrayList<UnspentOutput> utxos = new ArrayList<>();
        UnspentOutput utxo1 = new UnspentOutput(
                "d14ed23c498a7f8f5c5ec44efe8421ecb6e6888bb85ba0bd1605c12a3b6efbb4",
                1,"qUp5bB5Qb6MkAJ2okcPLPUGdpGtiH9bJA4", new BigDecimal("56"));
        utxos.add(utxo1);
        ArrayList<Recipient> recipients = new ArrayList<>();
        recipients.add(new Recipient(BitcoinStateProvider.generateP2PKHScript("qUp5bB5Qb6MkAJ2okcPLPUGdpGtiH9bJA4"), new BigDecimal("10")));
        String encodedTx = BitcoinStateProvider.encodeTransaction(utxos, recipients, "qUp5bB5Qb6MkAJ2okcPLPUGdpGtiH9bJA4", new BigDecimal("0.0009"));
        System.out.println(encodedTx);

        System.out.println(qtum.signRawTransaction(encodedTx, keys));
    }
}
