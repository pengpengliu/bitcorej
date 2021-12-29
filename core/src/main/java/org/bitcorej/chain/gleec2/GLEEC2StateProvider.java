package org.bitcorej.chain.gleec2;

import org.bitcoinj.core.Base58;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.zcash.ZcashStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;

public class GLEEC2StateProvider extends ZcashStateProvider {

    public GLEEC2StateProvider(Network network) {
        super(network);
        super.params = GLEEC2NetParams.get();
        super.consensusBranchId = 0x76b809bb;
    }

    @Override
    public String generateP2PKHScript(String address) {
        byte[] versionAndDataBytes = Base58.decodeChecked(address);
        byte[] bytes = new byte[versionAndDataBytes.length - 1];
        System.arraycopy(versionAndDataBytes, 1, bytes, 0, versionAndDataBytes.length - 1);

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