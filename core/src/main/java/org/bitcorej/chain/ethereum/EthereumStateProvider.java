package org.bitcorej.chain.ethereum;

import org.bitcorej.chain.ChainState;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

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
        return null;
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, List<PrivateKey> keys) {
        return new byte[0];
    }
}
