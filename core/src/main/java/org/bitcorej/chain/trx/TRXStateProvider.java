package org.bitcorej.chain.trx;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.trx.crypto.ECKey;
import org.bitcorej.utils.NumericUtil;

import java.security.SecureRandom;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class TRXStateProvider implements ChainState {

    public static String encode58Check(byte[] input) {
        byte[] hash0 = Sha256Hash.hash(input);
        byte[] hash1 = Sha256Hash.hash(hash0);
        byte[] inputCheck = new byte[input.length + 4];
        System.arraycopy(input, 0, inputCheck, 0, input.length);
        System.arraycopy(hash1, 0, inputCheck, input.length, 4);
        return Base58.encode(inputCheck);
    }

    private static byte[] decode58Check(String input) {
        byte[] decodeCheck = Base58.decode(input);
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = Sha256Hash.hash(decodeData);
        byte[] hash1 = Sha256Hash.hash(hash0);
        if (hash1[0] == decodeCheck[decodeData.length] &&
                hash1[1] == decodeCheck[decodeData.length + 1] &&
                hash1[2] == decodeCheck[decodeData.length + 2] &&
                hash1[3] == decodeCheck[decodeData.length + 3]) {
            return decodeData;
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        return new KeyPair(secret, encode58Check(ecKey.getAddress()));
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(NumericUtil.bytesToHex(new ECKey(new SecureRandom()).getPrivKeyBytes()));
    }

    @Override
    public Boolean validateTx(String rawTx, String requestTx) {
        return null;
    }

    @Override
    public org.bitcorej.chain.Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(0)));
        JSONObject signedTx = new JSONObject(rawTx);
        String rawDataHex = signedTx.getString("raw_data_hex");
        byte[] hash = Sha256Hash.hash(NumericUtil.hexToBytes(rawDataHex));
        byte[] sign = ecKey.sign(hash).toByteArray();
        JSONArray signatures = new JSONArray();
        signatures.put(NumericUtil.bytesToHex(sign));
        signedTx.put("signature", signatures);
        return signedTx.toString();
    }
}
