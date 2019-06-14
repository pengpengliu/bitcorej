package org.bitcorej.examples;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.ChainStateProxy;

import java.util.Collections;
import java.util.List;

public class ExampleVETWallet {
    public static void main(String[] args) throws Exception {
        ChainState vet = new ChainStateProxy("vet", "test");
        System.out.println(vet.generateKeyPair());
        System.out.println(vet.generateKeyPair("39eb72a0f7df1e7ecdb5554a0029f8b6e7bb846bf2c2c47c65a67ce7ce317711"));

        List<String> keys = Collections.singletonList("39eb72a0f7df1e7ecdb5554a0029f8b6e7bb846bf2c2c47c65a67ce7ce317711");

        String signedTx = vet.signRawTransaction("{\"blockRef\":\"002e83fdfa05d3b3\",\"amount\":\"0.1\",\"to\":\"0xeab0d9de81c64aae470a1b4bd1fea75848a79159\"}", keys);
        System.out.println(signedTx);

        String signedVETOTx = vet.signRawTransaction("{\"blockRef\":\"002e83fdfa05d3b3\",\"amount\":\"0.1\",\"to\":\"0xeab0d9de81c64aae470a1b4bd1fea75848a79159\",\"token\":{\"name\":\"ERC20\",\"address\":\"0x0000000000000000000000000000456e65726779\",\"unit\":18}}", keys);
        System.out.println(signedVETOTx);
    }
}
