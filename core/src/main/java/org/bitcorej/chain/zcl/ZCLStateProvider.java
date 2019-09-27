package org.bitcorej.chain.zcl;

import org.bitcorej.chain.zcash.ZcashStateProvider;
import org.bitcorej.core.Network;

public class ZCLStateProvider extends ZcashStateProvider {

    public ZCLStateProvider(Network network) {
        super(network);
        super.consensusBranchId = 2466993165l;
    }
}
