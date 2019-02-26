package org.bitcorej.chain;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.ethereum.EthereumStateProvider;
import org.bitcorej.core.PrivateKey;

import java.util.HashMap;

public class ChainStateProxy implements ChainState {
    private static HashMap<String, ChainState> services;

    static
    {
        services = new HashMap<>();
        services.put("BTC", new BitcoinStateProvider());
        services.put("ETH", new EthereumStateProvider());
    }

    private ChainState provider;

    public ChainStateProxy(String chain) throws Exception {
        chain = chain.toUpperCase();
        this.provider = services.get(chain);
        if (provider == null) {
            throw new Exception("Chain " + chain + " doesn't have a ChainState registered");
        }
    }

    @Override
    public String getAddress(PrivateKey privKey) {
        return provider.getAddress(privKey);
    }
}
