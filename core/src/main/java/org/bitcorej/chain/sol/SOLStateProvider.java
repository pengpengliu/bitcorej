package org.bitcorej.chain.sol;

import iost.crypto.Base58;
import iost.crypto.Ed25519;
import iost.model.transaction.Signature;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class SOLStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        Ed25519 keyPair = new Ed25519(NumericUtil.hexToBytes(secret));
        return new KeyPair(NumericUtil.bytesToHex(keyPair.seckey()), keyPair.B58PubKey());
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(NumericUtil.bytesToHex(new Ed25519().seckey()));
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
        JSONObject json = new JSONObject(rawTx);
        String raw = json.getString("raw");
        String signData = json.getString("signData");
        Ed25519 keyPair = new Ed25519(NumericUtil.hexToBytes(keys.get(0)));
        Signature sig = keyPair.sign(NumericUtil.hexToBytes(signData));
        String signature = NumericUtil.bytesToHex(sig.signature);
        raw = raw.replace("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", signature);
        JSONObject packedTx = new JSONObject();
        packedTx.put("raw", Base58.encode(NumericUtil.hexToBytes(raw)));
        return packedTx.toString();
    }

}
