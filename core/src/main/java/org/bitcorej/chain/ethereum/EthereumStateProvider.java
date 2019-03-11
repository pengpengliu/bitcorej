package org.bitcorej.chain.ethereum;

import org.bitcorej.chain.ChainState;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;
import org.bitcorej.utils.NumericUtil;
import org.web3j.crypto.*;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

public class EthereumStateProvider implements ChainState {
    @Override
    public String createAddress(PrivateKey privKey) {
        return Numeric.prependHexPrefix(Keys.getAddress(Sign.publicKeyFromPrivate(privKey.toBigInteger())));
    }

    @Override
    public String createAddress(PublicKey pubKey) {
        return Numeric.prependHexPrefix(Keys.getAddress(pubKey.toBigInteger()));
    }

    @Override
    public String createAddress(List<PublicKey> publicKeys) {
        if ( publicKeys.size() == 1 ) {
            return this.createAddress(publicKeys.get(0));
        }
        return null;
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, List<PrivateKey> keys) {
        RawTransaction tx;
        byte[] transaction = Numeric.hexStringToByteArray(NumericUtil.bytesToHex(rawTx));
        RlpList rlpList = RlpDecoder.decode(transaction);
        RlpList values = (RlpList) rlpList.getValues().get(1);
        BigInteger nonce = ((RlpString) values.getValues().get(0)).asPositiveBigInteger();
        BigInteger gasPrice = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        BigInteger gasLimit = ((RlpString) values.getValues().get(2)).asPositiveBigInteger();
        String to = ((RlpString) values.getValues().get(3)).asString();
        BigInteger value = ((RlpString) values.getValues().get(4)).asPositiveBigInteger();
        String data = ((RlpString) values.getValues().get(5)).asString();
        if (values.getValues().size() > 6) {
            byte v = ((RlpString) values.getValues().get(6)).getBytes()[0];
            byte[] r = Numeric.toBytesPadded(
                    Numeric.toBigInt(((RlpString) values.getValues().get(7)).getBytes()), 32);
            byte[] s = Numeric.toBytesPadded(
                    Numeric.toBigInt(((RlpString) values.getValues().get(8)).getBytes()), 32);
            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            tx = new SignedRawTransaction(nonce, gasPrice, gasLimit,
                    to, value, data, signatureData);
        } else {
            tx = RawTransaction.createTransaction(nonce,
                    gasPrice, gasLimit, to, value, data);
        }
        return TransactionEncoder.signMessage(tx, Credentials.create(keys.get(0).toString()));
    }
}
