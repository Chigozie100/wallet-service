package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;
import java.util.List;

import com.wayapaychat.temporalwallet.entity.WalletTransaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WalletAccountStatement {
	
	BigDecimal currentBalance;
    List<WalletTransaction> transactionHistory;

}
