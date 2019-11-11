package org.bitcorej.chain;

import org.bitcorej.chain.bitcoin.Recipient;

import java.math.BigDecimal;
import java.util.List;

public interface USDTState {
    List<Recipient> buildRecipients(String from, String to, BigDecimal amount);
    List<Recipient> buildRecipients(String from, String to, BigDecimal amount, int propertyId);
}
