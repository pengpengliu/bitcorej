package org.bitcorej.chain.usdt;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.USDTState;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class USDTStateProvider extends BitcoinStateProvider implements USDTState {
    public USDTStateProvider(Network network) {
        super(network);
    }

    public List<Recipient> buildRecipients(String from, String to, BigDecimal amount) {
        return buildRecipients(from, to, amount, 1);
    }

    public List<Recipient> buildRecipients(String from, String to, BigDecimal amount, int propertyId) {
        List<Recipient> recipients = new ArrayList<>();

        recipients.add(new Recipient(generateP2PKHScript(to), new BigDecimal("0.00000546")));

        String protocolHex = "6f6d6e69"; // omni
        String versionHex = "0000";
        String propertyPrefixHex = "000000000000";
        String propertyIdHex = Integer.toHexString(propertyId);
        propertyIdHex = propertyPrefixHex.substring(0, propertyPrefixHex.length() - propertyIdHex.length()) + propertyIdHex;
        String amountPrefixHex = "0000000000000000";
        String amountHex = amount.multiply(DECIMALS).toBigInteger().toString(16);
        amountHex = amountPrefixHex.substring(0, amountPrefixHex.length() - amountHex.length()) + amountHex;
        String hex = protocolHex + versionHex + propertyIdHex + amountHex;

        Script script = ScriptBuilder.createOpReturnScript(NumericUtil.hexToBytes(hex));

        recipients.add(new Recipient(NumericUtil.bytesToHex(script.getProgram()), new BigDecimal("0")));

        return recipients;
    }
}
