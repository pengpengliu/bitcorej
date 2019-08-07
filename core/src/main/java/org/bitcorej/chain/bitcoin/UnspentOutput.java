package org.bitcorej.chain.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.bch.AddressConverter;
import org.bitcorej.utils.NumericUtil;

import java.math.BigDecimal;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;

public class UnspentOutput {
    private String txId;
    private int vout;
    private String scriptPubKey;
    private BigDecimal amount;

    public UnspentOutput(String txId, int vout, String address, BigDecimal amount) {
        if (address.matches("^(bitcoincash:)?(q|p)[a-z0-9]{41}")) {
            address = AddressConverter.toLegacyAddress(address);
        }
        this.txId = txId;
        this.vout = vout;
        // Zcash
        if (address.matches("^t1[a-zA-Z0-9]{33}$")) {
            byte[] versionAndDataBytes = Base58.decodeChecked(address);
            byte[] bytes = new byte[versionAndDataBytes.length - 2];
            System.arraycopy(versionAndDataBytes, 2, bytes, 0, versionAndDataBytes.length - 2);
            Script script = new ScriptBuilder()
                    .op(OP_DUP)
                    .op(OP_HASH160)
                    .data(bytes)
                    .op(OP_EQUALVERIFY)
                    .op(OP_CHECKSIG)
                    .build();
            this.scriptPubKey = NumericUtil.bytesToHex(script.getProgram());
        } else {
            this.scriptPubKey = NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(Address.getParametersFromAddress(address), address)).getProgram());
        }
        this.amount = amount;
    }

    public String getTxId() {
        return txId;
    }

    public int getVout() {
        return vout;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return this.txId + ':' + this.vout;
    }
}
