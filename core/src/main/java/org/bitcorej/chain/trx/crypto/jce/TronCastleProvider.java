package org.bitcorej.chain.trx.crypto.jce;

import java.security.Provider;
import java.security.Security;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.bitcorej.chain.trx.crypto.cryptohash.Keccak256;
import org.bitcorej.chain.trx.crypto.cryptohash.Keccak512;

public final class TronCastleProvider {

    public static Provider getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final Provider INSTANCE;

        static {
            Provider p = Security.getProvider("SC");

            INSTANCE = (p != null) ? p : new BouncyCastleProvider();
            INSTANCE.put("MessageDigest.TRON-KECCAK-256", Keccak256.class.getName());
            INSTANCE.put("MessageDigest.TRON-KECCAK-512", Keccak512.class.getName());
        }
    }
}
