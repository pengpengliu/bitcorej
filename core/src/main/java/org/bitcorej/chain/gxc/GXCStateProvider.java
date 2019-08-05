package org.bitcorej.chain.gxc;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.RandomUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.trx.crypto.Hash;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class GXCStateProvider implements ChainState {
    protected static final BigDecimal DECIMALS = new BigDecimal(10).pow(5);

    private static final String BITSHARES_PREFIX = "GXC";
    private static final int DEFAULT_EXPIRATION_TIME = 360;

    private static byte[] calculateChecksum(byte[] data){
        byte[] checksum = new byte[160 / 8];
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(data, 0, data.length);
        ripemd160Digest.doFinal(checksum, 0);
        return Arrays.copyOfRange(checksum, 0, 4);
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        byte[] pubKey = ecKey.getPubKey();
        byte[] checksum = calculateChecksum(pubKey);
        byte[] pubKeyChecksummed = Bytes.concat(pubKey, checksum);
        String address = BITSHARES_PREFIX + Base58.encode(pubKeyChecksummed);
        return new KeyPair(ecKey.getPrivateKeyAsHex(), address);
    }

    private static ECKey addressToPublicKey(String address) {
        byte[] decoded = Base58.decode(address.substring(3, address.length()));
        byte[] pubKey = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
        byte[] checksum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
        byte[] calculatedChecksum = calculateChecksum(pubKey);
        Preconditions.checkArgument(Arrays.deepEquals(new byte[][] {checksum}, new byte[][] {calculatedChecksum}), "checkSum error");
        return ECKey.fromPublicOnly(pubKey);
    }

    public static JSONObject buildMEMO(String memoFromPublicKey, String memoToPublicKey, String privateKey, String memo) {
        // 构建 MEMO
        JSONObject memoObject = null;
        // The 1s are base58 for all zeros (null)
        if (Pattern.matches(memoFromPublicKey, "111111111111111111111")) {
            memoFromPublicKey = null;
        }

        if (Pattern.matches(memoToPublicKey, "111111111111111111111")) {
            memoToPublicKey = null;
        }
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(privateKey));
        if (!addressToPublicKey(memoFromPublicKey).getPublicKeyAsHex().equals(ecKey.getPublicKeyAsHex())) {
            throw new RuntimeException("memo signer not exist");
        }

        memoObject = new JSONObject();
        BigInteger nonce = BigInteger.valueOf(RandomUtils.nextLong(0L, Long.MAX_VALUE));
        byte[] message = Crypto.encryptMessage(ecKey, addressToPublicKey(memoToPublicKey), nonce, memo);
        memoObject.put("from", memoFromPublicKey);
        memoObject.put("to", memoToPublicKey);
        memoObject.put("nonce", nonce.toString());
        memoObject.put("message", NumericUtil.bytesToHex(message));
        return memoObject;
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
        try {
            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(0)));

            JSONObject jsonObject = new JSONObject(rawTx);
            String chainId = jsonObject.getString("chainId");
            String time = jsonObject.getString("time");
            SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            ISO8601DATEFORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
            long expirationTime = (ISO8601DATEFORMAT.parse(time).getTime() / 1000) + DEFAULT_EXPIRATION_TIME;
            long headBlockNumber = jsonObject.getLong("headBlockNumber");
            long headBlockId = jsonObject.getLong("headBlockId");

            String fromId = jsonObject.getJSONObject("from").getString("id");
            String toId = jsonObject.getJSONObject("to").getString("id");
            String amount = new BigDecimal(jsonObject.getString("amount")).multiply(DECIMALS).toBigInteger().toString();
            String amountAssetId = jsonObject.getString("coinAssetId");
            String fee = new BigInteger(jsonObject.getString("fee")).toString();
            String feeAssetId = jsonObject.getString("feeAssetId");
            JSONObject memoObject = jsonObject.has("memo") ? jsonObject.getJSONObject("memo") : null;

            // Make tx json
            JSONObject txJson = new JSONObject();
            txJson.put("ref_block_num", headBlockNumber);
            txJson.put("ref_block_prefix", headBlockId);
            txJson.put("expiration", ISO8601DATEFORMAT.format(new Date(expirationTime * 1000)));

            JSONArray operations = new JSONArray();
            JSONArray operation = new JSONArray();
            operation.put(0);
            JSONObject operationObject = new JSONObject();
            operationObject.put("from", fromId);
            operationObject.put("to", toId);
            JSONObject amountObject = new JSONObject();
            amountObject.put("amount", amount);
            amountObject.put("asset_id", amountAssetId);
            operationObject.put("amount", amountObject);
            JSONObject feeObject = new JSONObject();
            feeObject.put("amount", fee);
            feeObject.put("asset_id", feeAssetId);
            operationObject.put("fee", feeObject);
            operationObject.put("extensions", new JSONArray());
            if (memoObject != null) {
                operationObject.put("memo", memoObject);
            }
            operation.put(operationObject);
            operations.put(operation);

            txJson.put("operations", operations);
            txJson.put("extensions", new JSONArray());

            String serialized = jsonObject.getString("serialized");
            // String serialized = NumericUtil.bytesToHex(TxSerializerUtil.serializeTransaction(txJson.toString()));
            // Creating a List of Bytes and adding the first bytes from the chain apiId
            List<Byte> byteArray = new ArrayList<>();
            byteArray.addAll(Bytes.asList(NumericUtil.hexToBytes(chainId)));
            byteArray.addAll(Bytes.asList(NumericUtil.hexToBytes(serialized)));

            String txid = NumericUtil.bytesToHex(BitUtils.copyOfRange(Sha256.from(NumericUtil.hexToBytes(serialized)).getBytes(), 0, 20));

            byte[] signature = Crypto.signature(Bytes.toArray(byteArray), ecKey);
            JSONArray signatures = new JSONArray();
            signatures.put(NumericUtil.bytesToHex(signature));
            txJson.put("txid", txid);
            txJson.put("signatures", signatures);

            return txJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
