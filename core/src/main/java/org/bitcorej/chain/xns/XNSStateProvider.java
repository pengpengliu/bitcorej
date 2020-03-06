package org.bitcorej.chain.xns;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.math.ec.ECPoint;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class XNSStateProvider implements ChainState {

    private static final String SPEC = "secp256r1";
    private static final String ALGO = "SHA256withECDSA";

    private static ECPrivateKey calECPrivateKey(String secret) {
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec(SPEC));

            ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

            ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(secret, 16), ecParameterSpec);
            ECPrivateKey privateKey = (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(ecPrivateKeySpec);
            return privateKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ECPublicKey calPublicKey(String secret) {
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec(SPEC));

            ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

            ECPrivateKey privateKey = calECPrivateKey(secret);

            org.bouncycastle.jce.spec.ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(SPEC);

            ECPoint Q = ecSpec.getG().multiply(privateKey.getS());
            byte[] publicKeyBytes = Q.getEncoded(false);
            byte[] X = Arrays.copyOfRange(publicKeyBytes, 1, 33);
            byte[] Y = Arrays.copyOfRange(publicKeyBytes, 33, 65);

            java.security.spec.ECPoint W = new java.security.spec.ECPoint(new BigInteger(X), new BigInteger(Y));

            ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(W, ecParameterSpec);
            ECPublicKey publicKey = (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(ecPublicKeySpec);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        try {
            ECPrivateKey privateKey = calECPrivateKey(secret);

            org.bouncycastle.jce.spec.ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(SPEC);

            ECPoint Q = ecSpec.getG().multiply(privateKey.getS());
            byte[] publicKeyBytes = Q.getEncoded(false);
            return new KeyPair(secret, NumericUtil.bytesToHex(publicKeyBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generateCreateMemberReq(String seed, String secret) {
        try {
            ECPrivateKey privateKey = calECPrivateKey(secret);
            ECPublicKey publicKey = calPublicKey(secret);
            String b64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String plaintext = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"contract.call\",\"params\":{\"seed\":\"" + seed + "\",\"callSite\":\"member.create\",\"callParams\":{},\"publicKey\":\"-----BEGIN PUBLIC KEY-----\\n" + b64.substring(0, 64) + "\\n" + b64.substring(64) + "\\n-----END PUBLIC KEY-----\\n\"}}";
            Signature ecdsaSign = Signature.getInstance(ALGO);
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] signature = ecdsaSign.sign();
            return "keyId=\"member-pub-key\", algorithm=\"ecdsa\", headers=\"digest\", signature=" + Base64.getEncoder().encodeToString(signature) + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
            g.initialize(ecSpec, new SecureRandom());
            String secret = NumericUtil.bytesToHex(ByteUtil.trimLeadingZeroes(((ECPrivateKey)g.generateKeyPair().getPrivate()).getS().toByteArray()));
            return generateKeyPair(secret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Boolean validateTx(String rawTx, String requestTx) {
        return null;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        try {
            String secret = keys.get(0);
            ECPrivateKey privateKey = calECPrivateKey(secret);
            ECPublicKey publicKey = calPublicKey(secret);
            String b64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String plaintext = rawTx.replace("<publicKey>", "-----BEGIN PUBLIC KEY-----\\n" + b64.substring(0, 64) + "\\n" + b64.substring(64) + "\\n-----END PUBLIC KEY-----\\n");
            Signature ecdsaSign = Signature.getInstance(ALGO);
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(plaintext.getBytes(StandardCharsets.UTF_8));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] signature = ecdsaSign.sign();

            String hash64 = Base64.getEncoder().encodeToString(encodedhash);
            String signature64 = Base64.getEncoder().encodeToString(signature);

            System.out.println("plaintext: " + plaintext);
            System.out.println("hash64: " + hash64);
            System.out.println("signature64: " + signature64);

            JSONObject json = new JSONObject();
            json.put("payload", plaintext);
            json.put("hash", Base64.getEncoder().encodeToString(encodedhash));
            json.put("signature", Base64.getEncoder().encodeToString(signature));
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
