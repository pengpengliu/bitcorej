package org.bitcorej.chain.eos;

import io.eblock.eos4j.ecc.EccTool;
import io.eblock.eos4j.utils.ByteUtils;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class FIOStateProvider extends EOSStateProvider {
    public FIOStateProvider() {
        super();
        super.chainId = "21dcae42c0182200e93f954a074011f9048a7624c6fe81d3c9541a614a88bd1c";
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        EOSKey eosKey = EOSKey.fromWIF(secret);
        return new KeyPair(secret, eosKey.getPublicKeyAsHex("FIO"));
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject jsonObject = new JSONObject(rawTx);

        String packed_trx = jsonObject.getString("packed_trx");

        byte[] signBuf = new byte[32];
        signBuf = ByteUtils.concat(NumericUtil.hexToBytes(packed_trx), signBuf);
        signBuf = ByteUtils.concat(NumericUtil.hexToBytes(chainId), signBuf);

        String signature = EccTool.signHash(keys.get(0), signBuf);
        JSONArray signatures = new JSONArray();
        signatures.put(signature);
        jsonObject.put("signatures", signatures);
        return jsonObject.toString();
    }
}
