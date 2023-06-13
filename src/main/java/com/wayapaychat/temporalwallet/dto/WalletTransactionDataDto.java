package com.wayapaychat.temporalwallet.dto;

import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.pojo.MyData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class WalletTransactionDataDto {
    private double currentBalance;
    private Long userId;
    private String custName;
    private String accountNo;
    private String email;
    private String phone;
    private String transactionReferenceNumber;
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
}
