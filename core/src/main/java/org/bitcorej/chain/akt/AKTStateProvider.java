package org.bitcorej.chain.akt;

import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcorej.chain.cosmos.CosmosStateProvider;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AKTStateProvider extends CosmosStateProvider {
    public AKTStateProvider() {
        super.bech32AccAddr = "akash";
        super.transferPrefix = "cosmos-sdk/MsgSend";
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject jsonObject = new JSONObject(rawTx);
        int accountNumber = jsonObject.getInt("account_number");
        String chainId = jsonObject.getString("chain_id");
        String feeDenom = jsonObject.getJSONObject("fees").getString("denom");
        String feeAmount = jsonObject.getJSONObject("fees").getString("amount");
        String gas = jsonObject.getString("gas");
        String memo = jsonObject.getString("memo");
        int sequence = jsonObject.getInt("sequence");
        String from = jsonObject.getString("from");
        String to = jsonObject.getJSONObject("msg").getString("to");
        String toDenom = jsonObject.getJSONObject("msg").getJSONArray("coins").getJSONObject(0).getString("denom");
        String toAmount = jsonObject.getJSONObject("msg").getJSONArray("coins").getJSONObject(0).getString("amount");

        String msg = "{\"account_number\":\"" + accountNumber + "\",\"chain_id\":\"" + chainId + "\",\"fee\":{\"amount\":[{\"amount\":\"" + feeAmount + "\",\"denom\":\"" + feeDenom + "\"}],\"gas\":\"" + gas + "\"},\"memo\":\"" + memo + "\",\"msgs\":[{\"type\":\"" + this.transferPrefix + "\",\"value\":{\"amount\":[{\"amount\":\"" + toAmount + "\",\"denom\":\"" + toDenom + "\"}],\"from_address\":\"" + from + "\",\"to_address\":\"" + to + "\"}}],\"sequence\":\"" + sequence + "\"}";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] hash = digest.digest(
                msg.getBytes(StandardCharsets.UTF_8));
        String sha256hex = new String(Hex.encode(hash));

        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(0)));
        ECKey.ECDSASignature sig = ecKey.sign(Sha256Hash.wrap(sha256hex));

        byte[] signature = ByteUtil.concat(sig.r.toByteArray(), sig.s.toByteArray());

        String sigBase64 = Base64.encodeBase64String(ByteUtil.trimLeadingZeroes(signature));
        String pub = Base64.encodeBase64String(ecKey.getPubKey());
        return "{\"tx\":{\"msg\":[{\"type\":\"" + this.transferPrefix + "\",\"value\":{\"amount\":[{\"amount\":\"" + toAmount +"\",\"denom\":\"" + toDenom + "\"}],\"from_address\":\"" + from + "\",\"to_address\":\"" + to + "\"}}],\"fee\":{\"amount\":[{\"denom\":\"" + feeDenom + "\",\"amount\":\"" + feeAmount + "\"}],\"gas\":\"" + gas + "\"},\"signatures\":[{\"pub_key\":{\"type\":\"tendermint/PubKeySecp256k1\",\"value\":\"" + pub + "\"},\"signature\":\"" + sigBase64 + "\"}],\"memo\":\"" + memo + "\"},\"mode\":\"async\"}";
    }
}
