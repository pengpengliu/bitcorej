package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;
import org.bitcorej.chain.bch.BCHStateProvider;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.chain.dash.DashStateProvider;
import org.bitcorej.core.Network;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExampleBitcoinCashWallet {
    public static void main(String[] args) throws Exception {
        ChainState bch = new BCHStateProvider(Network.MAIN);
        System.out.println(bch.generateKeyPair());
        // 1JEo2RDXrBM5LqEExhUDx7LHQtsiwWLAqi
        // 3866331504522df0119f65104caf6873618e616349cce285f4a57d49b277d472
        List<String> keys = Collections.singletonList("3866331504522df0119f65104caf6873618e616349cce285f4a57d49b277d472");

        ArrayList<UnspentOutput> utxos = new ArrayList<>();
        UnspentOutput utxo1 = new UnspentOutput(
                "0dfc7997593166351d57c280d4d1869852b37ff1e51660a680b42267d3882cde",
                0,"1JEo2RDXrBM5LqEExhUDx7LHQtsiwWLAqi", new BigDecimal("0.001"));
        UnspentOutput utxo2 = new UnspentOutput(
                "05184c8a8c5449f5df9d1e881ae3d1bb5fba00d6d1febf4f6fb078914571a5df",
                1,"1JEo2RDXrBM5LqEExhUDx7LHQtsiwWLAqi", new BigDecimal("0.08953875"));
        utxos.add(utxo1);
        utxos.add(utxo2);
        ArrayList<Recipient> recipients = new ArrayList<>();
        recipients.add(new Recipient("1NkWTNNNSPozbCGugZwsj18xEoxTzNFSfB", new BigDecimal("0.0119302000")));
        String encodedTx = BitcoinStateProvider.encodeTransaction(utxos, recipients, "1NkWTNNNSPozbCGugZwsj18xEoxTzNFSfB", new BigDecimal("0.00006980"));
        System.out.println(encodedTx);

        System.out.println(bch.signRawTransaction(encodedTx, keys));
    }
}
