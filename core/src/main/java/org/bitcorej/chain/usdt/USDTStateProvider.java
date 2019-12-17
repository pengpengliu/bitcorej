package org.bitcorej.chain.usdt;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcorej.chain.USDTState;
import org.bitcorej.chain.bitcoin.BitcoinStateProvider;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.core.Network;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class USDTStateProvider extends BitcoinStateProvider implements USDTState {
    public USDTStateProvider(Network network) {
        super(network);
    }

    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, BigDecimal decimals) {
        JSONObject encodedTx = new JSONObject();

        BigDecimal totalInputAmount = new BigDecimal(0);
        JSONArray encodedInputs = new JSONArray();
        for (int i = 0; i < utxos.size(); i++) {
            UnspentOutput utxo = utxos.get(i);
            JSONObject encodedInput = new JSONObject();
            encodedInput.put("txid", utxo.getTxId());
            encodedInput.put("vout", utxo.getVout());
            JSONObject output = new JSONObject();
            String scriptPubKey = generateP2PKHScript(utxo.getAddress());
            output.put("script", scriptPubKey);
            BigDecimal amount = utxo.getAmount();
            output.put("amount", amount.toString());
            totalInputAmount = totalInputAmount.add(amount);
            encodedInput.put("output", output);
            encodedInputs.put(encodedInput);
        }

        BigDecimal totalOutputAmount = new BigDecimal(0);
        JSONArray encodedOutputs = new JSONArray();
        JSONArray destinations = new JSONArray();

        Recipient recipient = recipients.get(0);
        // Step 1
        JSONObject encodedOutput1 = new JSONObject();
        encodedOutput1.put("script", generateP2PKHScript(recipient.getAddress()));
        encodedOutput1.put("amount", new BigDecimal("0.00000546").toString());
        encodedOutputs.put(encodedOutput1);
        // Step 2
        JSONObject encodedOutput2 = new JSONObject();
        encodedOutput2.put("script", generateScriptHash(recipient));
        encodedOutput2.put("amount", new BigDecimal("0").toString());
        encodedOutputs.put(encodedOutput2);

        destinations.put(recipient.toString());

        if (totalInputAmount.compareTo(totalOutputAmount) < 1) {
            throw new RuntimeException("INSUFFICIENT FUNDS");
        }

        BigDecimal changeAmount = totalInputAmount.subtract(totalOutputAmount.add(fee));

        if (changeAmount.compareTo(DUST_THRESHOLD.divide(decimals)) > -1) {
            JSONObject encodedOutput = new JSONObject();
            encodedOutput.put("amount", changeAmount.toString());
            String script = generateP2PKHScript(changeAddress);
            // String script = NumericUtil.bytesToHex(ScriptBuilder.createOutputScript(Address.fromBase58(Address.getParametersFromAddress(changeAddress), changeAddress)).getProgram());
            encodedOutput.put("script", script);
            encodedOutputs.put(encodedOutput);
        }
        encodedTx.put("version", 1);
        encodedTx.put("inputs", encodedInputs);
        encodedTx.put("outputs", encodedOutputs);

        encodedTx.put("destinations", destinations);

        encodedTx.put("nLockTime", 0);
        return encodedTx.toString();
    }

    public String generateScriptHash(Recipient recipient) {
        int propertyId = 31; // USDT
        return generateScriptHash(recipient, propertyId);
    }

    public String generateScriptHash(Recipient recipient, int propertyId) {
        String protocolHex = "6f6d6e69"; // omni
        String versionHex = "0000";
        String propertyPrefixHex = "000000000000";
        String propertyIdHex = Integer.toHexString(propertyId);
        propertyIdHex = propertyPrefixHex.substring(0, propertyPrefixHex.length() - propertyIdHex.length()) + propertyIdHex;
        String amountPrefixHex = "0000000000000000";
        String amountHex = recipient.getAmount().multiply(DECIMALS).toBigInteger().toString(16);
        amountHex = amountPrefixHex.substring(0, amountPrefixHex.length() - amountHex.length()) + amountHex;
        String hex = protocolHex + versionHex + propertyIdHex + amountHex;

        Script script = ScriptBuilder.createOpReturnScript(NumericUtil.hexToBytes(hex));
        return NumericUtil.bytesToHex(script.getProgram());
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
