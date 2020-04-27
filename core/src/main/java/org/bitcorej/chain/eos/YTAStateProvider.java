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

public class YTAStateProvider extends EOSStateProvider {
    public YTAStateProvider() {
        super();
        super.chainId = "9d7bec4bf167a7b136d0b45d8aac77bd45e761e35cbd2b7d0e88dfe05ebf3d62";
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        EOSKey eosKey = EOSKey.fromWIF(secret);

        return new KeyPair(secret, eosKey.getPublicKeyAsHex("YTA"));
    }

    String parseYSRTransferData(String from, String to, String quantity, String memo) {
        DataParam[] datas = new DataParam[] { new DataParam(from, DataType.name, Action.transfer),
                new DataParam(to, DataType.name, Action.transfer),
                new DataParam(quantity, DataType.asset, Action.transfer),
                new DataParam("true", DataType.unit16, Action.transfer),
                new DataParam(memo, DataType.string, Action.transfer), };
        byte[] allbyte = new byte[] {};
        for (DataParam value : datas) {
            if (value.getValue().equals("true")) {
                allbyte = ByteUtils.concat(allbyte, new byte[] { 1 });
            } else {
                allbyte = ByteUtils.concat(allbyte, value.seria());
            }
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
            String code = actionsJsonArray.getJSONObject(i).getString("code");
            // data
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("from", args.getString("from"));
            dataMap.put("to", args.getString("to"));
            dataMap.put("quantity", new DataParam(args.getString("quantity"), DataType.asset, Action.transfer).getValue());
            if (code.equals("ysr.ystar")) {
                dataMap.put("bcreate", true);
            }
            dataMap.put("memo", args.getString("memo"));
            // action
            TxAction action = new TxAction(args.getString("from"), code, actionsJsonArray.getJSONObject(i).getString("action"), dataMap);
            actions.add(action);
        }
        tx.setActions(actions);
        // sign
        String sign = Ecc.signTransaction(keys.get(0), new TxSign(this.chainId, tx));
        for (int i = 0; i < actionsJsonArray.length(); i++) {
            JSONObject args = actionsJsonArray.getJSONObject(i).getJSONObject("args");
            String code = actionsJsonArray.getJSONObject(i).getString("code");
            // data parse
            String data = code.equals("ysr.ystar") ? parseYSRTransferData(args.getString("from"), args.getString("to"), args.getString("quantity"), args.getString("memo")) : Ecc.parseTransferData(args.getString("from"), args.getString("to"), args.getString("quantity"), args.getString("memo"));
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
