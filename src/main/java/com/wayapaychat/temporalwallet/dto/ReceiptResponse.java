package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ReceiptResponse {

    private Long id;
    
    private String referenceNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+1")
    private Date transactionDate;
    
    private BigDecimal amount;
    
    private String transactionType;
    
    private String receiverName;
    
    private String receiverAccount;
    
    private String receiverBank;
    
    private String senderName;
    
    private String amountInWords;
    
    private String userID;
}
