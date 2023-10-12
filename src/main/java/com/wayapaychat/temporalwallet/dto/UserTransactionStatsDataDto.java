package com.wayapaychat.temporalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserTransactionStatsDataDto {
    private BigDecimal totalBalance = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalDeposit = BigDecimal.ZERO;
    private BigDecimal totalWithdrawal = BigDecimal.ZERO;
    private BigDecimal overAllWithdrawalCount = BigDecimal.ZERO;
    private BigDecimal overAllTransferCount = BigDecimal.ZERO;
    private BigDecimal singleAccountWithdrawalCount = BigDecimal.ZERO;
    private BigDecimal singleAccountTransferCount = BigDecimal.ZERO;

    private BigDecimal singleDataTopUpCount = BigDecimal.ZERO;
    private BigDecimal singleAirTimeTopUpCount = BigDecimal.ZERO;
    private BigDecimal singleUtilityCount = BigDecimal.ZERO;
    private BigDecimal singleCableCount = BigDecimal.ZERO;
    private BigDecimal singleBettingCount = BigDecimal.ZERO;

    private BigDecimal totalDataTopUpCount = BigDecimal.ZERO;
    private BigDecimal totalAirTimeTopUpCount = BigDecimal.ZERO;
    private BigDecimal totalUtilityCount = BigDecimal.ZERO;
    private BigDecimal totalCableCount = BigDecimal.ZERO;
    private BigDecimal totalBettingCount = BigDecimal.ZERO;

    private BigDecimal totalPosCount = BigDecimal.ZERO;
    private BigDecimal totalWebCount = BigDecimal.ZERO;
    private BigDecimal totalPosAmount = BigDecimal.ZERO;
    private BigDecimal totalPosCommission = BigDecimal.ZERO;
    private BigDecimal totalWebAmount = BigDecimal.ZERO;
    private BigDecimal totalWebCommission = BigDecimal.ZERO;
}
