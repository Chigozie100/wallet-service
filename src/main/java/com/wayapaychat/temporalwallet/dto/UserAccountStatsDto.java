package com.wayapaychat.temporalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserAccountStatsDto {
    private BigDecimal totalTrans = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalIncoming = BigDecimal.ZERO;
    private BigDecimal totalOutgoing = BigDecimal.ZERO;
    private String accountNumber;
    private String accountType;
    private String userId;

}
