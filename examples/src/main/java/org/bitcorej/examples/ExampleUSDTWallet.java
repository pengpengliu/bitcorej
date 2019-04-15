package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.chain.usdt.USDTStateProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExampleUSDTWallet {
    public static void main(String[] args) throws Exception {
        ChainState usdt = new ChainStateProxy("usdt", "test");

        KeyPair keyPair = usdt.generateKeyPair("e580512c800c6de3bd5e65695b4cab739211b7ac41ffc2991b0cf75c4d3ccbdf");
        System.out.println(keyPair);

        List<Recipient> recipients = USDTStateProvider.buildRecipients("mrmejCPpCUBroPn2XqPU2LpZ9jaDrmQZQr", "mjhAYkzNQbvdWAR2CTtP5HRqdr7RhaWE29", new BigDecimal("10"), 1);

        ArrayList<UnspentOutput> utxos = new ArrayList<>();
        UnspentOutput utxo = new UnspentOutput(
                "4f451420f8fa159e431f23c6e7c5ca3ba5e9a2c2b13154c6ec6e3977aeff408d",
                1,"mrmejCPpCUBroPn2XqPU2LpZ9jaDrmQZQr", new BigDecimal("0.06401269"));
        utxos.add(utxo);

        String encodedTx = BitcoinStateProvider.encodeTransaction(utxos, recipients, "mrmejCPpCUBroPn2XqPU2LpZ9jaDrmQZQr", new BigDecimal("0.0005"));
        System.out.println(encodedTx);

        ArrayList<String> keys = new ArrayList<>();
        keys.add(keyPair.getSecret());
        String signedTx = usdt.signRawTransaction(encodedTx, keys);
        System.out.println(signedTx);
    }
}
