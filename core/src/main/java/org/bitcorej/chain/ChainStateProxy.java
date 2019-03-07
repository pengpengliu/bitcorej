package org.bitcorej.chain;

import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.ethereum.EthereumStateProvider;
import org.bitcorej.core.Network;
import org.bitcorej.core.PrivateKey;
import org.bitcorej.core.PublicKey;

import java.util.HashMap;
import java.util.List;

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
    public String createAddress(PrivateKey privKey) {
        return provider.createAddress(privKey);
    }

    @Override
    public String createAddress(PublicKey pubKey) {
        return provider.createAddress(pubKey);
    }

    @Override
    public String createAddress(List<PublicKey> publicKeys) {
        return provider.createAddress(publicKeys);
    }

    @Override
    public byte[] signRawTransaction(byte[] rawTx, List<PrivateKey> keys) {
        return provider.signRawTransaction(rawTx, keys);
    }
}
