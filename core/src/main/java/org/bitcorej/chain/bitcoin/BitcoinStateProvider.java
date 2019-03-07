package org.bitcorej.chain.bitcoin;

import com.google.common.collect.ImmutableList;
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
import java.util.List;

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
    public String createAddress(PrivateKey privKey) {
        // update network
        return new PrivateKey(privKey.toString(), network).toPublicKey().toAddress();
    }

    @Override
    public String createAddress(PublicKey pubKey) {
        // update network
        return new PublicKey(pubKey.toString(), network).toAddress();
    }

    @Override
    public String createAddress(List<PublicKey> publicKeys) {
        if (publicKeys.size() == 1) {
            return new PublicKey(publicKeys.get(0).toString(), network).toAddress();
        } else if (publicKeys.size() == 3) {
            List<ECKey> keys = ImmutableList.of(
                    ECKey.fromPublicOnly(publicKeys.get(0).getRaw()),
                    ECKey.fromPublicOnly(publicKeys.get(1).getRaw()),
                    ECKey.fromPublicOnly(publicKeys.get(2).getRaw())
            );
            Script redeemScript = ScriptBuilder.createRedeemScript(2, keys);
            Script script = ScriptBuilder.createP2SHOutputScript(redeemScript);

            Address multisig = Address.fromP2SHScript(params, script);
            return multisig.toString();
        }
        return null;
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, List<PrivateKey> keys) {
        Transaction tx = new Transaction(this.params, rawTx);

        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);
            PrivateKey key = keys.get(i);

            ECKey ecKey = ECKey.fromPrivate(key.getRaw());

            Script scriptPubKey = ScriptBuilder.createOutputScript(ecKey.toAddress(this.params));

            Sha256Hash hash = tx.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
            ECKey.ECDSASignature ecSig = ecKey.sign(hash);
            TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
            if (scriptPubKey.isSentToRawPubKey()) {
                input.setScriptSig(ScriptBuilder.createInputScript(txSig));
            } else {
                if (!scriptPubKey.isSentToAddress()) {
                    return null;
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
