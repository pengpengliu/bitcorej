package org.bitcorej.chain.bitcoin;

import java.math.BigDecimal;

public class Recipient {
    private String address;
    private BigDecimal amount;
    private long bip115BlockHeight;
    private byte[] bip115BlockHash;

    public Recipient(String address, BigDecimal amount) {
        this.address = address;
        this.amount = amount;
    }

    public Recipient(String address, BigDecimal amount, long bip115BlockHeight, byte[] bip115BlockHash) {
        this.address = address;
        this.amount = amount;
        this.bip115BlockHeight = bip115BlockHeight;
        this.bip115BlockHash = bip115BlockHash;
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
        return address + " " + amount;
    }
}
