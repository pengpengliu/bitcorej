package org.bitcorej.chain.eos;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;
import org.bitcorej.utils.ByteUtil;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.util.Arrays;
import java.util.List;

public class EOSStateProvider implements ChainState {
    @Override
    public String createAddress(PrivateKey privKey) {
        return null;
    }

    @Override
    public String createAddress(PublicKey pubKey) {
        return null;
    }

    @Override
    public String createAddress(List<PublicKey> publicKeys) {
        return null;
    }

    @Override
    public String generatePublicKey(PrivateKey privKey) {
        ECKey ecKey = ECKey.fromPrivate(privKey.getRaw());
        byte[] pubKeyData = ecKey.getPubKey();
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(pubKeyData, 0, pubKeyData.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        byte[] checksumBytes = Arrays.copyOfRange(out, 0, 4);

        pubKeyData = ByteUtil.concat(pubKeyData, checksumBytes);
        return "EOS" + Base58.encode(pubKeyData);
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, List<PrivateKey> keys) {
        return new byte[0];
    }
}
