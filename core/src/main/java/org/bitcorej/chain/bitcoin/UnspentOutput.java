package org.bitcorej.chain.bitcoin;

import org.bitcorej.chain.bch.AddressConverter;
import java.math.BigDecimal;

public class UnspentOutput {
    private String txId;
    private int vout;
    private String address;
    private BigDecimal amount;

    public UnspentOutput(String txId, int vout, String address, BigDecimal amount) {
        if (address.matches("^(bitcoincash:)?(q|p)[a-z0-9]{41}")) {
            address = AddressConverter.toLegacyAddress(address);
        }
        this.txId = txId;
        this.vout = vout;
        this.address = address;
        this.amount = amount;
    }

    public String getTxId() {
        return txId;
    }

    public int getVout() {
        return vout;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return this.txId + ':' + this.vout;
    }
}
