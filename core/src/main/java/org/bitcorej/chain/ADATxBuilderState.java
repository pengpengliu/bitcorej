package org.bitcorej.chain;

import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;

import java.math.BigDecimal;
import java.util.List;

public interface ADATxBuilderState {
    String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, long bestSlot);
    String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, BigDecimal decimals, long bestSlot);
}
