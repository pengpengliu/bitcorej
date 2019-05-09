package org.bitcorej.chain.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.bch.AddressConverter;
import org.bitcorej.utils.NumericUtil;

import java.math.BigDecimal;

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
        this.scriptPubKey = NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(Address.getParametersFromAddress(address), address)).getProgram());
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
