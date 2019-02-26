package org.bitcorej.chain.ethereum;

import org.bitcorej.chain.ChainState;
import org.bitcorej.core.PrivateKey;
import org.web3j.crypto.Credentials;

public class EthereumStateProvider implements ChainState {
    @Override
    public String getAddress(PrivateKey privKey) {
        return Credentials.create(privKey.toString()).getAddress();
    }
}
