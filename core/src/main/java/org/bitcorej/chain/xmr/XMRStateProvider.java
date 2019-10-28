package org.bitcorej.chain.xmr;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.xmr.core.key.KeyFactory;
import org.bitcorej.chain.xmr.core.key.KeyImageProviderImpl;
import org.bitcorej.chain.xmr.core.key.PublicKey;
import org.bitcorej.utils.NumericUtil;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.Random;

public class XMRStateProvider implements ChainState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        WalletToolsXMR wt = new WalletToolsXMR();
        Account account = wt.getAccount(secret, 0);
        System.out.println("account = " + account);

//        KeyImageProviderImpl keyImageProvider = new KeyImageProviderImpl();
//        String keyImage = null;
//        try {
//            keyImage = NumericUtil.bytesToHex(keyImageProvider.getKeyImage(account.getPrivateSpendKey(), new KeyFactory().decodePublicKey(NumericUtil.hexToBytes("2be590a3298fa578f04169cefc114d7b0c5fae2d0fa0ca517b08851a24019bd9"))));
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        }
//        System.out.println("keyImage = " + keyImage);
        return new KeyPair(secret, account.getAddress().toString());
    }

    @Override
    public KeyPair generateKeyPair() {
        WalletToolsXMR wt = new WalletToolsXMR();
        String seed = wt.generateSeedMnemonicSeparatedBySpaces();
        return this.generateKeyPair(seed);
    }

    @Override
    public Boolean validateTx(String rawTx, String requestTx) {
        return null;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        return null;
    }

    public String generatePaymentId() {
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        return NumericUtil.bytesToHex(b);
    }
}

// 477toXjSM9X8FcfSdCJrs4ZWvRdGVdfJv2N7nWhHV5GPgEqxBMsQx92W4Auz3EkMpBc9tuBr3ukDhTHYGaKpm7bUCT2AunS
// 597d53a88fcff43e6f90a2e393d3eda7ef6e11680961a1c4e3834ff0615b065b