package com.wayapaychat.temporalwallet.dao;

import java.math.BigDecimal;
import java.util.List;

import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.entity.WalletAccount;

public interface TemporalWalletDAO {
	
	WalletAccount GetCommission(int cifId);
	
	String GenerateTranId();
	
	String SystemGenerateTranId();
	
	String GetSecurityTest(String account);
	
	List<AccountStatementDTO> fetchTransaction(String acctNo);
	
	int PaymenttranInsert(String event, String debitAccountNo, String creditAccountNo, BigDecimal amount,
			String paymentReference);
	
	void updateTransaction(String paymentReference, BigDecimal amount, String tranId);
	
	String transactionCount(String paymentReference, String accountNo);

}
