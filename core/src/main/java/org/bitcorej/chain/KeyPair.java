package org.bitcorej.chain;


public class KeyPair {
    private final String secretKey;
    private final String publicKey;

    public KeyPair(String secretKey, String publicKey) {
        this.secretKey = secretKey;
        this.publicKey = publicKey;
    }

    public String getSecret() {
        return this.secretKey;
    }

    public String getPublic() {
        return this.publicKey;
    }

    @Override
    public String toString() {
        return this.getPublic() + "\n" + this.getSecret();
    }

    //    private final byte[] privateKey;
//    private final byte[] publicKey;
//    private final String type;

//    // Generates a random keypair
//    public KeyPair(String type) throws Exception {
//        this.type = type;
//        if (type.equals("ed25519")) {
//            java.security.KeyPair keypair = new KeyPairGenerator().generateKeyPair();
//            this.privateKey = keypair.getPrivate().getEncoded();
//            this.publicKey = keypair.getPublic().getEncoded();
//        } else if (type.equals("secp256k1")) {
//            ECKey ecKey = new ECKey();
//            this.privateKey = ecKey.getPrivKeyBytes();
//            this.publicKey = ecKey.getPubKey();
//        } else throw new Exception("Invalid keys type.");
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public byte[] getPrivate() {
//        return privateKey;
//    }
//
//    public byte[] getPublic() {
//        return publicKey;
//    }
//
//    public byte[] sign(byte[] data) throws Exception {
//        if (type.equals("ed25519")) {
//            return new byte[0];
//        } else if (type.equals("secp256k1")) {
//            ECKey ecKey = ECKey.fromPrivate(this.privateKey);
//            return ecKey.sign(Sha256Hash.wrap(data)).encodeToDER();
//        } else throw new Exception("Invalid data.");
//    }

}
