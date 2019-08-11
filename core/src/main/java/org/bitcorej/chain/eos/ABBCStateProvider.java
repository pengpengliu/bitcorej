package org.bitcorej.chain.eos;

import org.bitcorej.chain.KeyPair;

public class ABBCStateProvider extends EOSStateProvider {
    public ABBCStateProvider() {
        super();
        super.chainId = "6c1ead7f71153b1bae506818fbad4ea587abe20548d24e158b7dad7089f28adb";
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        EOSKey eosKey = EOSKey.fromWIF(secret);

        return new KeyPair(secret, eosKey.getPublicKeyAsHex("ABBC"));
    }
}
