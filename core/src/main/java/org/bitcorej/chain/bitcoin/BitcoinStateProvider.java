package org.bitcorej.chain.bitcoin;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;

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
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        return new KeyPair(ecKey.getPrivateKeyAsHex(), ecKey.toAddress(this.network.getNetworkParameters()).toString());
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, List<String> keys) {
        Transaction tx = new Transaction(this.params, rawTx);

        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);

            String key = keys.get(i);

            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(key));

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
}
