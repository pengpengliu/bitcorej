package org.bitcorej.chain.eos;

import org.bitcorej.chain.KeyPair;

public class YTAStateProvider extends EOSStateProvider {
    public YTAStateProvider() {
        super();
        super.chainId = "9d7bec4bf167a7b136d0b45d8aac77bd45e761e35cbd2b7d0e88dfe05ebf3d62";
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        EOSKey eosKey = EOSKey.fromWIF(secret);

        return new KeyPair(secret, eosKey.getPublicKeyAsHex("YTA"));
    }
}
