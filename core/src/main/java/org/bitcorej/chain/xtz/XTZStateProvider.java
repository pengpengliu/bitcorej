package org.bitcorej.chain.xtz;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.util.List;

public class XTZStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        try {
            String publicKeyHash = TezosWalletUtil.generatePublicKeyHash(secret);
            return new KeyPair(secret, publicKeyHash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            String mnemonic = TezosWalletUtil.generateMnemonic();
            String publicKeyHash = TezosWalletUtil.generatePublicKeyHash(mnemonic);
            return new KeyPair(mnemonic, publicKeyHash);
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
            JSONObject forgedBytes = new JSONObject(rawTx);

            String opbytes = forgedBytes.getString("opbytes");
            String mnemonic = keys.get(0);
            JSONObject signed = TezosWalletUtil.sign(NumericUtil.hexToBytes(opbytes), "03", TezosWalletUtil.generatePrivateKeyBytes(mnemonic));

            forgedBytes.put("opbytes", signed.getString("sbytes"));

            JSONObject opOb = forgedBytes.getJSONObject("opOb");
            opOb.put("signature", signed.getString("edsig"));

            forgedBytes.put("opOb", opOb);
            return forgedBytes.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
