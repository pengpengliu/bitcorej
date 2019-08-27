package org.bitcorej.chain.binance;

import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcorej.chain.cosmos.CosmosStateProvider;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class BinanceStateProvider extends CosmosStateProvider {
    public BinanceStateProvider() {
        super.bech32AccAddr = "bnb";
        super.transferPrefix = "cosmos-sdk/Send";
    }

    public byte[] encodeVariableUInt(long val) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        do {
            byte b = (byte)((val) & 0x7f);
            val >>= 7;
            b |= ( ((val > 0) ? 1 : 0 ) << 7 );
            output.write(b);
        } while( val != 0 );
        return output.toByteArray();
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject jsonObject = new JSONObject(rawTx);
        int accountNumber = jsonObject.getInt("account_number");
        String chainId = jsonObject.getString("chain_id");
        String memo = jsonObject.getString("memo");
        int sequence = jsonObject.getInt("sequence");
        String from = jsonObject.getString("from");
        String to = jsonObject.getJSONObject("msg").getString("to");
        String toDenom = jsonObject.getJSONObject("msg").getJSONArray("coins").getJSONObject(0).getString("denom");
        String toAmount = jsonObject.getJSONObject("msg").getJSONArray("coins").getJSONObject(0).getString("amount");

        String signMsg = "{\"account_number\":\"" + accountNumber + "\",\"chain_id\":\"" + chainId + "\",\"data\":null,\"memo\":\"" + memo + "\",\"msgs\":[{\"inputs\":[{\"address\":\"" + from + "\",\"coins\":[{\"amount\":" + toAmount + ",\"denom\":\"" + toDenom + "\"}]}],\"outputs\":[{\"address\":\"" + to + "\",\"coins\":[{\"amount\":" + toAmount + ",\"denom\":\"" + toDenom + "\"}]}]}],\"sequence\":\"" + sequence + "\",\"source\":\"0\"}";
        System.out.println(signMsg);
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] hash = digest.digest(
                signMsg.getBytes(StandardCharsets.UTF_8));
        String sha256hex = new String(Hex.encode(hash));

        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(0)));
        ECKey.ECDSASignature sig = ecKey.sign(Sha256Hash.wrap(sha256hex));
        System.out.println(NumericUtil.bytesToHex(ecKey.getPrivKeyBytes()));
        byte[] signature = ByteUtil.trimLeadingZeroes(ByteUtil.concat(sig.r.toByteArray(), sig.s.toByteArray()));

        return "{\"msg\":[{\"inputs\":[{\"address\":\"" + from + "\",\"coins\":[{\"denom\":\"" + toDenom + "\",\"amount\":" + toAmount + "}]}],\"outputs\":[{\"address\":\"" + to + "\",\"coins\":[{\"denom\":\"" + toDenom + "\",\"amount\":" + toAmount + "}]}],\"msgType\":\"MsgSend\"}],\"signatures\":[{\"pub_key\":\"" + ecKey.getPublicKeyAsHex() + "\",\"signature\":\"" + NumericUtil.bytesToHex(signature) + "\",\"account_number\":" + accountNumber + ",\"sequence\":" + sequence + "}],\"memo\":\"" + memo + "\",\"source\":0,\"data\":\"\",\"msgType\":\"StdTx\"}";
    }
}