package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

public class MifosTransaction {

    private String requestId;
    private BigDecimal amount;
    private String accountNumber;
    private String accountType;
    private String narration;
    private String currency;
    private String transactionType;

    public MifosTransaction(String requestId, BigDecimal amount, String accountNumber, String accountType,
            String narration, String currency, String transactionType) {
        this.requestId = requestId;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.narration = narration;
        this.currency = currency;
        this.transactionType = transactionType;
    }

    public MifosTransaction() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String toString() {
        return "MifosTransaction [requestId=" + requestId + ", amount=" + amount + ", accountNumber=" + accountNumber
                + ", accountType=" + accountType + ", narration=" + narration + ", currency=" + currency
                + ", transactionType=" + transactionType + "]";
    }

}
