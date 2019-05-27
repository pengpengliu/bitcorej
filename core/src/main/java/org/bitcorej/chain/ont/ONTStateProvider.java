package org.bitcorej.chain.ont;

import com.github.ontio.OntSdk;
import com.github.ontio.account.Account;
import com.github.ontio.core.asset.Sig;
import com.github.ontio.crypto.SignatureScheme;
import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.util.List;

public class ONTStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));

        try {
            String address = new Account(ecKey.getPrivKeyBytes(), SignatureScheme.SHA256WITHECDSA).getAddressU160().toBase58();
            return new KeyPair(ecKey.getPrivateKeyAsHex(), address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        String sender = jsonObject.getString("sender");
        String recvAddr = jsonObject.getString("recvAddr");
        long amount = jsonObject.getLong("amount");
        String payer = jsonObject.getString("payer");
        long gasLimit = jsonObject.getLong("gasLimit");
        long gasPrice = jsonObject.getLong("gasPrice");
        try {
            com.github.ontio.core.transaction.Transaction tx = OntSdk.getInstance().nativevm().ont().makeTransfer(sender, recvAddr, amount, payer, gasLimit, gasPrice);
            System.out.println(tx.json());
            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(0)));
            Account account = new Account(ecKey.getPrivKeyBytes(), SignatureScheme.SHA256WITHECDSA);
            byte[] signature = tx.sign(account, SignatureScheme.SHA256WITHECDSA);
            Sig sig = new Sig();
            sig.M = 0;
            sig.pubKeys = new byte[][] {account.serializePublicKey()};
            sig.sigData = new byte[][] {signature};
            tx.sigs = new Sig[] {sig};
            return "{\"data\":\"" + tx.toHexString() + "\"}";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}