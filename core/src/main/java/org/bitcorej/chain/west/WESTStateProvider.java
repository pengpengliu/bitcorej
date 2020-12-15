package org.bitcorej.chain.west;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import iost.crypto.Base58;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.vsys.HashUtil;
import org.bitcorej.crypto.Hash;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.curve25519.OpportunisticCurve25519Provider;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class WESTStateProvider implements ChainState {
    private final static BigDecimal DECIMALS = new BigDecimal(10).pow(8);

    private final static byte ADDRESS_VERSION = 1;
    private final static byte MAIN_NET_CHAIN_ID = 86;
    private final static byte SIGNATURE_LENGTH = 64;

    @Override
    public KeyPair generateKeyPair(String secret) {
        try {
            Constructor<OpportunisticCurve25519Provider> constructor = OpportunisticCurve25519Provider.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            OpportunisticCurve25519Provider provider = constructor.newInstance();
            byte[] publicKey = provider.generatePublicKey(Base58.decode(secret));
            byte[] publicKeyHash = Hash.keccak256(HashUtil.hashB(publicKey));
            publicKeyHash = Arrays.copyOfRange(publicKeyHash, 0, 20);
            byte[] withoutChecksum = Bytes.concat(new byte[]{ ADDRESS_VERSION, MAIN_NET_CHAIN_ID }, publicKeyHash);
            byte[] checksum = calcCheckSum(withoutChecksum);
            String address = Base58.encode(Bytes.concat(withoutChecksum, checksum));
            return new KeyPair(secret, address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Curve25519KeyPair keyPair = cipher.generateKeyPair();
        return generateKeyPair(Base58.encode(keyPair.getPrivateKey()));
    }

    byte[] calcPublicKey(String secret) {
        try {
            Constructor<OpportunisticCurve25519Provider> constructor = OpportunisticCurve25519Provider.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            OpportunisticCurve25519Provider provider = constructor.newInstance();
            return provider.generatePublicKey(Base58.decode(secret));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    byte[] calcCheckSum(byte[] input) {
        byte[] hash = Hash.keccak256(HashUtil.hashB(input));
        return Arrays.copyOfRange(hash, 0, 4);
    }

    byte[] sign(byte[] data, String secret) {
        try {
            Constructor<OpportunisticCurve25519Provider> constructor = OpportunisticCurve25519Provider.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            OpportunisticCurve25519Provider provider = constructor.newInstance();
            return provider.calculateSignature(provider.getRandom(SIGNATURE_LENGTH), Base58.decode(secret), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        try {
            JSONObject rawTxJSON = new JSONObject(rawTx);
            int type = 4;
            int version = 2;

            String amountStr = rawTxJSON.getString("amount");
            long amount = new BigDecimal(amountStr).multiply(DECIMALS).longValue();

            String feeStr = rawTxJSON.getString("fee");
            long fee = new BigDecimal(feeStr).multiply(DECIMALS).longValue(); // default: 0.1

            String recipient = rawTxJSON.getString("recipient");
            byte[] attachment = new byte[]{ 0x0, 0x0 };// NumericUtil.hexToBytes("000568656c6c6f"); // hello
            long timestamp = new Date().getTime();

            UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
            stream.write(type); // type
            stream.write(version); // version

            byte[] pubkey = calcPublicKey(keys.get(0));
            stream.write(pubkey); // senderPublicKey
            stream.write(new byte[]{ 0x0 }); // assetId: empty
            stream.write(new byte[]{ 0x0 }); // feeAssetId: empty
            stream.write(Longs.toByteArray(timestamp)); // timestamp
            stream.write(Longs.toByteArray(amount)); // amount
            stream.write(Longs.toByteArray(fee)); // fee
            stream.write(Base58.decode(recipient)); // recipient
            stream.write(attachment); // attachment: hello

            byte[] serialized = stream.toByteArray();
            String txid = Base58.encode(HashUtil.hashB(serialized));
            byte[] sig = sign(serialized, keys.get(0));

            JSONObject packedTx = new JSONObject();
            packedTx.put("type", type);
            packedTx.put("version", version);
            packedTx.put("senderPublicKey", Base58.encode(pubkey));
            packedTx.put("assetId", JSONObject.NULL);
            packedTx.put("recipient", recipient);
            packedTx.put("amount", amount);
            packedTx.put("attachment", "");
            packedTx.put("fee", fee);
            packedTx.put("feeAssetId", JSONObject.NULL);
            packedTx.put("timestamp", timestamp);
            packedTx.put("chainId", MAIN_NET_CHAIN_ID);
            packedTx.put("id", txid);

            JSONArray proofs = new JSONArray();
            proofs.put(Base58.encode(sig));
            packedTx.put("proofs", proofs);
            return packedTx.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
