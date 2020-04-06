package org.bitcorej.chain.eos;

import io.eblock.eos4j.Ecc;
import io.eblock.eos4j.api.vo.transaction.push.Tx;
import io.eblock.eos4j.api.vo.transaction.push.TxAction;
import io.eblock.eos4j.api.vo.transaction.push.TxSign;
import io.eblock.eos4j.ese.Action;
import io.eblock.eos4j.ese.DataParam;
import io.eblock.eos4j.ese.DataType;
import io.eblock.eos4j.utils.ByteUtils;
import io.eblock.eos4j.utils.Hex;
import org.bitcorej.chain.KeyPair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ABBCStateProvider extends EOSStateProvider {
    public ABBCStateProvider() {
        super();
        super.chainId = "6c1ead7f71153b1bae506818fbad4ea587abe20548d24e158b7dad7089f28adb";
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        EOSKey eosKey = EOSKey.fromWIF(secret);

        return new KeyPair(secret, eosKey.getPublicKeyAsHex("ABBC"));
    }

    public static String parseTransferPubData(String from, String to, String quantity, String memo) {
        DataParam[] datas = new DataParam[] { new DataParam(from, DataType.name, Action.transfer),
                new DataParam(to, to.matches("[a-km-zA-HJ-NP-Z1-9]{50}") ? DataType.key : DataType.name, Action.transfer),
                new DataParam(quantity, DataType.asset, Action.transfer),
                new DataParam(memo, DataType.string, Action.transfer), };
        byte[] allbyte = new byte[] {};
        for (DataParam value : datas) {
            allbyte = ByteUtils.concat(allbyte, value.seria());
        }
        return Hex.bytesToHexString(allbyte);
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
            dataMap.put("to", args.getString("to").replace("ABBC", ""));
            dataMap.put("quantity", new DataParam(args.getString("quantity"), DataType.asset, Action.transfer).getValue());
            dataMap.put("memo", args.getString("memo"));
            // action
            TxAction action = new TxAction(args.getString("from"), actionsJsonArray.getJSONObject(i).getString("code"), actionsJsonArray.getJSONObject(i).getString("action"), dataMap);
            actions.add(action);
        }
        tx.setActions(actions);
        // sign
        String sign = Ecc.signTransaction(keys.get(0), new TxSign(this.chainId, tx));
        for (int i = 0; i < actionsJsonArray.length(); i++) {
            JSONObject args = actionsJsonArray.getJSONObject(i).getJSONObject("args");
            // data parse
            String data = parseTransferPubData(args.getString("from"), args.getString("to").replace("ABBC", ""), args.getString("quantity"), args.getString("memo"));
            // reset data
            tx.getActions().get(i).setData(data);
        }
        // reset expiration
        tx.setExpiration(dateFormatter.format(new Date(1000 * Long.parseLong(tx.getExpiration().toString()))));
        try {
            String packedTx = this.pushTransaction("none", tx, new String[] { sign });
            return packedTx;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
