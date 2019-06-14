package org.bitcorej.chain.vet;

import com.vechain.thorclient.core.model.clients.*;
import com.vechain.thorclient.utils.BytesUtils;
import com.vechain.thorclient.utils.CryptoUtils;
import com.vechain.thorclient.utils.Prefix;
import com.vechain.thorclient.utils.RawTransactionFactory;
import com.vechain.thorclient.utils.crypto.ECDSASign;
import com.vechain.thorclient.utils.crypto.ECKeyPair;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.util.List;

public class VETERC20StateProvider extends VETStateProvider {
    public void setProperties(String name, String address, int decimals) {
        super.token = ERC20Token.create(name, Address.fromHexString(address), decimals);
    }

    public VETERC20StateProvider() {
        super(Network.MAIN);
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        JSONObject json = new JSONObject(rawTx);

        byte chainTag = super.chainTag;
        byte[] blockRef = NumericUtil.hexToBytes(json.getString("blockRef"));

        Amount amount = Amount.createFromToken(super.token);
        amount.setDecimalAmount(json.getString("amount"));

        String toAddress = json.getString("to");
        ToClause clause = ERC20Contract.buildTranferToClause(
                (ERC20Token)super.token,
                Address.fromHexString(toAddress),
                amount);
        RawTransaction rawTransaction = RawTransactionFactory.getInstance().createRawTransaction(chainTag, blockRef, 720, 60000, (byte) 0x0, CryptoUtils.generateTxNonce(), clause);

        // sign tx
        ECDSASign.SignatureData signature = ECDSASign.signMessage(rawTransaction.encode(), ECKeyPair.create(keys.get(0)), true);
        byte[] signBytes = signature.toByteArray();
        rawTransaction.setSignature(signBytes);

        String raw = BytesUtils.toHexString(rawTransaction.encode(), Prefix.ZeroLowerX);

        return "{\"raw\":\"" + raw + "\"}";
    }
}
