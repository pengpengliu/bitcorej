package org.bitcorej.chain.dot;

import iost.crypto.Ed25519;
import org.bitcoinj.core.Base58;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.vsys.HashUtil;
import org.bitcorej.core.Network;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class DOTStateProvider implements ChainState {
    private final static byte[] AddressTypePolkadot = new byte[]{0x00};
    private final static byte[] AddressTypeWestend = new byte[]{0x2A};;

    private final static byte[] SignTypeEd25519 = new byte[]{0x00};;

    // SS58PRE => UInt8Array : 53533538505245
    private final static byte[] SS58_PREFIX = new byte[]{0x53, 0x53, 0x35, 0x38, 0x50, 0x52, 0x45};

    private byte[] addressType;

    public DOTStateProvider() {
        addressType = AddressTypePolkadot;
    }

    public DOTStateProvider(Network network) {
        switch (network) {
            case MAIN:
                addressType = AddressTypePolkadot;
                break;
            case TEST:
                addressType = AddressTypeWestend;
                break;
        }
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        Ed25519 keyPair = new Ed25519(NumericUtil.hexToBytes(secret));
        byte[] bytes = ByteUtil.concat(addressType, keyPair.pubkey());

        byte[] hash = HashUtil.hashB(ByteUtil.concat(SS58_PREFIX, bytes), 512);
        byte[] checksum = Arrays.copyOfRange(hash, 0, 2);
        return new KeyPair(secret, Base58.encode(ByteUtil.concat(bytes, checksum)));
    }

    @Override
    public KeyPair generateKeyPair() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return generateKeyPair(NumericUtil.bytesToHex(bytes));
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
        JSONObject data = new JSONObject(rawTx);
        String hex = data.getString("hex");
        String payload = data.getString("payload");

        byte[] toSign = NumericUtil.hexToBytes(payload);

        Ed25519 keyPair = new Ed25519(NumericUtil.hexToBytes(keys.get(0)));
        byte[] signature = ByteUtil.concat(SignTypeEd25519, keyPair.sign(toSign).signature);
        String placeholder = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";
        data.put("hex", hex.replace(placeholder, NumericUtil.bytesToHex(signature)));
        data.remove("payload");
        return data.toString();
    }
}