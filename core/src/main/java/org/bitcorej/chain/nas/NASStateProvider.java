package org.bitcorej.chain.nas;

import io.nebulas.core.Address;
import io.nebulas.core.TransactionBinaryPayload;
import io.nebulas.crypto.Crypto;
import io.nebulas.crypto.keystore.Algorithm;
import io.nebulas.crypto.keystore.PrivateKey;
import io.nebulas.crypto.keystore.Signature;
import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.math.BigInteger;
import java.util.List;

public class NASStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        try {
            PrivateKey privateKey = Crypto.NewPrivateKey(Algorithm.SECP256K1, ecKey.getPrivKeyBytes());
            String address = Address.NewAddressFromPubKey(privateKey.publickey().encode()).string();
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

        try {
            int chainID = jsonObject.getInt("chainID");
            Address from = Address.ParseFromString(jsonObject.getString("from"));
            Address to = Address.ParseFromString(jsonObject.getString("to"));
            Long nonce = jsonObject.getLong("nonce");
            BigInteger gasPrice = new BigInteger(jsonObject.getString("gasPrice"));
            BigInteger gasLimit = new BigInteger(jsonObject.getString("gasLimit"));
            BigInteger value = new BigInteger(jsonObject.getString("value"));

            io.nebulas.core.Transaction tx =  new io.nebulas.core.Transaction(chainID, from, to, value, nonce, io.nebulas.core.Transaction.PayloadType.BINARY, new TransactionBinaryPayload(null).toBytes(), gasPrice, gasLimit);
            Signature signature = Crypto.NewSignature(Algorithm.SECP256K1);
            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(0)));
            signature.initSign(Crypto.NewPrivateKey(Algorithm.SECP256K1, ecKey.getPrivKeyBytes()));
            tx.sign(signature);
            return Base64.toBase64String(tx.toProto());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
