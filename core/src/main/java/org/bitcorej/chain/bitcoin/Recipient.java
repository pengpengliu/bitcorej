package org.bitcorej.chain.bitcoin;

import java.math.BigDecimal;

public class Recipient {
    private String script;
    private BigDecimal amount;

    public Recipient(String script, BigDecimal amount) {
        this.script = script;
        this.amount = amount;
    }

    public String getScript() {
        return script;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
