package org.bitcorej.chain.bitcoin;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.ChainState;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

import java.util.ArrayList;

public class BitcoinStateProvider implements ChainState {
    private final static long DUST_THRESHOLD = 2730;

    private Network network;
    private NetworkParameters params;

    public BitcoinStateProvider(Network network) {
        switch (network) {
            case MAIN:
                params = MainNetParams.get();
            case TEST:
                params = TestNet3Params.get();
            case REGTEST:
                params = RegTestParams.get();
        }

        this.network = network;
    }

    @Override
    public String getAddress(PrivateKey privKey) {
        // update network
        return new PrivateKey(privKey.toString(), network).toPublicKey().toAddress();
    }

    @Override
    public String getAddress(PublicKey pubKey) {
        // update network
        return new PublicKey(pubKey.toString(), network).toAddress();
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, ArrayList<PrivateKey> keys) {
        Transaction tx = new Transaction(params, rawTx);

        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);
            PrivateKey key = keys.get(i);

            ECKey ecKey = ECKey.fromPrivate(key.getRaw());

//            Script scriptPubKey = ScriptBuilder.createOutputScript(ecKey);

            Script scriptPubKey = ScriptBuilder.createOutputScript(ecKey.toAddress(network.getNetworkParameters()));

            Sha256Hash hash = tx.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
            ECKey.ECDSASignature ecSig = ecKey.sign(hash);
            TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
            if (scriptPubKey.isSentToRawPubKey()) {
                input.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
            } else {
                if (!scriptPubKey.isSentToAddress()) {
                    return new byte[0];
                }
                input.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
            }
        }
        
        return tx.bitcoinSerialize();
    }

    public class Recipient {
        private String address;
        private long amount;

        public Recipient(String address, long amount) {
            this.address = address;
            this.amount = amount;
        }
    }

    public byte[] buildTransaction(ArrayList<UnspentOutput> utxos, ArrayList<Recipient> recipients, String changeAddress, long fee) throws Exception {
        Transaction tx = new Transaction(params);
        long totalInputAmount = 0L;
        for (UnspentOutput utxo: utxos) {
            totalInputAmount += utxo.getAmount();
            tx.addInput(Sha256Hash.wrap(utxo.getTxId()), utxo.getVout(), new Script(utxo.getScriptPubKey()));
        }
        long totalOutputAmount = 0L;
        for (Recipient r: recipients) {
            totalOutputAmount += r.amount;
            tx.addOutput(Coin.valueOf(r.amount), Address.fromBase58(params, r.address));
        }
        if (totalInputAmount < totalOutputAmount) {
            throw new Exception("INSUFFICIENT FUNDS");
        }
        long changeAmount = totalInputAmount - (totalOutputAmount + fee);
        if (changeAmount >= DUST_THRESHOLD) {
            tx.addOutput(Coin.valueOf(changeAmount), Address.fromBase58(params, changeAddress));
        }
        return tx.bitcoinSerialize();
    }
}
