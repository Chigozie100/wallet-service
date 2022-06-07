package com.wayapaychat.temporalwallet.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.wayapaychat.temporalwallet.dto.AccountLookUp;
import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.dto.AccountTransChargeDTO;
import com.wayapaychat.temporalwallet.dto.CommissionHistoryDTO;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.pojo.TransWallet;
import org.springframework.http.ResponseEntity;

public interface TemporalWalletDAO {

	WalletAccount GetCommission(int cifId);

	String GenerateTranId();

	String SystemGenerateTranId();
	
	String TransactionGenerate();

	String generateToken();

	String generatePIN();

	String generateRefNo();

	String SystemGenOffice();

	String GetSecurityTest(String account);

	List<AccountStatementDTO> fetchTransaction(String acctNo);

	List<AccountStatementDTO> fetchFilterTransaction(String acctNo, LocalDate fromDate, LocalDate toDate);

	int PaymenttranInsert(String event, String debitAccountNo, String creditAccountNo, BigDecimal amount,
			String paymentReference);

	void updateTransaction(String paymentReference, BigDecimal amount, String tranId);

	String transactionCount(String paymentReference, String accountNo);



	List<AccountStatementDTO> recentTransaction(String acctNo);

	List<AccountStatementDTO> TransactionReport(String acctNo);

	List<AccountTransChargeDTO> TransChargeReport();

	AccountLookUp GetAccountLookUp(String account);

	List<CommissionHistoryDTO> GetCommissionHistory();
	
	List<TransWallet> GetTransactionType(String account);

	List<TransWallet> GetTransactionType2(String account, Date fromdate, Date todate);

}
