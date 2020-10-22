package org.bitcorej.chain.vrsc;

import org.bitcoinj.core.Base58;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.zcash.ZcashStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;

public class VRSCStateProvider extends ZcashStateProvider {
    public VRSCStateProvider(Network network) {
        super(network);
        super.params = VRSCNetParameters.get();

        super.consensusBranchId = 0x76b809bb;
    }

    public String generateP2PKHScript(String address) {
        byte[] versionAndDataBytes = Base58.decodeChecked(address);
        byte[] bytes = new byte[versionAndDataBytes.length - 1];
        System.arraycopy(versionAndDataBytes, 1, bytes, 0, versionAndDataBytes.length - 1);
        System.out.println(NumericUtil.bytesToHex(versionAndDataBytes));
        System.out.println(NumericUtil.bytesToHex(bytes));
        Script script = new ScriptBuilder()
                .op(OP_DUP)
                .op(OP_HASH160)
                .data(bytes)
                .op(OP_EQUALVERIFY)
                .op(OP_CHECKSIG)
                .build();
        return NumericUtil.bytesToHex(script.getProgram());
    }
}
