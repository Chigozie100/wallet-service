package com.wayapaychat.temporalwallet.dao;

import com.wayapaychat.temporalwallet.entity.WalletAccount;

public interface TemporalWalletDAO {
	
	WalletAccount GetCommission(int cifId);
	String GenerateTranId();
	String SystemGenerateTranId();
	String GetSecurityTest(String account);

}
