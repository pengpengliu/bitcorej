package org.bitcorej.chain.ripple;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.core.PublicKey;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;

import java.util.Arrays;
import java.util.List;

public class RippleStateProvider implements ChainState {

    private String createAddress(PublicKey pubKey) {
        byte[] pubKeyData = pubKey.getRaw();

        SHA256Digest pubKeyInnerHash = new SHA256Digest();
        pubKeyInnerHash.update(pubKeyData, 0, pubKeyData.length);
        byte[] pubKeyInnerBytes = new byte[pubKeyInnerHash.getDigestSize()];
        pubKeyInnerHash.doFinal(pubKeyInnerBytes, 0);

        RIPEMD160Digest pubKeyOuterHash = new RIPEMD160Digest();
        pubKeyOuterHash.update(pubKeyInnerBytes, 0, pubKeyInnerHash.getDigestSize());
        byte[] accountId = new byte[pubKeyOuterHash.getDigestSize()];
        pubKeyOuterHash.doFinal(accountId, 0);

        byte[] addressTypePrefix = NumericUtil.hexToBytes("0x00");
        byte[] payload = ByteUtil.concat(addressTypePrefix, accountId);
        SHA256Digest checksum1 = new SHA256Digest();
        checksum1.update(payload, 0, payload.length);
        byte[] checksum1Bytes = new byte[checksum1.getDigestSize()];
        checksum1.doFinal(checksum1Bytes, 0);

        SHA256Digest checksum2 = new SHA256Digest();
        checksum2.update(checksum1Bytes, 0, checksum1Bytes.length);
        byte[] out = new byte[checksum2.getDigestSize()];
        checksum2.doFinal(out, 0);
        byte[] checksum2Bytes = Arrays.copyOfRange(out, 0, 4);
        return Base58.encode(ByteUtil.concat(payload, checksum2Bytes));
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        return null;
    }

    @Override
    public KeyPair generateKeyPair() {
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        return null;
    }
}
