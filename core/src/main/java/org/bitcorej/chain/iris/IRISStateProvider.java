package org.bitcorej.chain.iris;

import org.bitcorej.chain.cosmos.CosmosStateProvider;

public class IRISStateProvider extends CosmosStateProvider {
    public IRISStateProvider() {
        super.bech32AccAddr = "iaa";
        super.transferPrefix = "irishub/bank/Send";
    }
}
