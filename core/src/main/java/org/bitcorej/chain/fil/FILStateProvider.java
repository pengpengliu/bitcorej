package org.bitcorej.chain.fil;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.vsys.HashUtil;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Base64;
import java.util.List;

public class FILStateProvider implements ChainState {
    String prefix = "f";

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret), false);
        byte[] publicKey = ecKey.getPubKey();
        byte[] protocolByte = new byte[]{1};
        byte[] payload = HashUtil.hashB160(publicKey);
        byte[] checksum = HashUtil.hashB(ByteUtil.concat(protocolByte, payload), 32);
        return new KeyPair(secret, prefix + "1" + Base32.encode(ByteUtil.concat(payload, checksum)));
    }

    @Override
    public KeyPair generateKeyPair() {
        ECKey ecKey = new ECKey();
        return generateKeyPair(ecKey.getPrivateKeyAsHex());
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

        String messageWithParamsHexValue =  rawTxJSON.getString("MessageWithParamsHexValue"); //"89005501e6734efa6a8e0f896d3fe8c7cc44d5fd0cc25bc555011a426debaf05e08385306b79c762df09a12bdb060149008ac7230489e8000049000de0b6b3a76400001903e80040";
        byte[] cidPrefix = NumericUtil.hexToBytes("0171a0e40220");
        byte[] hash = HashUtil.hashB(NumericUtil.hexToBytes(messageWithParamsHexValue), 256);
        byte[] cid = ByteUtil.concat(cidPrefix, hash);
        byte[] toSign = HashUtil.hashB(cid, 256);

        ECKey key = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(0)), false);
        byte[] pub = key.getPubKey();
        ECKey.ECDSASignature sig = key.sign(Sha256Hash.wrap(toSign));

        int recId = -1;
        for (int i = 0; i < 4; i++) {
            ECKey k = ECKey.recoverFromSignature(i, sig, Sha256Hash.wrap(toSign), false);
            if (k != null && k.getPublicKeyAsHex().equals(NumericUtil.bytesToHex(pub))) {
                recId = i;
                break;
            }
        }

        byte[] signature = ByteUtil.concat(sig.r.toByteArray(), sig.s.toByteArray());
        signature = ByteUtil.concat(signature, BigInteger.valueOf(recId).toByteArray());
        JSONObject signatureJSON = new JSONObject();
        signatureJSON.put("Type", 1);
        signatureJSON.put("Data", Base64.getEncoder().encodeToString(signature));
        rawTxJSON.put("Signature", signatureJSON);
        rawTxJSON.remove("MessageWithParamsHexValue");
        return rawTxJSON.toString();
    }
}
