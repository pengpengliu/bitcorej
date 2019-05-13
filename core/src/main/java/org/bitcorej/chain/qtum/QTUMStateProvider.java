package org.bitcorej.chain.qtum;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.core.Network;

public class QTUMStateProvider extends BitcoinStateProvider {

    public QTUMStateProvider(Network network) {
        super(network);
        switch (network) {
            case MAIN:
                setParams(QTUMNetParams.get());
                break;
            case TEST:
                setParams(QTUMTestNetParams.get());
                break;
            default:
                super.params = QTUMNetParams.get();
                break;
        }

        super.network = network;
    }
}

