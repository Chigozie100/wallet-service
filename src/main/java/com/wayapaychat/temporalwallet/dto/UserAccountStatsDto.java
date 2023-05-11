package com.wayapaychat.temporalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserAccountStatsDto {
    BigDecimal totalTrans = BigDecimal.ZERO;
    BigDecimal totalRevenue = BigDecimal.ZERO;
    BigDecimal totalIncoming = BigDecimal.ZERO;
    BigDecimal totalOutgoing = BigDecimal.ZERO;
    String userId;

}
