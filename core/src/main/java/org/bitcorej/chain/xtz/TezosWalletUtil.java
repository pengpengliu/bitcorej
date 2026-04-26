package org.bitcorej.chain.xtz;

import com.goterl.lazysodium.SodiumJava;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class TezosWalletUtil {

    final static byte[] tz1Prefix =
            { (byte) 6, (byte) 161, (byte) 159 };

    static String generateMnemonic() throws Exception {
        MnemonicCode mc = new MnemonicCode();
        byte[] bytes = new byte[20];
        new java.util.Random().nextBytes(bytes);
        ArrayList<String> code = (ArrayList<String>) mc.toMnemonic(bytes);

        String strMessage = "";
        strMessage = (String) code.toString();

        // Cleans undesired characters from mnemonic words.
        String cleanMnemonic = strMessage.replace("[", "");
        cleanMnemonic = cleanMnemonic.replace("]", "");
        cleanMnemonic = cleanMnemonic.replace(",", " ");
        cleanMnemonic = cleanMnemonic.replace("  ", " ");

        return cleanMnemonic;
    }

    static byte[] generatePrivateKeyBytes(String mnemonic) {
        byte[] src_seed = MnemonicCode.toSeed(Arrays.asList(mnemonic.split(" ")), "");
        byte[] seed = Arrays.copyOfRange(src_seed, 0, 32);
        SodiumJava sodium = new SodiumJava();

        byte[] sodiumPrivateKey = new byte[32 * 2];
        byte[] sodiumPublicKey = new byte[32];
        sodium.crypto_sign_seed_keypair(sodiumPublicKey, sodiumPrivateKey, seed);
        byte[] genericHash = new byte[20];
        sodium.crypto_generichash(genericHash, genericHash.length, sodiumPublicKey, sodiumPublicKey.length,
                sodiumPublicKey, 0);
        return sodiumPrivateKey;
    }

    static String generatePublicKeyHash(String mnemonic) {
        byte[] src_seed = MnemonicCode.toSeed(Arrays.asList(mnemonic.split(" ")), "");
        byte[] seed = Arrays.copyOfRange(src_seed, 0, 32);
        SodiumJava sodium = new SodiumJava();

        byte[] sodiumPrivateKey = new byte[32 * 2];
        byte[] sodiumPublicKey = new byte[32];
        sodium.crypto_sign_seed_keypair(sodiumPublicKey, sodiumPrivateKey, seed);
        byte[] genericHash = new byte[20];
        sodium.crypto_generichash(genericHash, genericHash.length, sodiumPublicKey, sodiumPublicKey.length,
                sodiumPublicKey, 0);

        byte[] prefixedGenericHash = new byte[23];
        System.arraycopy(tz1Prefix, 0, prefixedGenericHash, 0, 3);
        System.arraycopy(genericHash, 0, prefixedGenericHash, 3, 20);

        byte[] firstFourOfDoubleChecksum = hashTwiceThenFirstFourOnly(prefixedGenericHash);
        byte[] prefixedPKhashWithChecksum = new byte[27];
        System.arraycopy(prefixedGenericHash, 0, prefixedPKhashWithChecksum, 0, 23);
        System.arraycopy(firstFourOfDoubleChecksum, 0, prefixedPKhashWithChecksum, 23, 4);

        return Base58.encode(prefixedPKhashWithChecksum);
    }

    static byte[] hashTwiceThenFirstFourOnly(byte[] input) {
        byte[] firstFourOfDoubleChecksum = new byte[4];
        byte[] sha256DoubleHash = Sha256Hash.hashTwice(input);
        System.arraycopy(sha256DoubleHash, 0, firstFourOfDoubleChecksum, 0, 4);
        return firstFourOfDoubleChecksum;
    }

    static JSONObject sign(byte[] bytes, String watermark, byte[] privateKeyBytes) throws Exception {
        byte[] workBytes = ArrayUtils.addAll(bytes);
        if(watermark != null)
        {
            byte[] wmBytes = NumericUtil.hexToBytes(watermark);
            workBytes = ArrayUtils.addAll(wmBytes, workBytes);
        }

        // Now we hash the combination of: watermark (if exists) + the bytes passed in
        // parameters.
        // The result will end up in the sig variable.
        byte[] hashedWorkBytes = new byte[32];

        SodiumJava sodium = new SodiumJava();
        sodium.crypto_generichash(hashedWorkBytes, hashedWorkBytes.length, workBytes, workBytes.length,
                workBytes, 0);

        byte[] sig = new byte[64];
        sodium.crypto_sign_detached(sig, null, hashedWorkBytes, hashedWorkBytes.length, privateKeyBytes);

        // To create the edsig, we need to concatenate the edsig prefix with the sig and
        // then encode it.
        // The sbytes will be the concatenation of bytes (in hex) + sig (in hex).
        byte[] edsigPrefix =
                { 9, (byte) 245, (byte) 205, (byte) 134, 18 };
        byte[] edsigPrefixedSig = new byte[edsigPrefix.length + sig.length];
        edsigPrefixedSig = ArrayUtils.addAll(edsigPrefix, sig);
        String edsig = Base58Check.encode(edsigPrefixedSig);
        String sbytes = NumericUtil.bytesToHex(bytes) + NumericUtil.bytesToHex(sig);

        // Now, with all needed values ready, we create and deliver the response.
        JSONObject response = new JSONObject();
        response.put("bytes", NumericUtil.bytesToHex(bytes));
        response.put("sig", NumericUtil.bytesToHex(sig));
        response.put("edsig", edsig);
        response.put("sbytes", sbytes);

        return response;
    }
}
