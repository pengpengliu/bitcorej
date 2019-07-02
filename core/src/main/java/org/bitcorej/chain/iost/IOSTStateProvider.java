package org.bitcorej.chain.iost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iost.crypto.Base58;
import iost.crypto.Ed25519;
import iost.model.transaction.Action;
import iost.model.transaction.Signature;
import iost.model.transaction.SignatureAdapter;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class IOSTStateProvider implements ChainState {

    @Override
    public KeyPair generateKeyPair(String secret) {
        try {
            Ed25519 keyPair = new Ed25519(Base58.decode(secret));
            return new KeyPair(keyPair.B58SecKey(), keyPair.B58PubKey());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new Ed25519().B58SecKey());
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
        iost.model.transaction.Transaction tx = (new Gson()).fromJson(rawTx, iost.model.transaction.Transaction.class);
        try {
            Ed25519 keyPair = new Ed25519(Base58.decode(keys.get(0)));
            tx.publisher_sigs.add(keyPair.sign(tx.getPublishHash()));
            tx.publisher = json.getString("publisher");

            List<Action> actions = tx.actions;
            assert actions.size() == 1;
            assert actions.get(0).contract.equals("token.iost");
            assert actions.get(0).action_name.equals("transfer");

            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(Signature.class, new SignatureAdapter());
            return gb.create().toJson(tx);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
