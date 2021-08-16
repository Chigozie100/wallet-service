package com.wayapaychat.temporalwallet.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
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
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,");
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

	public int PaymenttranInsert(String event, String debitAccountNo, String creditAccountNo, BigDecimal amount,
			String paymentReference) {
		Integer record = 0;
		StringBuilder query = new StringBuilder();
		Object[] params = null;
		if (event.isBlank() || event.isEmpty()) {
			query.append("INSERT INTO m_accounts_transaction(debit_account_no,credit_account_no,");
			query.append("tran_date,tran_amount,payment_reference)  ");
			query.append("VALUES(?,?,?,?,?)");
			params = new Object[] { debitAccountNo.trim().toUpperCase(), creditAccountNo.trim().toUpperCase(),
					LocalDate.now(), amount, paymentReference.trim().toUpperCase() };
		} else {
			query.append("INSERT INTO m_accounts_transaction(event_id,credit_account_no,");
			query.append("tran_date,tran_amount,payment_reference)  ");
			query.append("VALUES(?,?,?,?,?)");
			params = new Object[] { event.trim().toUpperCase(),
					creditAccountNo.trim().toUpperCase(), LocalDate.now(), amount,
					paymentReference.trim().toUpperCase() };
		}
		String sql = query.toString();
		try {
			int x = jdbcTemplate.update(sql, params);
			if (x == 1) {
				log.info("ACCOUNT TRANSACTION TABLE INSERTED SUCCESSFUL: {}", x);
				record = x;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage());
			if(ex.getMessage().contains("duplicate")) {
				record = 2;
			}
		}

		return record;

	}
	
	public void updateTransaction(String paymentReference, BigDecimal amount, String tranId) {
		StringBuilder query = new StringBuilder();
		Object[] params = null;
			query.append("Update m_accounts_transaction set processed_flg = 'Y',tran_id = ?  ");
			query.append("WHERE tran_date = ? AND tran_amount = ? AND payment_reference = ? ");
			params = new Object[] { tranId.trim().toUpperCase(),LocalDate.now(),amount, paymentReference.trim().toUpperCase()
					 };
		String sql = query.toString();
		try {
			int x = jdbcTemplate.update(sql, params);
			if (x == 1) {
				log.info("ACCOUNT TRANSACTION TABLE UPDATED SUCCESSFUL: {}", x);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage());
		}
		
	}
	
	public String transactionCount(String paymentReference, String accountNo) {
		StringBuilder query = new StringBuilder();
		String count = "";
		query.append("SELECT tran_id FROM m_accounts_transaction WHERE processed_flg = 'Y' ");
		query.append("AND payment_reference = ? AND credit_account_no = ? AND tran_date = ?");
		String sql = query.toString();
		try {
			Object[] params = new Object[] { paymentReference.trim().toUpperCase(), 
					accountNo.trim().toUpperCase(), LocalDate.now()};
			count = jdbcTemplate.queryForObject(sql, String.class, params);
			log.info("TOTAL TRANSACTION COUNT: {}", count);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return count;
	}

}
