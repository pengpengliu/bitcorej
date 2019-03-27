package org.bitcorej.chain.erc20;

import org.bitcorej.chain.ethereum.EthereumStateProvider;

public class ERC20StateProvider extends EthereumStateProvider {
    private String address;

    public ERC20StateProvider(String tokenAddress) {
        this.address = tokenAddress;
    }
}
