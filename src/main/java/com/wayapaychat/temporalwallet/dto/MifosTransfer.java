package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
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

}
