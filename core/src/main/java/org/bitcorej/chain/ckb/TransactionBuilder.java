package org.bitcorej.chain.ckb;

import org.nervos.ckb.type.OutPoint;
import org.nervos.ckb.type.cell.CellDep;
import org.nervos.ckb.type.cell.CellInput;
import org.nervos.ckb.type.cell.CellOutput;
import org.nervos.ckb.type.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionBuilder {

    private List<CellInput> cellInputs = new ArrayList<>();
    private List<CellOutput> cellOutputs = new ArrayList<>();
    private List<String> cellOutputsData = new ArrayList<>();
    private List<CellDep> cellDeps = new ArrayList<>();
    private List<String> headerDeps = Collections.emptyList();
    private List witnesses = new ArrayList<>();

    public TransactionBuilder() {
        this.cellDeps.add(
                new CellDep(new OutPoint("0x71a7ba8fc96349fea0ed3a5c47992e3b4084b031a42264a018e0072e8172e46c", "0x0"), CellDep.DEP_GROUP));
    }

    public void addInput(CellInput input) {
        this.cellInputs.add(input);
    }

    public void addInputs(List<CellInput> inputs) {
        this.cellInputs.addAll(inputs);
    }

    public void setInputs(List<CellInput> inputs) {
        this.cellInputs = inputs;
    }

    public void addWitnesses(List witnesses) {
        this.witnesses = witnesses;
    }

    public void addWitness(Object witness) {
        this.witnesses.add(witness);
    }

    public void addOutput(CellOutput output) {
        this.cellOutputs.add(output);
    }

    public void addOutputs(List<CellOutput> outputs) {
        this.cellOutputs.addAll(outputs);
    }

    public void setOutputs(List<CellOutput> outputs) {
        this.cellOutputs = outputs;
    }

    public void addCellDep(CellDep cellDep) {
        this.cellDeps.add(cellDep);
    }

    public void addCellDeps(List<CellDep> cellDeps) {
        this.cellDeps.addAll(cellDeps);
    }

    public List<CellDep> getCellDeps() {
        return this.cellDeps;
    }

    public void setOutputsData(List<String> outputsData) {
        this.cellOutputsData = outputsData;
    }

    public void setHeaderDeps(List<String> headerDeps) {
        this.headerDeps = headerDeps;
    }

    public Transaction buildTx() {
        if (cellOutputsData.size() == 0) {
            for (int i = 0; i < cellOutputs.size(); i++) {
                cellOutputsData.add("0x");
            }
        }

        return new Transaction(
                "0", cellDeps, headerDeps, cellInputs, cellOutputs, cellOutputsData, witnesses);
    }
}
