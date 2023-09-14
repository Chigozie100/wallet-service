package com.wayapaychat.temporalwallet.dto.kyc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class KycTierDataDto {
    private long userId;
    private String profileId;
    private String tierName;
    private long orderLevel;
    private BigDecimal singleTransactionLimit;
    private BigDecimal dailyTransactionLimit;
    private long dailyTransactionLimitCount;
}
