package org.bitcorej.chain.ltc;

import org.bitcoinj.core.Address;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;

public class LitecoinStateProvider extends BitcoinStateProvider {
    public LitecoinStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(LitecoinNetParameters.get());
                break;
            case TEST:
                super.params = TestNet3Params.get();
                break;
            default:
                super.params = LitecoinNetParameters.get();
                break;
        }

        super.network = network;
    }

    public String generateP2PKHScript(String address) {
        // Mainnet p2sh address (deprecated)
        if (address.matches("^3[a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
            Address addr = Address.fromBase58(MainNetParams.get(), address);
            address = Address.fromP2SHHash(this.params, addr.getHash160()).toBase58();
        }
        return NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(this.params, address)).getProgram());
    }
}
