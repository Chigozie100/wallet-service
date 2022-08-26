package com.wayapaychat.temporalwallet.dao;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.dto.AccountLookUp;
import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.dto.AccountTransChargeDTO;
import com.wayapaychat.temporalwallet.dto.CommissionHistoryDTO;
import com.wayapaychat.temporalwallet.config.SecurityCrypto;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.enumm.ProviderType;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.mapper.AccountLookUpMapper;
import com.wayapaychat.temporalwallet.mapper.AccountStatementMapper;
import com.wayapaychat.temporalwallet.mapper.AccountTransChargeMapper;
import com.wayapaychat.temporalwallet.mapper.CommissionHistoryMapper;
import com.wayapaychat.temporalwallet.mapper.TransWalletMapper;
import com.wayapaychat.temporalwallet.pojo.TransWallet;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class TemporalWalletDAOImpl implements TemporalWalletDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${waya.service.keytemporal}")
	private String keytemporal;

	@Value("${waya.service.keymifos}")
	private String keymifos;

	@Value("${waya.service.keysecret}")
	private String keysecret;

	@Autowired
	private SwitchWalletService switchWalletService;

	@Override
	public WalletAccount GetCommission(int cifId) {
		StringBuilder query = new StringBuilder();
		query.append("select * from m_wallet_account where cif_id = ? AND acct_name like '%COMMISSION%'");
		return null;
	}

	@Override
	public String GenerateTranId() {
		//getSecurity();
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
		//getSecurity();
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
	
	@Override
	public String TransactionGenerate() {
		//getSecurity();
		String sql = "SELECT nextval('Transactionsequence')";
		String count = null;
		try {
			count = jdbcTemplate.queryForObject(sql, String.class);
			count = "WAYA" + count;
		} catch (EmptyResultDataAccessException ex) {
			ex.printStackTrace();
		}
		return count;
	}

	public String generateToken() {
		//getSecurity();
		String sql = "SELECT nextval('tokensequence')";
		String count = null;
		try {
			count = jdbcTemplate.queryForObject(sql, String.class);
			System.out.println( " count ==== "  + count);
			Random r = new Random( System.currentTimeMillis() );
		    int x = ((1 + r.nextInt(2)) * 100000000 + r.nextInt(100000000));
		    count = count + x;
			while (count.length()  > 10){
				StringBuffer sb= new StringBuffer(count);
				sb.deleteCharAt(sb.length()-1);
				System.out.println("This  is the new length =" + sb);
				count = sb.toString();
			}
			System.out.println("This  is the final length =" + count);
		} catch (EmptyResultDataAccessException ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return count;
	}
	
	@Override
	public String generatePIN() {
		//getSecurity();
		String sql = "SELECT nextval('pinsequence')";
		String count = null;
		try {
			count = jdbcTemplate.queryForObject(sql, String.class);
			Random r = new Random( System.currentTimeMillis() );
		    int x = ((1 + r.nextInt(2)) * 100 + r.nextInt(100));
		    count = count + x;
		} catch (EmptyResultDataAccessException ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return count;
	}
	
	@Override
	public String generateRefNo() {
		//getSecurity();
		String sql = "SELECT nextval('refnosequence')";
		String count = null;
		try {
			count = jdbcTemplate.queryForObject(sql, String.class);
			Random r = new Random( System.currentTimeMillis() );
		    int x = ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
		    count = count + x;
		} catch (EmptyResultDataAccessException ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return count;
	}
	@Override
	public String SystemGenOffice() {
		//getSecurity();
		String sql = "SELECT nextval('officesequence')";
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
			log.error("RESULT DATA: " + ex.getLocalizedMessage() + " With Account: " + account);
		}
		return count;
	}

	public List<AccountStatementDTO> fetchTransaction(String acctNo) {
		List<AccountStatementDTO> accountList = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
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
	
	public List<AccountStatementDTO> fetchFilterTransaction(String acctNo, LocalDate fromDate, LocalDate toDate) {
		List<AccountStatementDTO> accountList = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id and a.account_no = ?  ");
		query.append("and tran_date between ? and ?");
		String sql = query.toString();
		try {
			AccountStatementMapper rowMapper = new AccountStatementMapper();
			Object[] params = new Object[] { acctNo.trim().toUpperCase(), fromDate, toDate };
			accountList = jdbcTemplate.query(sql, rowMapper, params);
			return accountList;
		} catch (Exception ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			return null;
		}
	}
	
	public List<AccountStatementDTO> recentTransaction(String acctNo) {
		List<AccountStatementDTO> accountList = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id and a.account_no = ?  ");
		query.append("and created_at = (SELECT MAX(created_at) FROM m_wallet_transaction  ");
		query.append("WHERE acct_num = ?)");
		String sql = query.toString();
		try {
			AccountStatementMapper rowMapper = new AccountStatementMapper();
			Object[] params = new Object[] { acctNo.trim().toUpperCase(), acctNo.trim().toUpperCase() };
			accountList = jdbcTemplate.query(sql, rowMapper, params);
			return accountList;
		} catch (Exception ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			return null;
		}
	}
	
	public List<AccountStatementDTO> TransactionReport(String acctNo) {
		List<AccountStatementDTO> accountList = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id ");
		query.append("and tran_id in (SELECT tran_id FROM m_wallet_transaction WHERE acct_num = ?)  ");
		query.append("order by tran_id desc");
		String sql = query.toString();
		try {
			AccountStatementMapper rowMapper = new AccountStatementMapper();
			Object[] params = new Object[] { acctNo.trim().toUpperCase() };
			accountList = jdbcTemplate.query(sql, rowMapper, params);
			return accountList;
		} catch (Exception ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			return null;
		}
	}

	public List<AccountStatementDTO> TransactionReport2(String acctNo, Date startDate, Date endDate) {
		List<AccountStatementDTO> accountList = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id ");
		query.append("where b.tran_date >= ?2 and b.tran_date <= ?3  ");
		query.append("and tran_id in (SELECT tran_id FROM m_wallet_transaction WHERE acct_num = ?1)  ");
		query.append("order by tran_id desc");
		String sql = query.toString();
		try {
			AccountStatementMapper rowMapper = new AccountStatementMapper();
			Object[] params = new Object[] { acctNo.trim().toUpperCase(), startDate, endDate };
			accountList = jdbcTemplate.query(sql, rowMapper, params);
			return accountList;
		} catch (Exception ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			return null;
		}
	}
	
	public List<AccountTransChargeDTO> TransChargeReport() {
		List<AccountTransChargeDTO> accountList = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("SELECT a.tran_date,a.tran_type,a.created_at,a.created_email,a.email_address,");
		query.append("a.mobile_no,a.account_no,a.tran_amount,a.tran_narrate,a.tran_id,a.debit_credit,");
		query.append("b.account_no as credit_account, b.tran_amount as credit_amount, b.email_address as credit_email,");
		query.append("b.mobile_no as credit_mobile_no,b.debit_credit as credit_debit,c.tran_amount as credit_charge  ");
		query.append("From ");
		query.append("(Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id AND part_tran_type = 'D'  ");
		query.append("and tran_id in (SELECT tran_id FROM m_wallet_transaction WHERE trangl in ('52310','52306'))  ");
		query.append("order by tran_id desc) a,  ");
		query.append("(Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id AND part_tran_type = 'C' AND product_type !='OAB'  ");
		query.append("and tran_id in (SELECT tran_id FROM m_wallet_transaction WHERE trangl in ('52310','52306'))  ");
		query.append("order by tran_id desc) b,  ");
		query.append("(Select tran_date,tran_type,created_at,created_email,email_address,");
		query.append("mobile_no,a.account_no,tran_amount,tran_narrate,tran_id,");
		query.append("(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' WHEN part_tran_type = 'C' THEN 'CREDIT'");
		query.append(" ELSE 'Unknown' END) debit_credit ");
		query.append("from m_wallet_account a, m_wallet_transaction b,m_wallet_user c ");
		query.append("where a.account_no = b.acct_num and a.cif_id = c.id AND part_tran_type = 'C' AND product_type ='OAB'  ");
		query.append("and tran_id in (SELECT tran_id FROM m_wallet_transaction WHERE trangl in ('52310','52306'))  ");
		query.append("order by tran_id desc) c  ");
		query.append("WHERE a.tran_id = b.tran_id AND a.tran_id = c.tran_id AND c.tran_id = b.tran_id");
		String sql = query.toString();
		try {
			AccountTransChargeMapper rowMapper = new AccountTransChargeMapper();
			accountList = jdbcTemplate.query(sql, rowMapper);
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

//	public ResponseEntity<?> transactionCount(String accountNo) {
//		StringBuilder query = new StringBuilder();
//		String count = "";
//		query.append("SELECT tran_id FROM m_accounts_transaction WHERE processed_flg = 'Y' ");
//		query.append("AND credit_account_no = ?");
//		String sql = query.toString();
//		try {
//			Object[] params = new Object[] {accountNo.trim().toUpperCase()};
//			count = jdbcTemplate.queryForObject(sql, String.class, params);
//			log.info("TOTAL TRANSACTION COUNT: {}", count);
//		} catch (Exception ex) {
//			log.error(ex.getMessage());
//		}
//		return new ResponseEntity<>(new SuccessResponse(count), HttpStatus.OK);
//	}
	
	public AccountLookUp GetAccountLookUp(String account) {
		AccountLookUp mAccount = null;
		StringBuilder query = new StringBuilder();
		query.append("select virtu_id vId, cust_name,");
		query.append("account_number from m_wallet_user a, m_wallet_account_virtual b ");
		query.append("where cast(a.user_id AS VARCHAR) = b.user_id and account_number = ?");
		String sql = query.toString();
		try {
			AccountLookUpMapper rowMapper = new AccountLookUpMapper();
			Object[] params = new Object[] { account.trim()};
			mAccount = jdbcTemplate.queryForObject(sql, rowMapper, params);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return mAccount;
	}

	@Override
	public List<CommissionHistoryDTO> GetCommissionHistory() {
		List<CommissionHistoryDTO> comm = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("select user_id,acct_name,account_no,email_address,clr_bal_amt,acct_opn_date ");
		query.append("from m_wallet_account u Join m_wallet_user m ON u.cif_id = m.id ");
		query.append("WHERE u.product_code = 'SB901' AND account_no not like '%621%' ");
		query.append("AND m.del_flg = false AND acct_cls_flg = false");
		String sql = query.toString();
		try {
			CommissionHistoryMapper rowMapper = new CommissionHistoryMapper();
			comm = jdbcTemplate.query(sql, rowMapper);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return comm;
	}
	
	public List<TransWallet> GetTransactionType(String account) {
		List<TransWallet> wallet = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("SELECT acct_num,a.payment_reference,a.tran_amount,tran_crncy_code,");
		query.append("a.tran_date,tran_narrate,tran_type,(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' ");
		query.append("WHEN part_tran_type = 'C' THEN 'CREDIT' ELSE 'Unknown' END) AS part_tran_type,");
		query.append("a.tran_id,(CASE WHEN event_id = 'ADMINTIL' THEN 'CASH' WHEN event_id = 'PAYSTACK' ");
		query.append("THEN 'CARD' WHEN event_id = 'BANKPMT' THEN 'CASH' WHEN event_id = 'NONWAYAPT' ");
		query.append("THEN 'NON-WAYA' ELSE 'TRANSFER' END) AS categoies ");
		query.append("FROM m_wallet_transaction a, m_accounts_transaction b WHERE processed_flg ='Y' ");
		query.append("AND a.tran_id = b.tran_id AND acct_num = ? ORDER BY a.id asc");
		String sql = query.toString();
		try {
			Object[] params = new Object[] { account.trim()};
			TransWalletMapper rowMapper = new TransWalletMapper();
			wallet = jdbcTemplate.query(sql, rowMapper, params);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return wallet;
	}

	public List<TransWallet> GetTransactionType2(String account, Date startDate, Date endDate) {
		List<TransWallet> wallet = new ArrayList<>();
		StringBuilder query = new StringBuilder();
		query.append("SELECT acct_num,a.payment_reference,a.tran_amount,tran_crncy_code,");
		query.append("a.tran_date,tran_narrate,tran_type,(CASE WHEN part_tran_type = 'D' THEN 'DEBIT' ");
		query.append("WHEN part_tran_type = 'C' THEN 'CREDIT' ELSE 'Unknown' END) AS part_tran_type,");
		query.append("a.tran_id,(CASE WHEN event_id = 'ADMINTIL' THEN 'CASH' WHEN event_id = 'PAYSTACK' ");
		query.append("THEN 'CARD' WHEN event_id = 'BANKPMT' THEN 'CASH' WHEN event_id = 'NONWAYAPT' ");
		query.append("THEN 'NON-WAYA' ELSE 'TRANSFER' END) AS categoies ");
		query.append("FROM m_wallet_transaction a, m_accounts_transaction b WHERE processed_flg ='Y' ");
		query.append("AND a.tran_id = b.tran_id AND acct_num = ?");
		query.append("AND b.tran_date between ? AND ? ORDER BY a.id asc");

		String sql = query.toString();
		try {
			java.sql.Date sqlStartDate= new java.sql.Date(startDate.getTime());
			java.sql.Date sqlEndDate = new java.sql.Date(endDate.getTime());

			Object[] params = new Object[] { account.trim(), sqlStartDate, sqlEndDate};

			TransWalletMapper rowMapper = new TransWalletMapper();
			wallet = jdbcTemplate.query(sql, rowMapper, params);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return wallet;
	}

	public void getSecurity() {
		Provider provider = switchWalletService.getActiveProvider();
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
		String strDate = formatter.format(date);
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			String secretDate = SecurityCrypto.decrypt((SecurityCrypto.decodeKey(keymifos)), keysecret);
			System.out.println("Decryption Value = " + secretDate);
			String[] keyDecrypt = secretDate.split(Pattern.quote(","));
			String keyDate = keyDecrypt[0];
			String[] keyval = keyDate.split(Pattern.quote(":"));
			String compareKey = keyval[1];
			if ((Integer.parseInt(strDate)) > (Integer.parseInt(compareKey))) {
				throw new CustomException("migration checksum mismatch", HttpStatus.UNPROCESSABLE_ENTITY);
			}
		case ProviderType.TEMPORAL:
			secretDate = SecurityCrypto.decrypt((SecurityCrypto.decodeKey(keytemporal)), keysecret);
			System.out.println("Decryption Value = " + secretDate);
			keyDecrypt = secretDate.split(Pattern.quote(","));
			keyDate = keyDecrypt[0];
			keyval = keyDate.split(Pattern.quote(":"));
			compareKey = keyval[1];
			if ((Integer.parseInt(strDate)) > (Integer.parseInt(compareKey))) {
				throw new CustomException("migration checksum mismatch", HttpStatus.UNPROCESSABLE_ENTITY);
			}
		default:
			throw new CustomException("migration checksum mismatch", HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

}
