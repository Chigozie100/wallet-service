package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class TransferSimulationDTO {

    //@NotNull
    @NotBlank(message = "Account must not Null or Blank")
    @Size(min=10, max=15, message = "Account must be 10 digit")
    private String debitAccountNumber;

    //@NotNull(message = "Account must be 10 digit")
    @NotBlank(message = "Account must not Null or Blank")
    @Size(min=10, max=15, message = "Account must be 10 digit")
    private String benefAccountNumber;

    @NotNull
    @Min(value = (long) 0.01, message ="Amount must be greater than zero")
    private BigDecimal amount;

    //@NotNull
    @NotBlank(message = "tranType must not Null or Blank")
    private String tranType;

    //@NotNull
    @NotBlank(message = "tranCrncy must not Null or Blank")
    @Size(min=3, max=5, message = "tranCrncy must be 3 alphanumeric (NGN)")
    private String tranCrncy;

    //@NotNull
    @NotBlank(message = "tranNarration must not Null or Blank")
    @Size(min=5, max=50, message = "tranNarration must be aleast 5 alphanumeric")
    private String tranNarration;

    //@NotNull
    @NotBlank(message = "paymentReference must not Null or Blank")
    @Size(min=3, max=50, message = "paymentReference must be aleast 3 alphanumeric")
    private String paymentReference;

    @NotBlank(message = "transactionCategory must not Null or Blank")
    @Size(min=3, max=50, message = "Transaction Category must be aleast 3 alphanumeric")
    private String transactionCategory;

    @NotBlank(message = "Sender Name must not be Null Or Blank")
    private String senderName;

    @NotBlank(message = "Receiver name must not be Null or Blank")
    private String receiverName;

    public TransferSimulationDTO(
            String debitAccountNumber,
            String benefAccountNumber,
            BigDecimal amount,
            String tranType,
            String tranCrncy,
            String tranNarration,
            String paymentReference,
            String transactionCategory,
            String receiverName,
            String senderName) {
        super();
        this.debitAccountNumber = debitAccountNumber;
        this.benefAccountNumber = benefAccountNumber;
        this.amount = amount;
        this.tranType = tranType;
        this.tranCrncy = tranCrncy;
        this.tranNarration = tranNarration;
        this.paymentReference = paymentReference;
        this.transactionCategory = transactionCategory;
        this.receiverName = receiverName;
        this.senderName = senderName;
    }
}
