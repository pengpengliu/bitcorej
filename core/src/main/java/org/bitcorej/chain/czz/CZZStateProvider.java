package org.bitcorej.chain.czz;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.bch.BCHStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;

import java.util.List;

public class CZZStateProvider extends BCHStateProvider {
    public CZZStateProvider(Network network) {
        super(network);
    }

    public String generateP2PKHScript(String address) {
        if (address.matches("^(c)[a-z0-9]{41}")) {
            address = org.bitcorej.chain.bch.AddressConverter.toLegacyAddress(address);
        }
        return NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(this.params, address)).getProgram());
    }

    @Override
    public String selectPrivateKeys(Script script, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            String address = script.getToAddress(this.params).toString();
            if (address.equals(address)) {
                return keys.get(i);
            }
        }
        return null;
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        String address = AddressConverter.toCashAddress(ecKey.toAddress(this.params).toString());
        return new KeyPair(ecKey.getPrivateKeyAsHex(), address);
    }
}
