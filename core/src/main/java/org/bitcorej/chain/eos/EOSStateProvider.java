package org.bitcorej.chain.eos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.eblock.eos4j.Ecc;
import io.eblock.eos4j.OfflineSign;
import io.eblock.eos4j.api.vo.SignParam;
import io.eblock.eos4j.api.vo.transaction.push.Tx;
import io.eblock.eos4j.api.vo.transaction.push.TxAction;
import io.eblock.eos4j.api.vo.transaction.push.TxSign;
import io.eblock.eos4j.ese.Action;
import io.eblock.eos4j.ese.DataParam;
import io.eblock.eos4j.ese.DataType;
import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.eos.types.TypeChainId;
import org.bitcorej.core.Network;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EOSStateProvider implements ChainState {

    private Network network;
    private TypeChainId chainId;

    public EOSStateProvider(Network network) {
        switch (network) {
            case MAIN:
                chainId = new TypeChainId("aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906");
                break;
            case TEST:
                chainId = new TypeChainId("e70aaab8997e1dfce58fbfac80cbbb8fecec7b99cf982a9444273cbc64c41473");
                break;
        }

        this.network = network;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        EOSKey eosKey = EOSKey.fromWIF(secret);

        return new KeyPair(eosKey.getPublicKeyAsHex(), secret);
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(EOSKey.fromPrivate(new ECKey().getPrivKeyBytes()).toString());
    }

    private String buildRawTransaction() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        Tx tx = new Tx();
        try {
            tx.setExpiration(dateFormatter.parse("2019-03-22T03:11:44").getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tx.setRef_block_num(19594346l);
        tx.setRef_block_prefix(1109991391l);
        tx.setNet_usage_words(0l);
        tx.setMax_cpu_usage_ms(0l);
        tx.setDelay_sec(0l);
        // actions
        List<TxAction> actions = new ArrayList<>();
        // data
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("from", "teos4oneroot");
        dataMap.put("to", "devhotwal111");
        dataMap.put("quantity", new DataParam("1.0000 EOS", DataType.asset, Action.transfer).getValue());
        dataMap.put("memo", "000006");
        // action
        TxAction action = new TxAction("teos4oneroot", "eosio.token", "transfer", dataMap);
        actions.add(action);
        tx.setActions(actions);
        // sgin
        String sign = Ecc.signTransaction("5KUZpcfAEtKMmZxQZbCp3HYJA29py744AbzKynWoXeKf2anQGFy", new TxSign("e70aaab8997e1dfce58fbfac80cbbb8fecec7b99cf982a9444273cbc64c41473", tx));
        // data parse
        String data = Ecc.parseTransferData("teos4oneroot", "devhotwal111", "1.0000 EOS", "000006");
        // reset data
        action.setData(data);
        // reset expiration
        tx.setExpiration(dateFormatter.format(new Date(1000 * Long.parseLong(tx.getExpiration().toString()))));
        try {
            return new OfflineSign().pushTransaction("none", tx, new String[] { sign });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
//        Gson mGson = new GsonBuilder()
//                .registerTypeAdapterFactory(new GsonEosTypeAdapterFactory())
//                .excludeFieldsWithoutExposeAnnotation().create();
//
//        SignedTransaction tx = mGson.fromJson(rawTx, SignedTransaction.class);
//        tx.sign(EOSKey.fromWIF(keys.get(0)).getPrivateKey(), this.chainId);
//
//        return mGson.toJson(tx);
        return buildRawTransaction();
    }
}
