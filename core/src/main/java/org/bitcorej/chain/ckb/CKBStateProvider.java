package org.bitcorej.chain.ckb;

import com.google.gson.Gson;
import org.bitcoinj.core.ECKey;
import org.bitcorej.chain.ChainState;
import org.bitcorej.chain.KeyPair;
import org.bitcorej.chain.Transaction;
import org.bitcorej.chain.UTXOState;
import org.bitcorej.chain.bitcoin.Recipient;
import org.bitcorej.chain.bitcoin.UnspentOutput;
import org.bitcorej.utils.NumericUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nervos.ckb.address.AddressUtils;
import org.nervos.ckb.address.Network;
import org.nervos.ckb.service.Api;
import org.nervos.ckb.transaction.ScriptGroup;
import org.nervos.ckb.transaction.Secp256k1SighashAllBuilder;
import org.nervos.ckb.type.OutPoint;
import org.nervos.ckb.type.Witness;
import org.nervos.ckb.type.cell.CellInput;
import org.nervos.ckb.type.cell.CellOutput;
import org.nervos.ckb.utils.Convert;
import org.nervos.ckb.utils.Numeric;
import org.nervos.ckb.utils.address.AddressParseResult;
import org.nervos.ckb.utils.address.AddressParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class CKBStateProvider implements ChainState, UTXOState {
    protected static final BigDecimal DECIMALS = new BigDecimal(10).pow(8);
    protected static final BigDecimal MIN_CELL_CAPACITY = new BigDecimal(61).multiply(DECIMALS);

    @Override
    public KeyPair generateKeyPair(String secret) {
        ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(secret));
        AddressUtils utils = new AddressUtils(Network.MAINNET);
        String address = utils.generateFromPublicKey(ecKey.getPublicKeyAsHex());
        return new KeyPair(ecKey.getPrivateKeyAsHex(), address);
    }

    @Override
    public KeyPair generateKeyPair() {
        return this.generateKeyPair(new ECKey().getPrivateKeyAsHex());
    }

    @Override
    public Boolean validateTx(String rawTx, String requestTx) {
        return null;
    }

    @Override
    public Transaction decodeRawTransaction(String rawTx) {
        return null;
    }

    private List<CellsWithAddress> calCellsWithAddresses(List<UnspentOutput> utxos) {
        List<CellsWithAddress> cellsWithAddresses = new ArrayList<>();
        Map<String, List<CellInput>> addresses = new HashMap<>();
        for (int i = 0; i < utxos.size(); i++) {
            UnspentOutput utxo = utxos.get(i);
            CellInput cellInput = new CellInput(new OutPoint(utxo.getTxId(), Numeric.toHexStringWithPrefix(BigInteger.valueOf(utxo.getVout()))), "0x0");
            List<CellInput> inputs = addresses.get(utxo.getAddress());
            if (inputs != null) {
                inputs.add(cellInput);
            } else {
                List<CellInput> inputs1 = new ArrayList<>();
                inputs1.add(cellInput);
                addresses.put(utxo.getAddress(), inputs1);
            }
        }
        Iterator iter = addresses.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<CellInput> inputs = (List<CellInput>) entry.getValue();
            cellsWithAddresses.add(new CellsWithAddress(inputs, key));
        }
        return cellsWithAddresses;
    }

    @Override
    public String signRawTransaction(String rawTx, List<String> keys) {
        org.nervos.ckb.type.transaction.Transaction tx = new Gson().fromJson(rawTx, org.nervos.ckb.type.transaction.Transaction.class);
        TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.addInputs(tx.inputs);
        txBuilder.addOutputs(tx.outputs);

        JSONObject jo = new JSONObject(rawTx);
        JSONArray jsonInputs = jo.getJSONArray("inputs");
        ArrayList<UnspentOutput> utxos = new ArrayList<>();
        for (int i = 0; i < jsonInputs.length(); i++) {
            String address = jsonInputs.getJSONObject(i).getString("address");
            CellInput cellInput = tx.inputs.get(i);
            utxos.add(new UnspentOutput(cellInput.previousOutput.txHash, Numeric.toBigInt(cellInput.previousOutput.index).intValue(), address, new BigDecimal(0)));
        }
        List<CellsWithAddress> cellsWithAddresses = calCellsWithAddresses(utxos);
        try {
            List<ScriptGroupWithPrivateKeys> scriptGroupWithPrivateKeysList = new ArrayList<>();
            int startIndex = 0;
            for (CellsWithAddress cellsWithAddress : cellsWithAddresses) {
//                scriptGroupWithPrivateKeysList.add(
//                        new ScriptGroupWithPrivateKeys(
//                                new ScriptGroup(NumericUtil.regionToList(startIndex, cellsWithAddress.inputs.size())),
//                                Collections.singletonList(selectPrivateKeys(cellsWithAddress.address, keys))));
                for (int i = 0; i < cellsWithAddress.inputs.size(); i++) {
                    txBuilder.addWitness(i == 0 ? new Witness(Witness.SIGNATURE_PLACEHOLDER) : "0x");
                }
                if (cellsWithAddress.inputs.size() > 0) {
                    scriptGroupWithPrivateKeysList.add(
                            new ScriptGroupWithPrivateKeys(
                                    new ScriptGroup(
                                            NumericUtil.regionToList(startIndex, cellsWithAddress.inputs.size())),
                                    Collections.singletonList(selectPrivateKeys(cellsWithAddress.address, keys))));
                    startIndex += cellsWithAddress.inputs.size();
                }
            }
            // signing
            Secp256k1SighashAllBuilder signBuilder = new Secp256k1SighashAllBuilder(txBuilder.buildTx());

            for (int i = 0; i < scriptGroupWithPrivateKeysList.size(); i++) {
                ScriptGroupWithPrivateKeys scriptGroupWithPrivateKeys = scriptGroupWithPrivateKeysList.get(i);
                signBuilder.sign(
                        scriptGroupWithPrivateKeys.scriptGroup, scriptGroupWithPrivateKeys.privateKeys.get(0));
            }
            Gson gson = new Gson();
            org.nervos.ckb.type.transaction.Transaction t = signBuilder.buildTx();
            JSONObject packedTx = new JSONObject();
            packedTx.put("txid", t.computeHash());
            packedTx.put("raw", new JSONObject(gson.toJson(Convert.parseTransaction(t))));
            return packedTx.toString();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    String selectPrivateKeys(String address, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(keys.get(i)));
            AddressUtils utils = new AddressUtils(Network.MAINNET);
            String address2 = utils.generateFromPublicKey(ecKey.getPublicKeyAsHex());
            if (address2.equals(address)) {
                return keys.get(i);
            }
        }
        return null;
    }

    public void buildTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, List<String> keys) {
        BigDecimal needCapacity = BigDecimal.ZERO;
        for (Recipient receiver : recipients) {
            needCapacity = needCapacity.add(receiver.getAmount());
        }

        TransactionBuilder txBuilder = new TransactionBuilder();

        List<CellOutput> cellOutputs = new ArrayList<>();
        for (Recipient receiver : recipients) {
            AddressParseResult addressParseResult = AddressParser.parse(receiver.getAddress());
            cellOutputs.add(
                    new CellOutput(
                            Numeric.toHexStringWithPrefix(receiver.getAmount().multiply(DECIMALS).toBigInteger()), addressParseResult.script));
        }

        AddressParseResult addressParseResult = AddressParser.parse(changeAddress);
        cellOutputs.add(new CellOutput("0x0", addressParseResult.script));

        // Single Key
        BigDecimal totalInputAmount = new BigDecimal(0);
        for (int i = 0; i < utxos.size(); i++) {
            UnspentOutput utxo = utxos.get(i);
            txBuilder.addInput(new CellInput(new OutPoint(utxo.getTxId(),  Numeric.toHexStringWithPrefix(BigInteger.valueOf(utxo.getVout()))), "0x0"));
            totalInputAmount = totalInputAmount.add(utxo.getAmount());
        }
        for (int i = 0; i < utxos.size(); i++) {
            txBuilder.addWitness(i == 0 ? new Witness(Witness.SIGNATURE_PLACEHOLDER) : "0x");
        }

        if (totalInputAmount.compareTo(needCapacity) < 1) {
            throw new RuntimeException("INSUFFICIENT FUNDS");
        }

        BigDecimal changeAmount = totalInputAmount.subtract(needCapacity.add(fee));
        // update change output capacity after collecting cells
        cellOutputs.get(cellOutputs.size() - 1).capacity = Numeric.toHexStringWithPrefix(changeAmount.multiply(DECIMALS).toBigInteger());
        txBuilder.addOutputs(cellOutputs);

        Gson gson = new Gson();
        org.nervos.ckb.type.transaction.Transaction tx = txBuilder.buildTx(); // gson.fromJson(gson.toJson(txBuilder.buildTx()), org.nervos.ckb.type.transaction.Transaction.class);
        try {
            // signing
            Secp256k1SighashAllBuilder signBuilder = new Secp256k1SighashAllBuilder(tx);
            ScriptGroup scriptGroup = new ScriptGroup(NumericUtil.regionToList(0, utxos.size()));
            signBuilder.sign(scriptGroup, selectPrivateKeys(utxos.get(0).getAddress(), keys));
            System.out.println(gson.toJson(Convert.parseTransaction(signBuilder.buildTx())));
        } catch (IOException e) {
        }

    }

    @Override
    public String toWIF(String privateKeyHex) {
        return null;
    }

    @Override
    public String calcRedeemScript(String segWitAddress) {
        return null;
    }

    @Override
    public String calcWitnessScript(String segWitAddress) {
        return null;
    }

    @Override
    public String calcSegWitAddress(String legacyAddress) {
        return null;
    }

    @Override
    public String generateP2PKHScript(String address) {
        return null;
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee) {
        return encodeTransaction(utxos, recipients, changeAddress, fee, DECIMALS);
    }

    @Override
    public String encodeTransaction(List<UnspentOutput> utxos, List<Recipient> recipients, String changeAddress, BigDecimal fee, BigDecimal decimals) {
        BigDecimal needCapacity = BigDecimal.ZERO;
        for (Recipient receiver : recipients) {
            needCapacity = needCapacity.add(receiver.getAmount());
        }

        TransactionBuilder txBuilder = new TransactionBuilder();

        List<CellOutput> cellOutputs = new ArrayList<>();
        for (Recipient receiver : recipients) {
            if (receiver.getAmount().multiply(DECIMALS).compareTo(MIN_CELL_CAPACITY) < 1) {
                return null;
            }
            AddressParseResult addressParseResult = AddressParser.parse(receiver.getAddress());
            cellOutputs.add(
                    new CellOutput(
                            Numeric.toHexStringWithPrefix(receiver.getAmount().multiply(DECIMALS).toBigInteger()), addressParseResult.script));
        }

        AddressParseResult addressParseResult = AddressParser.parse(changeAddress);
        cellOutputs.add(new CellOutput("0x0", addressParseResult.script));
        // Cal fees
        BigDecimal totalInputAmount = new BigDecimal(0);
        for (int i = 0; i < utxos.size(); i++) {
            UnspentOutput utxo = utxos.get(i);
            totalInputAmount = totalInputAmount.add(utxo.getAmount());
        }

        if (totalInputAmount.compareTo(needCapacity) < 1) {
            throw new RuntimeException("INSUFFICIENT FUNDS");
        }

        List<CellsWithAddress> cellsWithAddresses = calCellsWithAddresses(utxos);
        for (CellsWithAddress cellsWithAddress : cellsWithAddresses) {
            txBuilder.addInputs(cellsWithAddress.inputs);
            for (int i = 0; i < cellsWithAddress.inputs.size(); i++) {
                txBuilder.addWitness(i == 0 ? new Witness(Witness.SIGNATURE_PLACEHOLDER) : "0x");
            }
        }

        BigDecimal changeAmount = totalInputAmount.subtract(needCapacity.add(fee));
        // update change output capacity after collecting cells
        cellOutputs.get(cellOutputs.size() - 1).capacity = Numeric.toHexStringWithPrefix(changeAmount.multiply(DECIMALS).toBigInteger());
        txBuilder.addOutputs(cellOutputs);
        String rawTx = new Gson().toJson(txBuilder.buildTx());
        JSONObject jo = new JSONObject(rawTx);
        JSONArray ja = jo.getJSONArray("inputs");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject in = ja.getJSONObject(i);
            for(UnspentOutput utxo: utxos) {
                if (utxo.getTxId().equals(in.getJSONObject("previous_output").getString("tx_hash"))
                        & utxo.getVout() == Numeric.toBigInt(in.getJSONObject("previous_output").getString("index")).intValue()) {
                    in.put("address", utxo.getAddress());
                    break;
                }
            }
        }
        return jo.toString();
    }
}