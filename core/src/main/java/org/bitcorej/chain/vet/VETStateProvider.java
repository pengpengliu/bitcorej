package org.bitcorej.chain.vet;

import com.vechain.thorclient.core.model.clients.*;
import com.vechain.thorclient.core.model.clients.base.AbstractToken;
import com.vechain.thorclient.utils.BytesUtils;
import com.vechain.thorclient.utils.CryptoUtils;
import com.vechain.thorclient.utils.Prefix;
import com.vechain.thorclient.utils.RawTransactionFactory;
import com.vechain.thorclient.utils.crypto.ECDSASign;
import com.vechain.thorclient.utils.crypto.ECKeyPair;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class VETStateProvider implements ChainState {

    protected byte chainTag;
    protected AbstractToken token;

    public VETStateProvider(Network network) {
        switch (network) {
            case MAIN:
                chainTag = 74;
                break;
            case TEST:
                chainTag = 39;
                break;
        }
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKeyPair ecKeyPair = ECKeyPair.create(secret);
        String address = ecKeyPair.getAddress();
        return new KeyPair(secret, address);
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(NumericUtil.bytesToHex(ECKeyPair.create().getRawPrivateKey()));
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

        if (json.has("token")) {
            JSONObject token = json.getJSONObject("token");
            this.token = ERC20Token.create(token.getString("name"), Address.fromHexString(token.getString("address")), token.getInt("unit"));
        } else {
            this.token = AbstractToken.VET;
        }

        byte chainTag = this.chainTag;
        byte[] blockRef = NumericUtil.hexToBytes(json.getString("blockRef"));

        JSONArray to = json.getJSONArray("to");
        ToClause[] toClauses = new ToClause[to.length()];
        int gas = 5000;
        for (int i = 0; i < to.length(); i++) {
            JSONObject recipient = to.getJSONObject(i);
            String toAddress = recipient.getString("address");
            Amount amount = Amount.createFromToken(this.token);
            amount.setDecimalAmount(recipient.getString("amount"));
            ToClause clause;
            if (json.has("token")) {
                gas += 31518;
                clause = ERC20Contract.buildTranferToClause(
                        (ERC20Token) this.token,
                        Address.fromHexString(toAddress),
                        amount);
            } else {
                gas += 16000;
                clause = new ToClause(Address.fromHexString(toAddress), amount, ToData.ZERO);
            }
            toClauses[i] = clause;
        }
        RawTransaction rawTransaction = RawTransactionFactory.getInstance().createRawTransaction(chainTag, blockRef, 720, gas, (byte) 0x0, CryptoUtils.generateTxNonce(), toClauses);

        // sign tx
        ECDSASign.SignatureData signature = ECDSASign.signMessage(rawTransaction.encode(), ECKeyPair.create(keys.get(0)), true);
        byte[] signBytes = signature.toByteArray();
        rawTransaction.setSignature(signBytes);

        String raw = BytesUtils.toHexString(rawTransaction.encode(), Prefix.ZeroLowerX);

        return "{\"raw\":\"" + raw + "\"}";
    }
}
