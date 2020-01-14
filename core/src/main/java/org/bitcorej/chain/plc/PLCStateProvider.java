package org.bitcorej.chain.plc;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.BitUtils;
import org.bitcorej.utils.ByteUtil;
import org.bitcorej.utils.NumericUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;

public class PLCStateProvider extends BitcoinStateProvider {
    public PLCStateProvider(Network network) {
        super(network);
        super.params = PLCNetParameters.get();

        super.network = network;
    }

    public String calcTAddress(byte[] pubKeyHash) {
        byte[] version = ByteBuffer.allocate(4).putInt(this.params.getAddressHeader()).array();
        byte[] hash160 = ByteUtil.trimLeadingZeroes(BitUtils.concatenate(version, pubKeyHash));
        byte[] hash = Sha256Hash.hashTwice(hash160);
        byte[] checksum = Arrays.copyOfRange(hash, 0, 4);
        return Base58.encode(BitUtils.concatenate(hash160, checksum));
    }

    public String generateP2PKHScript(String address) {
        byte[] versionAndDataBytes = Base58.decodeChecked(address);
        byte[] bytes = new byte[versionAndDataBytes.length - 3];
        System.arraycopy(versionAndDataBytes, 3, bytes, 0, versionAndDataBytes.length - 3);
        Script script = new ScriptBuilder()
                .op(OP_DUP)
                .op(OP_HASH160)
                .data(bytes)
                .op(OP_EQUALVERIFY)
                .op(OP_CHECKSIG)
                .build();
        return NumericUtil.bytesToHex(script.getProgram());
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        return new KeyPair(ecKey.getPrivateKeyAsHex(), this.calcTAddress(pubKeyHash));
    }

    @Override
    protected String selectPrivateKeys(Script script, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            if (NumericUtil.bytesToHex(script.getProgram()).equals(generateP2PKHScript(this.generateKeyPair(keys.get(i)).getPublic()))) {
                return keys.get(i);
            }
        }
        return null;
    }
}