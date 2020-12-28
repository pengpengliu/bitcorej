package org.bitcorej.chain.stellar;

import org.bitcorej.chain.sol.SOLStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.stellar.sdk.*;

import java.math.BigDecimal;
import java.util.List;

public class KINStateProvider extends StellarStateProvider {
    public KINStateProvider(Network network) {
        super(network);
    }

    public static org.bitcorej.chain.KeyPair toSol(String secret) {
        char[] charSeed = secret.toCharArray();
        byte[] decoded = StrKey.decodeStellarSecretSeed(charSeed);
        return new SOLStateProvider().generateKeyPair(NumericUtil.bytesToHex(decoded));
    }

    @Override
    protected void switchNetwork() {
        org.stellar.sdk.Network.use(new org.stellar.sdk.Network("Kin Mainnet ; December 2018"));
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        switchNetwork();
        JSONObject jsonObject = new JSONObject(rawTx);
        String to = jsonObject.getString("to");
        String type = jsonObject.getString("type");
        Long sequence = Long.parseLong(jsonObject.getString("sequence"));
        int fee = jsonObject.getInt("fee");
        String memo = jsonObject.getString("memo");
        // The KIN decimals is 5 (XLM is 7)
        BigDecimal amount = new BigDecimal(jsonObject.getString("amount")).divide(new BigDecimal(100));
        org.stellar.sdk.KeyPair source = org.stellar.sdk.KeyPair.fromSecretSeed(keys.get(0));
        org.stellar.sdk.KeyPair destination = org.stellar.sdk.KeyPair.fromAccountId(to);
        Asset asset = new AssetTypeNative();
        Operation op;
        if (type.equals("payment")) {
            op = new PaymentOperation.Builder(destination, asset, amount.toString()).build();
        } else if (type.equals("create_account")) {
            op = new CreateAccountOperation.Builder(destination, amount.toString()).build();
        } else {
            throw new RuntimeException("no sup op");
        }
        org.stellar.sdk.Transaction transaction = new org.stellar.sdk.Transaction.Builder(new Account(source, sequence))
                .addOperation(op)
                .setOperationFee(fee)
                .addMemo(Memo.text(memo))
                .setTimeout(0)
                .build();
        transaction.sign(source);
        JSONObject packedTx = new JSONObject();
        packedTx.put("XDR", transaction.toEnvelopeXdrBase64());
        return packedTx.toString();
    }
}
