package org.bitcorej.chain;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.eos.EOSStateProvider;
import org.bitcorej.chain.ethereum.EthereumStateProvider;
import org.bitcorej.chain.ripple.RippleStateProvider;
import org.bitcorej.chain.stellar.StellarStateProvider;
import org.bitcorej.core.Network;

import java.util.HashMap;
import java.util.List;

public class ChainStateProxy implements ChainState {
    private static HashMap<String, ChainState> services;

    static
    {
        services = new HashMap<>();
        services.put("BTC_MAIN", new BitcoinStateProvider(Network.MAIN));
        services.put("BTC_TEST", new BitcoinStateProvider(Network.TEST));
        services.put("ETH", new EthereumStateProvider());
        services.put("EOS", new EOSStateProvider());
        services.put("XRP", new RippleStateProvider());
        services.put("XLM", new StellarStateProvider());
    }

    private ChainState provider;

    public ChainStateProxy(String chain, String network) throws Exception {
        this.provider = services.get(chain.toUpperCase() + "_" + network.toUpperCase());
        if (this.provider == null) {
            throw new Exception("Chain " + chain + " or Network " + network + " doesn't have a ChainState registered");
        }
    }

    public ChainStateProxy(String chain) throws Exception {
        this.provider = services.get(chain.toUpperCase());
        if (this.provider == null) {
            throw new Exception("Chain " + chain + " doesn't have a ChainState registered");
        }
    }

    @Override
    public KeyPair generateKeyPair(String secret) {
        return provider.generateKeyPair(secret);
    }

    @Override
    public KeyPair generateKeyPair() {
        return provider.generateKeyPair();
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        return provider.signRawTransaction(rawTx, keys);
    }
}
