package org.bitcorej.chain.gxc;

import com.google.common.primitives.Bytes;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.bouncycastle.crypto.digests.SHA256Digest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Crypto {
    public static byte[] encryptMessage(ECKey privateKey, ECKey publicKey, BigInteger nonce, String message) {
        byte[] encrypted = null;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // Getting nonce bytes
            String stringNonce = nonce.toString();
            byte[] nonceBytes = Arrays.copyOfRange(ByteUtil.hexlify(stringNonce), 0, stringNonce.length());

            // Getting shared secret
            byte[] secret = publicKey.getPubKeyPoint().multiply(privateKey.getPrivKey()).normalize().getXCoord().getEncoded();

            // SHA-512 of shared secret
            byte[] ss = sha512.digest(secret);

            byte[] seed = Bytes.concat(nonceBytes, ByteUtil.hexlify(NumericUtil.bytesToHex(ss)));

            // Calculating checksum
            byte[] sha256Msg = sha256.digest(message.getBytes());
            byte[] checksum = Arrays.copyOfRange(sha256Msg, 0, 4);

            // Concatenating checksum + message bytes
            byte[] msgFinal = Bytes.concat(checksum, message.getBytes());

            // Applying encryption
            encrypted = ByteUtil.encryptAES(msgFinal, seed);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return encrypted;
    }

    public static byte[] signature(byte[] data, ECKey ecKey) {
        GxcPrivateKey gxcPrivateKey = new GxcPrivateKey(WIF.fromPrivateKey(ecKey.getPrivateKeyAsHex()).toString());
        Sha256 msg = Sha256.from(data);
        return gxcPrivateKey.sign(msg).encoding(true);
    }
}
