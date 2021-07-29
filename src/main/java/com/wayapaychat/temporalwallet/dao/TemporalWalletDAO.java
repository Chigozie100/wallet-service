package com.wayapaychat.temporalwallet.dao;

import java.util.List;

import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.entity.WalletAccount;

public interface TemporalWalletDAO {
	
	WalletAccount GetCommission(int cifId);
	
	String GenerateTranId();
	
	String SystemGenerateTranId();
	
	String GetSecurityTest(String account);
	
	List<AccountStatementDTO> fetchTransaction(String acctNo);

}
