package org.bitcorej.chain.cosmos;

import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class CosmosStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        byte[] publicKey = ecKey.getPubKey();
        SHA256Digest hmac = new SHA256Digest();
        hmac.update(publicKey, 0, publicKey.length);
        byte[] hmacBytes = new byte[hmac.getDigestSize()];
        hmac.doFinal(hmacBytes, 0);
        System.out.println(NumericUtil.bytesToHex(hmacBytes));
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(hmacBytes, 0, hmacBytes.length);
        byte[] digestBytes = new byte[digest.getDigestSize()];
        digest.doFinal(digestBytes, 0);
        return new KeyPair(ecKey.getPrivateKeyAsHex(), Bech32.encode("cosmos", Bech32.toWords(digestBytes)));
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    @Override
    public Boolean validateTx(String rawTx, String requestTx) {
        return null;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
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

        String msg = "{\"account_number\":\"" + accountNumber + "\",\"chain_id\":\"" + chainId + "\",\"fee\":{\"amount\":[{\"amount\":\"" + feeAmount + "\",\"denom\":\"" + feeDenom + "\"}],\"gas\":\"" + gas + "\"},\"memo\":\"" + memo + "\",\"msgs\":[{\"type\":\"cosmos-sdk/MsgSend\",\"value\":{\"amount\":[{\"amount\":\"" + toAmount + "\",\"denom\":\"" + toDenom + "\"}],\"from_address\":\"" + from + "\",\"to_address\":\"" + to + "\"}}],\"sequence\":\"" + sequence + "\"}";

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

        String sigBase64 = Base64.encodeBase64String(signature);
        String pub = Base64.encodeBase64String(ecKey.getPubKey());

        return "{\"tx\":{\"msg\":[{\"type\":\"cosmos-sdk/MsgSend\",\"value\":{\"amount\":[{\"amount\":\"" + toAmount +"\",\"denom\":\"" + toDenom + "\"}],\"from_address\":\"" + from + "\",\"to_address\":\"" + to + "\"}}],\"fee\":{\"amount\":[{\"denom\":\"" + feeDenom + "\",\"amount\":\"" + feeAmount + "\"}],\"gas\":\"" + gas + "\"},\"signatures\":[{\"pub_key\":{\"type\":\"tendermint/PubKeySecp256k1\",\"value\":\"" + pub + "\"},\"signature\":\"" + sigBase64 + "\"}],\"memo\":\"" + memo + "\"},\"mode\":\"block\"}";
    }
}