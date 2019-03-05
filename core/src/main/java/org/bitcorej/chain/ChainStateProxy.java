package org.bitcorej.chain;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.ethereum.EthereumStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

import java.util.ArrayList;
import java.util.HashMap;

public class ChainStateProxy implements ChainState {
    private static HashMap<String, ChainState> services;

    static
    {
        services = new HashMap<>();
        services.put("BTC_MAIN", new BitcoinStateProvider(Network.MAIN));
        services.put("BTC_TEST", new BitcoinStateProvider(Network.TEST));
        services.put("BTC_REGTEST", new BitcoinStateProvider(Network.REGTEST));
        services.put("ETH_MAIN", new EthereumStateProvider());
    }

    private ChainState provider;

    public ChainStateProxy(String chain, String network) throws Exception {
        this.provider = services.get(chain.toUpperCase() + "_" + network.toUpperCase());
        if (this.provider == null) {
            throw new Exception("Chain " + chain + " or Network " + network + " doesn't have a ChainState registered");
        }
    }

    @Override
    public String getAddress(PrivateKey privKey) {
        return provider.getAddress(privKey);
    }

    @Override
    public String getAddress(PublicKey pubKey) {
        return provider.getAddress(pubKey);
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, ArrayList<PrivateKey> keys) {
        return provider.signRawTransaction(rawTx, keys);
    }
}
