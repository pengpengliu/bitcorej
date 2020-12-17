package org.bitcorej.chain.bch;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

public class BCHStateProvider extends BitcoinStateProvider {
    public BCHStateProvider(Network network) {
        super(network);
    }

    public String generateP2PKHScript(String address) {
        if (address.matches("^(bitcoincash:)?(q|p)[a-z0-9]{41}")) {
            address = AddressConverter.toLegacyAddress(address);
        }
        return NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(this.params, address)).getProgram());
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        Transaction tx = super.buildTransaction(rawTx);
        tx.setVersion(2);

        JSONObject decodedTx = new JSONObject(rawTx);

        List<TransactionInput> inputs = tx.getInputs();

        for (int i = 0; i < inputs.size(); i++) {
            TransactionInput input = tx.getInput(i);

            Script scriptPubKey = new Script(input.getScriptBytes());

            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(this.selectPrivateKeys(scriptPubKey, keys)));

            String amountStr = decodedTx.getJSONArray("inputs").getJSONObject(i).getJSONObject("output").getString("amount");
            Long amount = new BigDecimal(amountStr).multiply(DECIMALS).longValue();

            Sha256Hash hash = tx.hashForSignatureWitness(i, scriptPubKey, Coin.valueOf(amount), Transaction.SigHash.ALL, false);
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

            // tx.addSignedInput(input.getOutpoint(), scriptPubKey, ecKey, Transaction.SigHash.ALL,false, true);
        }
        JSONObject packedTx = new JSONObject();
        packedTx.put("txid", tx.getHashAsString());
        packedTx.put("raw", NumericUtil.bytesToHex(tx.bitcoinSerialize()));

        if (decodedTx.has("destinations")) {
            packedTx.put("destinations", decodedTx.getJSONArray("destinations"));
        }
        return packedTx.toString();
    }
}
