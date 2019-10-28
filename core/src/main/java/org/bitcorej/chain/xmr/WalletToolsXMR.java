package org.bitcorej.chain.xmr;

import org.bitcorej.chain.xmr.core.Utils;
import org.bitcorej.chain.xmr.core.common.Keccak256;
import org.bitcorej.chain.xmr.core.ed25519.Ed25519Constants;
import org.bitcorej.chain.xmr.core.key.KeyFactory;
import org.bitcorej.chain.xmr.core.key.PrivateKey;
import org.bitcorej.chain.xmr.core.key.PublicKey;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class WalletToolsXMR {


    public String generateSeedMnemonicSeparatedBySpaces() {
        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            BigInteger val = Utils.randomBigInt(prng, Ed25519Constants.L);
            //seed should be already reduced no need to reduce it
            byte[] entropy = Utils.as256BitLe(val);
            String seedWords = XMRMnemonicUtility.toMnemonic(entropy);
            return seedWords;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] generateShortAndLongSeedMnemonicsSeparatedBySpaces() {
        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            Keccak256 keccak256 = new Keccak256();
            byte[] digest;
            byte[] longSeed;
            byte[] shortSeed = new byte[16];
            do {
                prng.nextBytes(shortSeed);
                keccak256.reset();
                keccak256.update(shortSeed);
                digest = keccak256.digest().array();
                longSeed = Utils.sc_reduce32(digest);
            }while(!Arrays.equals(digest,longSeed)); //we need to bruteforce this

            String shortMnemonic = XMRMnemonicUtility.toMnemonic(shortSeed);
            String longMnemonic = XMRMnemonicUtility.toMnemonic(longSeed);
            return new String[]{shortMnemonic,longMnemonic};
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Account getAccount(String seedMnemonicSeparatedBySpaces, int index) {
        try {
            //clean
            seedMnemonicSeparatedBySpaces = seedMnemonicSeparatedBySpaces.trim().replace("\n","");
            if (seedMnemonicSeparatedBySpaces.contains(":")) {
                //remove leading protocol
                seedMnemonicSeparatedBySpaces = seedMnemonicSeparatedBySpaces.substring(seedMnemonicSeparatedBySpaces.indexOf(":") + 1);
            }

            //remove leading slashes
            if (seedMnemonicSeparatedBySpaces.startsWith("//")) {
                seedMnemonicSeparatedBySpaces = seedMnemonicSeparatedBySpaces.substring("//".length());
            }

            //remove things after
            if (seedMnemonicSeparatedBySpaces.contains("?")) {
                seedMnemonicSeparatedBySpaces = seedMnemonicSeparatedBySpaces.substring(0,seedMnemonicSeparatedBySpaces.indexOf("?"));
            }

            byte[] seed = XMRMnemonicUtility.toEntropy(seedMnemonicSeparatedBySpaces);
            if (seed != null) {

                if (seed.length != 256/8) { //if seed is not long enough make it 256bit (MyMonero case)
                    Keccak256 keccak256 = new Keccak256();
                    keccak256.update(seed);
                    seed = keccak256.digest().array();
                }

                byte[] b = Utils.sc_reduce32(seed); //just to be sure (seed should be already valid)
                Keccak256 keccak256 = new Keccak256();
                keccak256.update(seed);
                byte[] a = Utils.sc_reduce32(keccak256.digest().array()); //calculate viewkey

                PrivateKey secretA = null;
                PublicKey publicA = null;
                PrivateKey secretB = null;
                PublicKey publicB = null;
                try {
                    KeyFactory keyFactory = new KeyFactory();
                    secretA = keyFactory.decodePrivateKey(a);
                    publicA = keyFactory.generatePublicKey(secretA);
                    secretB = keyFactory.decodePrivateKey(b);
                    publicB = keyFactory.generatePublicKey(secretB);

                    if (index != 0) {
                        //derive another account
                        //TODO: I hope some day I will have time to implement this subaddress derivation.
                    }

                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                return new Account(seedMnemonicSeparatedBySpaces, seed, secretB, secretA, publicB, publicA, index);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAddressValid(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        Address parsed = Address.parse(address);
        return parsed != null;
    }

}