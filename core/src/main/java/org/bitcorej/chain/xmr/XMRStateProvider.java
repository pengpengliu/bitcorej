package org.bitcorej.chain.xmr;

import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.XMRState;
import org.bitcorej.utils.NumericUtil;

import java.util.List;
import java.util.Random;

public class XMRStateProvider implements ChainState, XMRState {
    @Override
    public KeyPair generateKeyPair(String secret) {
        WalletToolsXMR wt = new WalletToolsXMR();
        Account account = wt.getAccount(secret, 0);
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

    public String generateViewKey(String secret) {
        WalletToolsXMR wt = new WalletToolsXMR();
        Account account = wt.getAccount(secret, 0);
        return account.getPrivateViewKey().toString();
    }

    public String generatePaymentId() {
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        return NumericUtil.bytesToHex(b);
    }
}