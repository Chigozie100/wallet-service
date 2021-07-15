package com.wayapaychat.temporalwallet.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.wayapaychat.temporalwallet.entity.WalletAccount;

public class TemporalWalletDAOImpl implements TemporalWalletDAO {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public WalletAccount GetCommission(int cifId) {
		StringBuilder query = new StringBuilder();
		query.append("select * from m_wallet_account where cif_id = ? AND acct_name like '%COMMISSION%'");
		return null;
	}

}
