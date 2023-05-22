package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CustomerTransactionSumary { 

    private BigDecimal totalBalance; 
    private BigDecimal totalDeposit; 
    private BigDecimal totalWithdrawal;

    public CustomerTransactionSumary(BigDecimal totalBalance, BigDecimal totalDeposit, BigDecimal totalWithdrawal) {
        this.totalBalance = totalBalance;
        this.totalDeposit = totalDeposit;
        this.totalWithdrawal = totalWithdrawal;
    } 

}
