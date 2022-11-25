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


    public CBAEntryTransaction(MyData userToken, String paymentReference, CategoryType transactionCategory, String accountNo, String tranNarration, BigDecimal amount) {
        this.paymentReference = paymentReference;
        this.transactionCategory = transactionCategory;
        this.accountNo = accountNo;
        this.tranNarration = tranNarration;
        this.amount = amount;

        this.userToken = userToken;
        this.senderName = "WAYA USER";
	    this.receiverName = "WAYA USER";
    }

    public CBAEntryTransaction(MyData userToken, String tranId, String paymentReference, CategoryType transactionCategory, String accountNo, String tranNarration,BigDecimal amount,
                Integer tranPart, TransactionTypeEnum tranType, String receiverName) {
        this.accountNo = accountNo; 
        this.amount = amount;
        this.tranPart = tranPart;
        this.tranId = tranId;
        this.tranType = tranType;
        this.tranNarration = tranNarration;
        this.paymentReference = paymentReference;
        this.transactionCategory = transactionCategory;
        this.receiverName = receiverName;

        this.userToken = userToken;
        this.senderName = "WAYA USER";
	    this.receiverName = "WAYA USER";
    }

    public CBAEntryTransaction(MyData userToken, String tranId, String paymentReference, CategoryType transactionCategory, String accountNo, String tranNarration,BigDecimal amount,
                Integer tranPart, String senderName, TransactionTypeEnum tranType) {
        this.accountNo = accountNo; 
        this.amount = amount;
        this.tranPart = tranPart;
        this.tranId = tranId;
        this.tranType = tranType;
        this.tranNarration = tranNarration;
        this.paymentReference = paymentReference;
        this.transactionCategory = transactionCategory;
        this.senderName = senderName;

        this.userToken = userToken;
        this.senderName = "WAYA USER";
	    this.receiverName = "WAYA USER";
    }

}