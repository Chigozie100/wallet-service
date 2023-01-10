package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import java.math.BigDecimal;

public class MifosTransfer {
    private String requestId;
    private BigDecimal amount;
    private String destinationAccountNumber;
    private String destinationAccountType;
    private String sourceAccountNumber;
    private String sourceAccountType;
    private String narration;
    private String transactionType;
    private String destinationCurrency;
    private String sourceCurrency;

    public MifosTransfer(String requestId, BigDecimal amount, String destinationAccountNumber, String destinationAccountType, String sourceAccountNumber, String sourceAccountType, String narration, String transactionType, String destinationCurrency, String sourceCurrency) {
        this.requestId = requestId;
        this.amount = amount;
        this.destinationAccountNumber = destinationAccountNumber;
        this.destinationAccountType = destinationAccountType;
        this.sourceAccountNumber = sourceAccountNumber;
        this.sourceAccountType = sourceAccountType;
        this.narration = narration;
        this.transactionType = transactionType;
        this.destinationCurrency = destinationCurrency;
        this.sourceCurrency = sourceCurrency;
    }

    public MifosTransfer() {
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

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public String getDestinationAccountType() {
        return destinationAccountType;
    }

    public void setDestinationAccountType(String destinationAccountType) {
        this.destinationAccountType = destinationAccountType;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getSourceAccountType() {
        return sourceAccountType;
    }

    public void setSourceAccountType(String sourceAccountType) {
        this.sourceAccountType = sourceAccountType;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDestinationCurrency() {
        return destinationCurrency;
    }

    public void setDestinationCurrency(String destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    @Override
    public String toString() {
        return "MifosTransfer{" +
                "requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", destinationAccountNumber='" + destinationAccountNumber + '\'' +
                ", destinationAccountType='" + destinationAccountType + '\'' +
                ", sourceAccountNumber='" + sourceAccountNumber + '\'' +
                ", sourceAccountType='" + sourceAccountType + '\'' +
                ", narration='" + narration + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", destinationCurrency='" + destinationCurrency + '\'' +
                ", sourceCurrency='" + sourceCurrency + '\'' +
                '}';
    }
}
