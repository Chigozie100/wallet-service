package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAccountStatement;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.pojo.AdminUserTransferDto;
import com.wayapaychat.temporalwallet.pojo.MifosTransactionPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.WalletToWalletDto;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import com.wayapaychat.temporalwallet.util.ReqIPUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransAccountServiceImpl implements TransAccountService {

	@Autowired
	WalletUserRepository walletUserRepository;

	@Autowired
	WalletAccountRepository walletAccountRepository;

	@Autowired
	ReqIPUtils reqIPUtils;

	@Autowired
	TemporalWalletDAO tempwallet;

	@Autowired
	WalletTransactionRepository walletTransactionRepository;

	@Override
	public ApiResponse<TransactionRequest> makeTransaction(String command, TransactionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiResponse<TransactionRequest> walletToWalletTransfer(String command, WalletToWalletDto walletDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiResponse<TransactionRequest> adminTransferForUser(String command, AdminUserTransferDto adminTranser) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiResponse<TransactionRequest> transferUserToUser(String command, TransactionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiResponse<Page<Transactions>> getTransactionByWalletId(int page, int size, Long walletId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiResponse<Page<Transactions>> getTransactionByType(int page, int size, String transactionType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiResponse<Page<Transactions>> findByAccountNumber(int page, int size, String accountNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiResponse<?> makeWalletTransaction(String command, MifosTransactionPojo transactionPojo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ApiResponse<?> sendMoney(TransferTransactionDTO transfer) {
		String fromAccountNumber = transfer.getDebitAccountNumber();
        String toAccountNumber = transfer.getBenefAccountNumber();
        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
        ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
        try {
			String tranId = createTransaction(fromAccountNumber, toAccountNumber, 
					transfer.getTranCrncy(), transfer.getAmount(),tranType, transfer.getTranNarration());
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if(tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			if(!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}       
		return resp;
	}

	public String createTransaction(String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration)
			throws Exception {
		try {
			// Does account exist
			WalletAccount accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
			WalletAccount accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			String compareDebit = tempwallet.GetSecurityTest(debitAcctNo);
			log.info(compareDebit);
			String secureDebit = reqIPUtils.WayaDecrypt(accountDebit.getHashed_no());
			log.info(secureDebit);
			String[] keyDebit = secureDebit.split(Pattern.quote("|"));
			if ((!keyDebit[1].equals(accountDebit.getAccountNo()))
					|| (!keyDebit[2].equals(accountDebit.getProduct_code()))
					|| (!keyDebit[3].equals(accountDebit.getAcct_crncy_code()))) {
				return "DJGO|DEBIT ACCOUNT DATA INTEGRITY ISSUE";
			}
            
			log.info(accountCredit.getHashed_no());
			String compareCredit = tempwallet.GetSecurityTest(creditAcctNo);
			log.info(compareCredit);
			String secureCredit = reqIPUtils.WayaDecrypt(accountCredit.getHashed_no());
			log.info(secureCredit);
			String[] keyCredit = secureCredit.split(Pattern.quote("|"));
			if ((!keyCredit[1].equals(accountCredit.getAccountNo()))
					|| (!keyCredit[2].equals(accountCredit.getProduct_code()))
					|| (!keyCredit[3].equals(accountCredit.getAcct_crncy_code()))) {
				return "DJGO|CREDIT ACCOUNT DATA INTEGRITY ISSUE";
			}
			// Check for Amount Limit
			if (!accountDebit.getAcct_ownership().equals("O")) {
				
				Long userId = Long.parseLong(keyDebit[0]);
				WalletUser user = walletUserRepository.findByUserId(userId);
				BigDecimal AmtVal = new BigDecimal(user.getCust_debit_limit());
				if (amount.compareTo(AmtVal) == -1) {
					return "DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
				
				if(new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
				
				if(new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
			}
			String tranId = "";
			if (tranType.getValue().equalsIgnoreCase("MONEY")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}
			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "D", accountDebit.getGl_code());
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "C", accountCredit.getGl_code());
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);

			double clrbalAmtDr = accountDebit.getClr_bal_amt() + amount.doubleValue();
			double cumbalDrAmtDr = accountDebit.getCum_dr_amt() + amount.doubleValue();
			accountDebit.setLast_tran_id_dr(tranId);
			accountDebit.setClr_bal_amt(clrbalAmtDr);
			accountDebit.setCum_dr_amt(cumbalDrAmtDr);
			accountDebit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountDebit);

			double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
			accountCredit.setLast_tran_id_cr(tranId);
			accountCredit.setClr_bal_amt(clrbalAmtCr);
			accountCredit.setCum_cr_amt(cumbalCrAmtCr);
			accountCredit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountCredit);
			return tranId;
		} catch (Exception e) {
            e.printStackTrace();
            return ("DJGO|" + e.getMessage());
		}

	}

	@Override
	public ApiResponse<?> getStatement(String accountNumber) {
		WalletAccountStatement statement = null;
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
		if(account == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		}
		List<WalletTransaction> transaction = walletTransactionRepository.findByAcctNumEquals(accountNumber);
		if(transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		statement = new WalletAccountStatement(new BigDecimal(account.getClr_bal_amt()), transaction);
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", statement);
	}

}
