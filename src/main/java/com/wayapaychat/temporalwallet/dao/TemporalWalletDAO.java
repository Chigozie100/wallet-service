package com.wayapaychat.temporalwallet.dao;

import com.wayapaychat.temporalwallet.entity.WalletAccount;

public interface TemporalWalletDAO {
	
	WalletAccount GetCommission(int cifId);

}
