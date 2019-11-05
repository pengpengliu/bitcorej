package org.bitcorej.chain.vsys;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.vsys.utils.BytesHelper;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.curve25519.java.curve_sigs;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VSYSStateProvider implements ChainState {
    private static final Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private static final byte ADDR_VERSION = 5;

    public static final byte TEST_NET = 'T';
    public static final byte MAIN_NET = 'M';

    private static final int KBYTE = 1024;
    private static final byte V2 = 2;

    private static final byte PAYMENT = 2;
    private static final byte EXECUTE_CONTRACT = 9;

    private byte chainId = MAIN_NET;

    public VSYSStateProvider() {}

    public VSYSStateProvider(org.bitcorej.core.Network network) {
        switch (network) {
            case MAIN:
                chainId = MAIN_NET;
                break;
            case TEST:
                chainId = TEST_NET;
                break;
        }
    }

    private static byte[] generateAddress(byte[] publicKey, byte chainId) {
        ByteBuffer buf = ByteBuffer.allocate(26);
        byte[] hash = HashUtil.secureHash(publicKey, 0, publicKey.length);
        buf.put(ADDR_VERSION).put(chainId).put(hash, 0, 20);
        byte[] checksum = HashUtil.secureHash(buf.array(), 0, 22);
        buf.put(checksum, 0, 4);
        return buf.array();
    }

    private static byte[] generatePublickKey(byte[] privateKey) {
        byte[] publicKey = new byte[32];
        curve_sigs.curve25519_keygen(publicKey, privateKey);
        return publicKey;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        byte[] privateKey = Base58.decode(secret);
        return new KeyPair(secret, Base58.encode(generateAddress(generatePublickKey(privateKey), chainId)));
    }

    @Override
    public KeyPair generateKeyPair() {
        Curve25519KeyPair keyPair = cipher.generateKeyPair();
        String secret = Base58.encode(keyPair.getPrivateKey());
        return generateKeyPair(secret);
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
        JSONObject rawTxJSON = new JSONObject(rawTx);
        String attachment = rawTxJSON.getString("attachment");
        byte[] attachmentBytes = (attachment == null ? "" : attachment).getBytes();
        String recipient = rawTxJSON.getString("recipient");
        long amount = rawTxJSON.getLong("amount");

        JSONObject packedTx = new JSONObject();
        packedTx.put("attachment", Base58.encode(attachmentBytes));

        Timestamp ts = new Timestamp(new Date().getTime());
        BigInteger timestamp = BigInteger.valueOf(ts.getTime() / 1000 * 1000000000 + ts.getNanos());
        packedTx.put("timestamp", timestamp.longValue());

        int type = 2;
        if (rawTxJSON.has("coin")) {
            JSONObject coin = rawTxJSON.getJSONObject("coin");
            if (!coin.get("id").equals("VSYS")) {
                type = 9;
            }
        }
        // Token Transfer
        if (type == 9) {
            JSONObject coin = rawTxJSON.getJSONObject("coin");
            String contractId = coin.getString("contractId");

            long fee = 30000000L;
            short feeScale = Short.parseShort("100");
            packedTx.put("fee", fee);
            packedTx.put("feeScale", feeScale);

            byte[] functionData = BytesHelper.toBytes((short)2);
            functionData = BytesHelper.concat(functionData, new byte[]{ 2 });
            functionData = BytesHelper.concat(functionData, Base58.decode(recipient));
            functionData = BytesHelper.concat(functionData, new byte[]{ 3 });
            functionData = BytesHelper.concat(functionData, BytesHelper.toBytes(amount));

            byte[] key = Base58.decode(keys.get(0));

            byte[] serialized = new byte[]{};
            serialized = BytesHelper.concat(serialized, new byte[]{ EXECUTE_CONTRACT });
            serialized = BytesHelper.concat(serialized, Base58.decode(contractId));
            serialized = BytesHelper.concat(serialized, BytesHelper.toBytes((short)3));  // functionIndex
            serialized = BytesHelper.concat(serialized, BytesHelper.toBytes((short)functionData.length));
            serialized = BytesHelper.concat(serialized, functionData);
            serialized = BytesHelper.concat(serialized, BytesHelper.toBytes((short)attachmentBytes.length));
            serialized = BytesHelper.concat(serialized, attachmentBytes);
            serialized = BytesHelper.concat(serialized, BytesHelper.toBytes(fee));
            serialized = BytesHelper.concat(serialized, BytesHelper.toBytes(feeScale));
            serialized = BytesHelper.concat(serialized, timestamp.toByteArray());

            packedTx.put("contractId", contractId);
            packedTx.put("functionIndex", 3);
            packedTx.put("functionData", Base58.encode(functionData));

            packedTx.put("senderPublicKey", Base58.encode(generatePublickKey(key)));
            String signature = sign(key, serialized);
            packedTx.put("signature", signature);
        }
        // Payment
        else {
            long fee = 10000000L;
            short feeScale = Short.parseShort("100");
            packedTx.put("fee", fee);
            packedTx.put("feeScale", feeScale);

            ByteBuffer buf = ByteBuffer.allocate(KBYTE);
            buf.put(PAYMENT);
            putBigInteger(buf, timestamp);
            buf.putLong(amount).putLong(fee);
            buf.putShort(feeScale);
            putRecipient(buf, chainId, recipient);
            putString(buf, attachment);

            packedTx.put("amount", amount);
            packedTx.put("recipient", recipient);

            byte[] key = Base58.decode(keys.get(0));
            packedTx.put("senderPublicKey", Base58.encode(generatePublickKey(key)));
            String signature = sign(key, toBytes(buf));

            packedTx.put("signature", signature);
        }

        return packedTx.toString();
    }

    private String sign(byte[] privateKey, byte[] bytes){
        return Base58.encode(cipher.calculateSignature(privateKey, bytes));
    }

    private static void putString(ByteBuffer buffer, String s) {
        if (s == null) s = "";
        putBytes(buffer, s.getBytes(UTF8));
    }

    private static void putBigInteger(ByteBuffer buffer, BigInteger b) {
        if (b == null) b = BigInteger.ZERO;
        buffer.put(b.toByteArray());
    }

    private static byte[] toBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.position()];
        buffer.position(0);
        buffer.get(bytes);
        return bytes;
    }

    private static void putBytes(ByteBuffer buffer, byte[] bytes) {
        buffer.putShort((short) bytes.length).put(bytes);
    }

    private static void putRecipient(ByteBuffer buffer, byte chainId, String recipient) {
        if (recipient.length() <= 30) {
            // assume an alias
            buffer.put((byte) 0x02).put(chainId).putShort((short) recipient.length()).put(recipient.getBytes(UTF8));
        }
        else {
            buffer.put(Base58.decode(recipient));
        }
    }

}
