package org.bitcorej.chain.ripple;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.crypto.ecdsa.Seed;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

public class RippleStateProvider implements ChainState {
    private static final BigDecimal DECIMALS = new BigDecimal(10).pow(6);

    private static final BigDecimal MAX_FEE = new BigDecimal("50").multiply(DECIMALS);

    @Override
    public KeyPair generateKeyPair(String secret) {
        return new KeyPair(secret, AccountID.fromKeyPair(Seed.fromBase58(secret).keyPair()).toString());
    }

    @Override
    public KeyPair generateKeyPair() {
        SecureRandom random = new SecureRandom();
        byte[] seedBytes = new byte[16];
        random.nextBytes(seedBytes);
        Seed seed = new Seed(seedBytes);
        return generateKeyPair(seed.toString());
    }

    @Override
    public Boolean validateTx(String rawTx, String tx) {
        Transaction decodedTx = this.decodeRawTransaction(rawTx);
        return decodedTx.equals(new Transaction(tx)) && decodedTx.getFee().compareTo(MAX_FEE) < 0;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        JSONObject jsonObject = new JSONObject(rawTx);

        Transaction tx = new Transaction();
        JSONObject amount = jsonObject.getJSONObject("Amount");
        tx.addInput(tx.new Input(jsonObject.getString("Account"), new BigDecimal(amount.getString("value"))));
        tx.addOutput(tx.new Output(jsonObject.getString("Destination"), new BigDecimal(amount.getString("value")), jsonObject.getString("DestinationTag")));
        tx.setFee(new BigDecimal(jsonObject.getString("Fee")));
        return tx;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        Payment payment = new Payment();

        JSONObject jsonObject = new JSONObject(rawTx);

        payment.as(AccountID.Account, jsonObject.getString("Account"));
        payment.as(AccountID.Destination, jsonObject.getString("Destination"));
        payment.as(UInt32.DestinationTag, jsonObject.getString("DestinationTag"));
        payment.as(Amount.Amount, new BigDecimal(jsonObject.getJSONObject("Amount").getString("value")).multiply(DECIMALS).toString());
        payment.as(UInt32.Sequence, jsonObject.getInt("Sequence"));
        payment.as(UInt32.LastLedgerSequence, jsonObject.getInt("LastLedgerSequence"));
        payment.as(Amount.Fee, jsonObject.getString("Fee"));
        SignedTransaction signed = payment.sign(keys.get(0));
        if (signed != null) {
            JSONObject packedTx = new JSONObject();
            packedTx.put("hex", signed.tx_blob);
            return packedTx.toString();
        }
        return null;
    }
}
