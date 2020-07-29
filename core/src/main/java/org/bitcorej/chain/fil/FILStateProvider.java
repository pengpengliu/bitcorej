package org.bitcorej.chain.fil;

import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.vsys.HashUtil;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;

import java.util.List;

public class FILStateProvider implements ChainState {
    String prefix = "f";

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret), false);
        byte[] publicKey = ecKey.getPubKey();
        byte[] protocolByte = new byte[]{1};
        byte[] payload = HashUtil.hashB160(publicKey);
        byte[] checksum = HashUtil.hashB(ByteUtil.concat(protocolByte, payload), 32);
        return new KeyPair(secret, prefix + "1" + Base32.encode(ByteUtil.concat(payload, checksum)));
    }

    @Override
    public KeyPair generateKeyPair() {
        ECKey ecKey = new ECKey();
        return generateKeyPair(ecKey.getPrivateKeyAsHex());
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
        return null;
    }
}
