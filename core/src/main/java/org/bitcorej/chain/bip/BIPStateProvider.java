package org.bitcorej.chain.bip;

import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BIPStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        String address = Numeric.prependHexPrefix(Keys.getAddress(Sign.publicKeyFromPrivate(ecKey.getPrivKey())));
        return new KeyPair(ecKey.getPrivateKeyAsHex(), address.replace("0x", "Mx"));
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
        BigInteger nonce = BigInteger.valueOf(jsonObject.getInt("nonce"));
        BigInteger gasPrice = new BigInteger(jsonObject.getString("gasPrice"));
        BigInteger value = new BigInteger(jsonObject.getString("value"));
        String to = jsonObject.getString("to");
        Sign.SignatureData signatureData = Sign.signMessage(
                encode(nonce, gasPrice, to, value, null), Credentials.create(keys.get(0)).getEcKeyPair());
        JSONObject packed = new JSONObject();
        packed.put("raw", Numeric.prependHexPrefix(NumericUtil.bytesToHex(encode(nonce, gasPrice, to, value, signatureData))));
        return packed.toString();
    }

    byte[] encode(BigInteger nonce, BigInteger gasPrice, String to, BigInteger value, Sign.SignatureData signatureData) {
        List<RlpType> values = new ArrayList<>();
        // nonce
        values.add(RlpString.create(nonce));
        // chainId: mainnet
        values.add(RlpString.create(NumericUtil.hexToBytes("0x01")));
        // gasPrice
        values.add(RlpString.create(gasPrice));
        // gasCoin
        values.add(RlpString.create(strrpad(10, "BIP")));
        // type: SEND
        values.add(RlpString.create(BigInteger.valueOf(1)));
        // data
        values.add(RlpString.create(encodeSendCoin(to, value)));
        // payload
        values.add(RlpString.create(new byte[]{}));
        // serviceData
        values.add(RlpString.create(new byte[]{}));
        // signatureType: Single
        values.add(RlpString.create(BigInteger.valueOf(1)));
        // signatureData
        if (signatureData != null) {
            List<RlpType> signature = new ArrayList<>();
            signature.add(RlpString.create(signatureData.getV()));
            signature.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            signature.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
            values.add(RlpString.create(RlpEncoder.encode(new RlpList(signature))));
        }
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }
    String strrpad(int size, String input) {
        int offset = size - input.length();
        char[] inBytes = input.toCharArray();
        char[] out = new char[size];

        for (int i = 0, s = 0; i < size; i++) {
            if (i < input.length()) {
                out[i] = inBytes[i];
            } else {
                out[i] = '\0';
            }
        }
        return new String(out);
    }

    byte[] encodeSendCoin(String to, BigInteger value) {
        List<RlpType> values = new ArrayList<>();
        // coin
        values.add(RlpString.create(strrpad(10, "BIP")));
        // to
        values.add(RlpString.create(Numeric.hexStringToByteArray(to.substring(2))));
        // value
        values.add(RlpString.create(value));
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }
}
