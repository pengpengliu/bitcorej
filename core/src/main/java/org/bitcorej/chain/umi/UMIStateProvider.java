package org.bitcorej.chain.umi;

import iost.crypto.Ed25519;
import iost.model.transaction.Signature;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.crypto.Bech32;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.util.List;

public class UMIStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        Ed25519 keyPair = new Ed25519(NumericUtil.hexToBytes(secret));
        return new KeyPair(NumericUtil.bytesToHex(keyPair.seckey()), Bech32.encode("umi", Bech32.toWords(keyPair.pubkey())));
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
        String raw = json.getString("serialized");
        String signData = json.getString("signatureHash");
        Ed25519 keyPair = new Ed25519(NumericUtil.hexToBytes(keys.get(0)));
        Signature sig = keyPair.sign(NumericUtil.hexToBytes(signData));
        String signature = NumericUtil.bytesToHex(sig.signature);
        JSONObject packedTx = new JSONObject();
        packedTx.put("serialized", raw);
        packedTx.put("signature", signature);
        return packedTx.toString();
    }
}
