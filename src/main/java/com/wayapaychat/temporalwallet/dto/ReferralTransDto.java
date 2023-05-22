package com.wayapaychat.temporalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class ReferralTransDto {
    private Long userId;
    private String custName;
    private String accountNo;
    private String email;
    private String phone;
    private String transactionReferenceNumber;
    private String transactionType;
    private String transactionCategory;
    private BigDecimal amount;
}
