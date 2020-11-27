package org.bitcorej.chain.bitcoin;

import org.bitcorej.chain.bch.AddressConverter;
import java.math.BigDecimal;

public class UnspentOutput {
    private String txId;
    private int vout;
    private String address;
    private BigDecimal amount;
    private long bip115BlockHeight;
    private byte[] bip115BlockHash;

    public UnspentOutput(String txId, int vout, String address, BigDecimal amount) {
        if (address.matches("^(bitcoincash:)?(q|p)[a-z0-9]{41}")) {
            address = AddressConverter.toLegacyAddress(address);
        }
        this.txId = txId;
        this.vout = vout;
        this.address = address;
        this.amount = amount;
    }

    public UnspentOutput(String txId, int vout, String address, BigDecimal amount, long bip115BlockHeight, byte[] bip115BlockHash) {
        if (address.matches("^(bitcoincash:)?(q|p)[a-z0-9]{41}")) {
            address = AddressConverter.toLegacyAddress(address);
        }
        this.txId = txId;
        this.vout = vout;
        this.address = address;
        this.amount = amount;
        this.bip115BlockHeight = bip115BlockHeight;
        this.bip115BlockHash = bip115BlockHash;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
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

    public long getBip115BlockHeight() {
        return bip115BlockHeight;
    }

    public byte[] getBip115BlockHash() {
        return bip115BlockHash;
    }

    @Override
    public String toString() {
        return this.txId + ':' + this.vout;
    }
}
