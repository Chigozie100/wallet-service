package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAccountStatement;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletTeller;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.WalletToWalletDto;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletTellerRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
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

	@Autowired
	ParamDefaultValidation paramValidation;
	
	@Autowired
	WalletTellerRepository walletTellerRepository;
	
	@Autowired
	WalletEventRepository walletEventRepository;

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
	public ApiResponse<?> adminTransferForUser(String command, AdminUserTransferDTO transfer) {
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			String tranId = createAdminTransaction(transfer.getAdminUserId(), toAccountNumber, transfer.getTranCrncy(),
					transfer.getAmount(), tranType, transfer.getTranNarration(),transfer.getPaymentReference(), command);
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	public ApiResponse<?> cashTransferByAdmin(String command, WalletAdminTransferDTO transfer) {
		Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumber());
		if(!wallet.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE NO DOES NOT EXIST", null);
		}
		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if(!defaultAcct.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
		}
		String toAccountNumber = defaultAcct.get().getAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			String tranId = createAdminTransaction(transfer.getAdminUserId(), toAccountNumber, transfer.getTranCrncy(),
					transfer.getAmount(), tranType, transfer.getTranNarration(),transfer.getPaymentReference(), command);
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	@Override
	public ApiResponse<?> EventTransferPayment(EventPaymentDTO transfer) {
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			String tranId = createEventTransaction(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
					transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	@Override
	public ApiResponse<TransactionRequest> transferUserToUser(String command, TransactionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public ApiResponse<Page<WalletTransaction>> findAllTransaction(int page, int size) {
		Pageable paging = PageRequest.of(page, size);
		Page<WalletTransaction> transaction = walletTransactionRepository.findAll(paging);
		if (transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
	}

	@Override
	public ApiResponse<Page<WalletTransaction>> getTransactionByWalletId(int page, int size, Long walletId) {
		Optional<WalletAccount> account = walletAccountRepository.findById(walletId);
		if (!account.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		}
		WalletAccount acct = account.get();
		Pageable sortedByName = PageRequest.of(page, size, Sort.by("tranDate"));
		Page<WalletTransaction> transaction = walletTransactionRepository.findAllByAcctNum(acct.getAccountNo(),
				sortedByName);
		if (transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
	}

	@Override
	public ApiResponse<Page<Transactions>> getTransactionByType(int page, int size, String transactionType) {
		return null;
	}

	@Override
	public ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber) {
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
		if (account == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		}
		Pageable sortedByName = PageRequest.of(page, size, Sort.by("tranDate"));
		Page<WalletTransaction> transaction = walletTransactionRepository.findAllByAcctNum(accountNumber, sortedByName);
		if (transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
	}

	@Override
	public ApiResponse<?> makeWalletTransaction(String command, TransferTransactionDTO transactionPojo) {
		String fromAccountNumber = transactionPojo.getDebitAccountNumber();
		String toAccountNumber = transactionPojo.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transactionPojo.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			String tranId = createTransaction(fromAccountNumber, toAccountNumber, transactionPojo.getTranCrncy(),
					transactionPojo.getAmount(), tranType, transactionPojo.getTranNarration(), transactionPojo.getPaymentReference());
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	@Override
	public ApiResponse<?> sendMoney(TransferTransactionDTO transfer) {
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
					transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	@Override
	public ApiResponse<?> sendMoneyCharge(WalletTransactionChargeDTO transfer) {
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			String tranId = createChargeTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
					transfer.getAmount(), tranType, transfer.getTranNarration(), 
					transfer.getPaymentReference(), transfer.getEventChargeId());
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	@Override
	public ApiResponse<?> sendMoneyCustomer(WalletTransactionDTO transfer) {
		Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumber());
		if(!wallet.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE NO DOES NOT EXIST", null);
		}
		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if(!defaultAcct.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = defaultAcct.get().getAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
					transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createTransaction(String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef) throws Exception {
		try {
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
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
				if (AmtVal.compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
			}
			// AUth Security check
			// **********************************************

			// **********************************************
			String tranId = "";
			if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}
			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "D", accountDebit.getGl_code(),paymentRef);
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "C", accountCredit.getGl_code(),paymentRef);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);

			double clrbalAmtDr = accountDebit.getClr_bal_amt() - amount.doubleValue();
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
	
	public String createChargeTransaction(String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String eventCharge) throws Exception {
		try {
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletEventCharges> wevent = walletEventRepository.findByEventId(eventCharge);
			if (!wevent.isPresent()) {
				return "DJGO|Event Charge Does Not Exist";
			}
			WalletEventCharges event = wevent.get();
			// Does account exist
			WalletAccount accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
			WalletAccount accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository.findByUserPlaceholder(event.getPlaceholder(), event.getCrncyCode(), accountDebit.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO CHARGE TILL ACCOUNT EXIST";
			}
			WalletAccount chargeTill = accountDebitTeller.get();
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
				if (AmtVal.compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
			}
			// AUth Security check
			// **********************************************

			// **********************************************
			String tranId = "";
			if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}
			String tranNarrate = "WALLET-" + tranNarration;
			BigDecimal chargeAmt = event.getTranAmt().add(amount);
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), chargeAmt, tranType,
					tranNarrate, new Date(), tranCrncy, "D", accountDebit.getGl_code(),paymentRef);
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "C", accountCredit.getGl_code(),paymentRef);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);

			double clrbalAmtDr = accountDebit.getClr_bal_amt() - chargeAmt.doubleValue();
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
			
			double clrbalAmtChg = chargeTill.getClr_bal_amt() + event.getTranAmt().doubleValue();
			double cumbalCrAmtChg = chargeTill.getCum_cr_amt() + event.getTranAmt().doubleValue();
			chargeTill.setLast_tran_id_cr(tranId);
			chargeTill.setClr_bal_amt(clrbalAmtChg);
			chargeTill.setCum_cr_amt(cumbalCrAmtChg);
			chargeTill.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(chargeTill);
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createAdminTransaction(String adminUserId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String command) throws Exception {
		try {
			
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletTeller> wteller = walletTellerRepository.findByUserSol(Long.valueOf(adminUserId), tranCrncy, "0000");
			if (!wteller.isPresent()) {
				return "DJGO|NO TELLER TILL CREATED";
			}
			WalletTeller teller = wteller.get();
			boolean validate2 = paramValidation.validateDefaultCode(teller.getAdminCashAcct(),"Batch Account");
			if(!validate2) {
				return "DJGO|Batch Account Validation Failed";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository.findByUserPlaceholder(teller.getAdminCashAcct(), teller.getCrncyCode(), teller.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO TELLER TILL ACCOUNT EXIST";
			}
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if(command.toUpperCase().equals("CREDIT")) {
			   accountDebit = accountDebitTeller.get();
			   accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}else {
				accountCredit = accountDebitTeller.get();
				accountDebit  = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			if(!accountDebit.getAcct_ownership().equals("O")) {
			String compareDebit = tempwallet.GetSecurityTest(accountDebit.getAccountNo());
			log.info(compareDebit);
			}
			String secureDebit = reqIPUtils.WayaDecrypt(accountDebit.getHashed_no());
			log.info(secureDebit);
			String[] keyDebit = secureDebit.split(Pattern.quote("|"));
			if ((!keyDebit[1].equals(accountDebit.getAccountNo()))
					|| (!keyDebit[2].equals(accountDebit.getProduct_code()))
					|| (!keyDebit[3].equals(accountDebit.getAcct_crncy_code()))) {
				return "DJGO|DEBIT ACCOUNT DATA INTEGRITY ISSUE";
			}

			log.info(accountCredit.getHashed_no());
			if(!accountDebit.getAcct_ownership().equals("O")) {
			String compareCredit = tempwallet.GetSecurityTest(creditAcctNo);
			log.info(compareCredit);
			}
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
				if (AmtVal.compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
			}
			// AUth Security check
			// **********************************************

			// **********************************************
			String tranId = "";
			if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}
			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "D", accountDebit.getGl_code(),paymentRef);
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "C", accountCredit.getGl_code(),paymentRef);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);

			double clrbalAmtDr = accountDebit.getClr_bal_amt() - amount.doubleValue();
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
	
	public String createEventTransaction(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef) throws Exception {
		try {
			
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
			if(!eventInfo.isPresent()) {
				return "DJGO|Event Code Does Not Exist";
			}
			WalletEventCharges charge = eventInfo.get();
			boolean validate2 = paramValidation.validateDefaultCode(charge.getPlaceholder(),"Batch Account");
			if(!validate2) {
				return "DJGO|Event Validation Failed";
			}
			WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (eventAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if(!charge.isChargeCustomer() && charge.isChargeWaya()) {
			   accountDebit = accountDebitTeller.get();
			   accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}else {
				accountCredit = accountDebitTeller.get();
				accountDebit  = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			if(!accountDebit.getAcct_ownership().equals("O")) {
			String compareDebit = tempwallet.GetSecurityTest(accountDebit.getAccountNo());
			log.info(compareDebit);
			}
			String secureDebit = reqIPUtils.WayaDecrypt(accountDebit.getHashed_no());
			log.info(secureDebit);
			String[] keyDebit = secureDebit.split(Pattern.quote("|"));
			if ((!keyDebit[1].equals(accountDebit.getAccountNo()))
					|| (!keyDebit[2].equals(accountDebit.getProduct_code()))
					|| (!keyDebit[3].equals(accountDebit.getAcct_crncy_code()))) {
				return "DJGO|DEBIT ACCOUNT DATA INTEGRITY ISSUE";
			}

			log.info(accountCredit.getHashed_no());
			if(!accountDebit.getAcct_ownership().equals("O")) {
			String compareCredit = tempwallet.GetSecurityTest(creditAcctNo);
			log.info(compareCredit);
			}
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
				if (AmtVal.compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}

				if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
			}
			// AUth Security check
			// **********************************************

			// **********************************************
			//Account Transaction Locks
			//*********************************************
			
			//**********************************************
			String tranId = "";
			if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}
			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "D", accountDebit.getGl_code(),paymentRef);
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, new Date(), tranCrncy, "C", accountCredit.getGl_code(),paymentRef);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);

			double clrbalAmtDr = accountDebit.getClr_bal_amt() - amount.doubleValue();
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
		if (account == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		}
		List<WalletTransaction> transaction = walletTransactionRepository.findByAcctNumEquals(accountNumber);
		if (transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		statement = new WalletAccountStatement(new BigDecimal(account.getClr_bal_amt()), transaction);
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", statement);
	}

}
