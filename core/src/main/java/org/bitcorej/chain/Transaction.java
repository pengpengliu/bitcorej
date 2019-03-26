package org.bitcorej.chain;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    public class Input {
        private String address;
        private BigDecimal amount;

        public Input(String address, BigDecimal amount) {
            this.address = address;
            this.amount = amount;
        }
    }

    public class Output {
        private String address;
        private BigDecimal amount;
        private String memo;

        public Output(String address, BigDecimal amount, String memo) {
            this.address = address;
            this.amount = amount;
            this.memo = memo;
        }
    }

    private List<Input> from;
    private List<Output> to;

    private BigDecimal fee;

    public Transaction() {}

    public Transaction(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray from = jsonObject.getJSONArray("from");
        for (int i = 0; i < from.length(); i++) {
            JSONObject input = from.getJSONObject(i);
            this.addInput(new Input(input.getString("address"), input.has("amount") ? new BigDecimal(input.getString("amount")) : null));
        }
        JSONArray to = jsonObject.getJSONArray("to");
        for (int i = 0; i < to.length(); i++) {
            JSONObject output = to.getJSONObject(i);
            this.addOutput(new Output(output.getString("address"), new BigDecimal(output.getString("amount")), output.has("memo") ? output.getString("memo") : ""));
        }
        this.fee = jsonObject.has("fee") ? new BigDecimal(jsonObject.getString("fee")) : null;
    }

    public List<Input> getFrom() {
        return from;
    }

    public void setFrom(List<Input> from) {
        this.from = from;
    }

    public void addInput(Input input) {
        if (this.from == null)
            this.from = new ArrayList<>();
        this.from.add(input);
    }

    public List<Output> getTo() {
        return to;
    }

    public void setTo(List<Output> to) {
        this.to = to;
    }

    public void addOutput(Output output) {
        if (this.to == null)
            this.to = new ArrayList<>();
        this.to.add(output);
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Transaction)) return false;

        Transaction tx = (Transaction)obj;
        for (int i = 0; i < tx.getFrom().size(); i++) {
            if (!tx.getFrom().get(i).address.equals(this.getFrom().get(i).address)) {
                return false;
            }
            if (tx.getFrom().get(i).amount != null) {
                if (tx.getFrom().get(i).amount.compareTo(this.getFrom().get(i).amount) != 0) {
                    return false;
                }
            }
        }

        for (int i = 0; i < tx.getTo().size(); i++) {
            if (!tx.getTo().get(i).address.equals(this.getTo().get(i).address)) {
                return false;
            }
            if (tx.getTo().get(i).amount.compareTo(this.getTo().get(i).amount) != 0) {
                return false;
            }
            if (!tx.getTo().get(i).memo.equals(this.getTo().get(i).memo)) {
                return false;
            }
        }

        if (tx.getFee() != null) {
            return tx.getFee().compareTo(getFee()) == 0;
        } else {
            return true;
        }
    }
}
