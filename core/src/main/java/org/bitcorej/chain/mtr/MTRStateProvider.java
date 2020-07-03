package org.bitcorej.chain.mtr;

import com.vechain.thorclient.utils.BytesUtils;
import com.vechain.thorclient.utils.Prefix;
import com.vechain.thorclient.utils.RLPUtils;
import com.vechain.thorclient.utils.crypto.ECDSASign;
import com.vechain.thorclient.utils.crypto.ECKeyPair;
import com.vechain.thorclient.utils.rlp.*;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.util.List;

public class MTRStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
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
        JSONObject json = new JSONObject(rawTx);
        String hex = json.getString("hex");

        ECDSASign.SignatureData signature = ECDSASign.signMessage(NumericUtil.hexToBytes(hex), ECKeyPair.create(keys.get(0)), true);
        byte[] signBytes = signature.toByteArray();
        // add sign to rlp list
        RlpList rlpList = (RlpList) RlpDecoder.decode(NumericUtil.hexToBytes(hex)).getValues().get(0);
        List values = rlpList.getValues();
        values.add(RlpString.create(signBytes));
        rlpList = new RlpList(values);
        byte[] signedTx = RlpEncoder.encode(rlpList);
        String raw = BytesUtils.toHexString(signedTx, Prefix.ZeroLowerX);
        return "{\"raw\":\"" + raw + "\"}";
    }
}
