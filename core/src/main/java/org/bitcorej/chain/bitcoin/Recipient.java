package org.bitcorej.chain.bitcoin;

import java.math.BigDecimal;

public class Recipient {
    private String address;
    private BigDecimal amount;

    public Recipient(String address, BigDecimal amount) {
        this.address = address;
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return address + " " + amount;
    }
}
