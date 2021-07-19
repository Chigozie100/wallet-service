package com.wayapaychat.temporalwallet.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletAccount;
@Repository
public class TemporalWalletDAOImpl implements TemporalWalletDAO {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public WalletAccount GetCommission(int cifId) {
		StringBuilder query = new StringBuilder();
		query.append("select * from m_wallet_account where cif_id = ? AND acct_name like '%COMMISSION%'");
		return null;
	}

	@Override
	public String GenerateTranId() {
		String sql = "SELECT nextval('transequence')";
		String count = null;
		try {
			count = jdbcTemplate.queryForObject(sql, String.class);
			count = "M" + count;
		} catch (EmptyResultDataAccessException ex) {
			ex.printStackTrace();
		}
		return count;
	}

	@Override
	public String SystemGenerateTranId() {
		String sql = "SELECT nextval('syssequence')";
		String count = null;
		try {
			count = jdbcTemplate.queryForObject(sql, String.class);
			count = "S" + count;
		} catch (EmptyResultDataAccessException ex) {
			ex.printStackTrace();
		}
		return count;
	}
	
	public String GetSecurityTest(String account) {
		String sql = "select user_id||'|'||account_no||'|'||product_code||'|'||acct_crncy_code  "; 
		sql = sql + "as record from m_wallet_account a,m_wallet_user b where b.id = a.cif_id  ";
		sql = sql + "and account_no = ? ";
		String count = null;
		try {
			Object[] params = new Object[] { account };
			count = jdbcTemplate.queryForObject(sql, String.class, params);
		} catch (EmptyResultDataAccessException ex) {
			ex.printStackTrace();
		}
		return count;
	}

}
