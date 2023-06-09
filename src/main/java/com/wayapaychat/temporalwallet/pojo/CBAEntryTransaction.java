package com.wayapaychat.temporalwallet.pojo;

import java.math.BigDecimal;

import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class CBAEntryTransaction {
 
    private String accountNo;
    private BigDecimal amount;

    private Integer tranPart;
    private String tranId;
    private TransactionTypeEnum tranType;
    private String tranCrncy;
    private String tranNarration;
	private String paymentReference;
	private CategoryType transactionCategory;
	private String senderName;
	private String receiverName;
    private MyData userToken;
    private Long sessionID;
    private BigDecimal chargeAmount;
    private BigDecimal vat;


    public CBAEntryTransaction(MyData userToken, Long sessionID, String paymentReference, CategoryType transactionCategory, String accountNo, String tranNarration, BigDecimal amount) {
        this.paymentReference = paymentReference;
        this.transactionCategory = transactionCategory;
        this.accountNo = accountNo;
        this.tranNarration = tranNarration;
        this.amount = amount;

        this.userToken = userToken;
        this.senderName = "";
	    this.receiverName = "";
        this.sessionID = sessionID;
    }

    public CBAEntryTransaction(MyData userToken, Long sessionID, String tranId, String paymentReference, CategoryType transactionCategory, String accountNo, String tranNarration,BigDecimal amount,
                Integer tranPart, TransactionTypeEnum tranType, String senderName, String receiverName,BigDecimal chargeAmount, BigDecimal vat) {
        this.accountNo = accountNo; 
        this.amount = amount;
        this.tranPart = tranPart;
        this.tranId = tranId;
        this.tranType = tranType;
        this.tranNarration = tranNarration;
        this.paymentReference = paymentReference;
        this.transactionCategory = transactionCategory;

        this.userToken = userToken;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.sessionID = sessionID;
        this.chargeAmount = chargeAmount;
        this.vat = vat;
    }



}