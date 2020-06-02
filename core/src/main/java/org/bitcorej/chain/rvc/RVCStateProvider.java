package org.bitcorej.chain.rvc;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.util.List;

public class RVCStateProvider extends BitcoinStateProvider {
    public RVCStateProvider(Network network) {
        super(network);
        super.params = RVCNetParams.get();
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject rawTxJSON = new JSONObject(rawTx);
        Transaction tx = buildTransaction(rawTx);
        tx.setVersion(2);
        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);
            Script scriptPubKey = new Script(input.getScriptBytes());

            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));

            int sigHash = TransactionSignature.calcSigHashValue(Transaction.SigHash.ALL, false, true);
            Sha256Hash hash = tx.hashForSignature(i, input.getScriptBytes(), (byte) sigHash);
            ECKey.ECDSASignature ecSig = ecKey.sign(hash);
            TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false, true);
            if (scriptPubKey.isSentToRawPubKey()) {
                input.setScriptSig(ScriptBuilder.createInputScript(txSig));
            } else {
                if (!scriptPubKey.isSentToAddress()) {
                    return null;
                }
                input.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
            }
        }
        JSONObject packedTx = new JSONObject();
        packedTx.put("txid", tx.getHashAsString());
        packedTx.put("raw", NumericUtil.bytesToHex(tx.bitcoinSerialize()));

        if (rawTxJSON.has("destinations")) {
            packedTx.put("destinations", rawTxJSON.getJSONArray("destinations"));
        }

        return packedTx.toString();
    }
}