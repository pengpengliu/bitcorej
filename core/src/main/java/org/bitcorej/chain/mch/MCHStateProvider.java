package org.bitcorej.chain.mch;

import com.google.common.math.LongMath;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcorej.chain.cent.CENTStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;


public class MCHStateProvider extends CENTStateProvider {

    public MCHStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(MCHNetParameters.get());
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = MCHNetParameters.get();
                break;
        }

        super.network = network;
    }

    @Override
    protected Transaction buildTransaction(String json) {
        JSONObject jsonObject = new JSONObject(json);
        Transaction tx = new Transaction(this.params);

        JSONArray inputs = jsonObject.getJSONArray("inputs");
        for (int i = 0; i < inputs.length(); i++) {
            JSONObject input = inputs.getJSONObject(i);
            String amountStr = input.getJSONObject("output").getString("amount");
            Long amount = new BigDecimal(amountStr).multiply(new BigDecimal(10).pow(6)).longValue();
            Coin coin = Coin.valueOf(amount);
            TransactionInput txInput = new TransactionInput(this.params, tx, new Script(NumericUtil.hexToBytes(input.getJSONObject("output").getString("script"))).getProgram(), new TransactionOutPoint(params, input.getLong("vout"), Sha256Hash.wrap(input.getString("txid"))), coin);
            tx.addInput(txInput);

        }
        JSONArray outputs = jsonObject.getJSONArray("outputs");
        for (int i = 0; i < outputs.length(); i++) {
            JSONObject output = outputs.getJSONObject(i);
            Coin coin = Coin.valueOf(new BigDecimal(output.getString("amount")).multiply(BigDecimal.valueOf(LongMath.pow(10, 6))).longValue());
            tx.addOutput(new TransactionOutput(this.params, tx, coin, NumericUtil.hexToBytes(output.getString("script"))));
        }
        return tx;
    }

}
