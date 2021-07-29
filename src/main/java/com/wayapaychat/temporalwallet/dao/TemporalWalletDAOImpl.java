package com.wayapaychat.temporalwallet.dao;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.mapper.AccountStatementMapper;

import lombok.extern.slf4j.Slf4j;
@Repository
@Slf4j
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
	
	public List<AccountStatementDTO> fetchTransaction(String acctNo) {
		List<AccountStatementDTO> accountList = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no, account_no,tran_amount,tran_narrate,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id and a.account_no = ?");
		String sql = query.toString();
		try {
			AccountStatementMapper rowMapper = new AccountStatementMapper();
			Object[] params = new Object[] { acctNo };
			accountList = jdbcTemplate.query(sql, rowMapper, params);
			return accountList;
		} catch (Exception ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			return null;
		}
	}

}
