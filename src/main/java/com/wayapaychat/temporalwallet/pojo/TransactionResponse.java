package com.wayapaychat.temporalwallet.pojo;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

	public Long id;
    public Long walletId;
    public String transactionType;
    public double amount;
    public double runningBalance;
    public LocalDate transactionDate;
}
