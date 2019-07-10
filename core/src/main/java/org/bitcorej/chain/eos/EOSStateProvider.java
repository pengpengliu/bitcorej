package org.bitcorej.chain.eos;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eblock.eos4j.Ecc;
import io.eblock.eos4j.OfflineSign;
import io.eblock.eos4j.api.vo.transaction.push.Tx;
import io.eblock.eos4j.api.vo.transaction.push.TxAction;
import io.eblock.eos4j.api.vo.transaction.push.TxRequest;
import io.eblock.eos4j.api.vo.transaction.push.TxSign;
import io.eblock.eos4j.ese.Action;
import io.eblock.eos4j.ese.DataParam;
import io.eblock.eos4j.ese.DataType;
import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.core.Network;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EOSStateProvider implements ChainState {

    private Network network;
    private String chainId;

    public EOSStateProvider(Network network) {
        switch (network) {
            case MAIN:
                chainId = "aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906";
                break;
            case TEST:
                chainId = "e70aaab8997e1dfce58fbfac80cbbb8fecec7b99cf982a9444273cbc64c41473";
                break;
        }

        this.network = network;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        EOSKey eosKey = EOSKey.fromWIF(secret);

        return new KeyPair(secret, eosKey.getPublicKeyAsHex());
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(EOSKey.fromPrivate(new ECKey().getPrivKeyBytes()).toString());
    }

    @Override
    public Boolean validateTx(String rawTx, String tx) {
        Transaction decodedTx = this.decodeRawTransaction(rawTx);
        return decodedTx.equals(new Transaction(tx));
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        JSONObject jsonObject = new JSONObject(rawTx);

        Transaction tx = new Transaction();

        JSONArray actions = jsonObject.getJSONArray("actions");
        for (int i = 0; i < actions.length(); i++) {
            JSONObject action = actions.getJSONObject(i);
            if (!action.getString("code").equals("eosio.token") || !action.getString("action").equals("transfer")) {
                break;
            }
            JSONObject args = actions.getJSONObject(i).getJSONObject("args");
            String quantity = args.getString("quantity");
            BigDecimal amount = new BigDecimal(quantity.substring(0, quantity.indexOf(" ")));
            tx.addInput(tx.new Input(args.getString("from"), amount));
            tx.addOutput(tx.new Output(args.getString("to"), amount, args.getString("memo")));
        }
        return tx;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject jsonObject = new JSONObject(rawTx);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        Tx tx = new Tx();
        try {
            tx.setExpiration(dateFormatter.parse(jsonObject.getString("expiration")).getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tx.setRef_block_num(jsonObject.getLong("ref_block_num"));
        tx.setRef_block_prefix(jsonObject.getLong("ref_block_prefix"));
        tx.setNet_usage_words(jsonObject.getLong("max_net_usage_words"));
        tx.setMax_cpu_usage_ms(jsonObject.getLong("max_cpu_usage_ms"));
        tx.setDelay_sec(jsonObject.getLong("delay_sec"));
        // actions
        List<TxAction> actions = new ArrayList<>();

        JSONArray actionsJsonArray = jsonObject.getJSONArray("actions");
        for (int i = 0; i < actionsJsonArray.length(); i++) {
            JSONObject args = actionsJsonArray.getJSONObject(i).getJSONObject("args");
            // data
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("from", args.getString("from"));
            dataMap.put("to", args.getString("to"));
            dataMap.put("quantity", new DataParam(args.getString("quantity"), DataType.asset, Action.transfer).getValue());
            dataMap.put("memo", args.getString("memo"));
            // action
            TxAction action = new TxAction(args.getString("from"), actionsJsonArray.getJSONObject(i).getString("code"), actionsJsonArray.getJSONObject(i).getString("action"), dataMap);
            actions.add(action);
        }
        tx.setActions(actions);
        // sgin
        String sign = Ecc.signTransaction(keys.get(0), new TxSign(this.chainId, tx));
        for (int i = 0; i < actionsJsonArray.length(); i++) {
            JSONObject args = actionsJsonArray.getJSONObject(i).getJSONObject("args");
            // data parse
            String data = Ecc.parseTransferData(args.getString("from"), args.getString("to"), args.getString("quantity"), args.getString("memo"));
            // reset data
            tx.getActions().get(i).setData(data);
        }
        // reset expiration
        tx.setExpiration(dateFormatter.format(new Date(1000 * Long.parseLong(tx.getExpiration().toString()))));
        try {
            ObjectMapper mapper = new ObjectMapper();
            String mapJakcson = mapper.writeValueAsString(tx);
            JSONObject packedTx = new JSONObject(mapJakcson);
            packedTx.put("signatures", new String[] { sign });
            return packedTx.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
