package org.bitcorej.chain.ethereum;

import org.bitcorej.chain.ChainState;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.util.ArrayList;

public class EthereumStateProvider implements ChainState {
    @Override
    public String getAddress(PrivateKey privKey) {
        return Numeric.prependHexPrefix(Keys.getAddress(Sign.publicKeyFromPrivate(privKey.toBigInteger())));
    }

    @Override
    public String getAddress(PublicKey pubKey) {
        return Numeric.prependHexPrefix(Keys.getAddress(pubKey.toBigInteger()));
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, ArrayList<byte[]> keys) {
        return new byte[0];
    }
}
