package com.gkash.gkashsoftpossdk.model;

public class TransactionResult {
    public String type;
    public TransactionDetails result;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TransactionDetails getResult() {
        return result;
    }

    public void setResult(TransactionDetails result) {
        this.result = result;
    }
}
