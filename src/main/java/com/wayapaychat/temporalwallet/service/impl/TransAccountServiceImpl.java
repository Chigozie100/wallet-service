package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.dto.AccountTransChargeDTO;
import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.dto.BulkTransactionCreationDTO;
import com.wayapaychat.temporalwallet.dto.BulkTransactionExcelDTO;
import com.wayapaychat.temporalwallet.dto.ClientComTransferDTO;
import com.wayapaychat.temporalwallet.dto.CommissionTransferDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.ExcelTransactionCreationDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaPaymentDTO;
import com.wayapaychat.temporalwallet.dto.OfficeTransferDTO;
import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.UserTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAccountStatement;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WayaTradeDTO;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletTeller;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;
import com.wayapaychat.temporalwallet.pojo.WalletToWalletDto;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletTellerRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import com.wayapaychat.temporalwallet.util.ExcelHelper;
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

	@Autowired
	AuthUserServiceDAO authService;

	@Autowired
	private TokenImpl tokenService;

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
			int intRec = tempwallet.PaymenttranInsert("ADMINTIL", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createAdminTransaction(transfer.getAdminUserId(), toAccountNumber,
						transfer.getTranCrncy(), transfer.getAmount(), tranType, transfer.getTranNarration(),
						transfer.getPaymentReference(), command);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ApiResponse<?> cashTransferByAdmin(String command, WalletAdminTransferDTO transfer) {
		Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumber());
		if (!wallet.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE NO DOES NOT EXIST", null);
		}
		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if (!defaultAcct.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
		}
		String toAccountNumber = defaultAcct.get().getAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("ADMINTIL", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createAdminTransaction(transfer.getAdminUserId(), toAccountNumber,
						transfer.getTranCrncy(), transfer.getAmount(), tranType, transfer.getTranNarration(),
						transfer.getPaymentReference(), command);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	@Override
	public ApiResponse<?> EventTransferPayment(EventPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventTransaction(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	public ApiResponse<?> EventNonPayment(NonWayaPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		String toAccountNumber = transfer.getCustomerDebitAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("NONWAYAPT", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventTransaction("NONWAYAPT", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	public ApiResponse<?> EventNonRedeem(NonWayaPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		String toAccountNumber = transfer.getCustomerDebitAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("NONWAYAPT", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventRedeem("NONWAYAPT", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ApiResponse<?> EventBuySellPayment(WayaTradeDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		String toAccountNumber = transfer.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventTransaction(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	@Override
	public ApiResponse<?> EventCommissionPayment(EventPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getCustomerAccountNumber());
		if (!acctComm.getGl_code().equals("SB901")) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT COMMISSION WALLET", null);
		}
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventCommission(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ApiResponse<?> BankTransferPayment(BankPaymentDTO transfer) {
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("BANKPMT", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = BankTransactionPay("BANKPMT", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						transfer.getBankName());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
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
		// Pageable paging = PageRequest.of(page, size);
		// Page<WalletTransaction> transaction =
		// walletTransactionRepository.findAll(paging);
		Page<WalletTransaction> transaction = walletTransactionRepository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
		if (transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
	}
	
	public ApiResponse<List<WalletTransaction>> findClientTransaction(String tranId) {
		Optional<List<WalletTransaction>> transaction = walletTransactionRepository
				.findByTranIdIgnoreCase(tranId);
		if (!transaction.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction.get());
	}
	
	public ApiResponse<List<AccountStatementDTO>> ReportTransaction(String accountNo) {
		List<AccountStatementDTO> transaction = tempwallet.TransactionReport(accountNo);
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
		Pageable sortedByName = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<WalletTransaction> transaction = walletTransactionRepository.findAllByAcctNum(accountNumber, sortedByName);
		// Page<WalletTransaction> transaction =
		// walletTransactionRepository.findAll(PageRequest.of(page, size,
		// Sort.by(Sort.Direction.DESC, "createdAt")));
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
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber,
					transactionPojo.getAmount(), transactionPojo.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transactionPojo.getTranCrncy(),
						transactionPojo.getAmount(), tranType, transactionPojo.getTranNarration(),
						transactionPojo.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
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
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ApiResponse<?> OfficialMoneyTransfer(OfficeTransferDTO transfer) {
		String fromAccountNumber = transfer.getOfficeDebitAccount();
		String toAccountNumber = transfer.getOfficeCreditAccount();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());

		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
				LocalDate.now(), transfer.getTranCrncy());
		if (!transRef.isEmpty()) {
			Optional<WalletTransaction> ret = transRef.stream()
					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
			if (ret.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
						"Duplicate Payment Reference on the same Day", null);
			}
		}

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ApiResponse<?> OfficialUserTransfer(OfficeUserTransferDTO transfer) {
		String fromAccountNumber = transfer.getOfficeDebitAccount();
		String toAccountNumber = transfer.getCustomerCreditAccount();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());

		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
				LocalDate.now(), transfer.getTranCrncy());
		if (!transRef.isEmpty()) {
			Optional<WalletTransaction> ret = transRef.stream()
					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
			if (ret.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
						"Duplicate Payment Reference on the same Day", null);
			}
		}

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ApiResponse<?> AdminsendMoney(AdminLocalTransferDTO transfer) {
		UserDetailPojo user = authService.AuthUser(transfer.getUserId().intValue());
		if (user == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID USER ID", null);
		}
		if (!user.is_admin()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "USER ID PERFORMING OPERATION IS NOT AN ADMIN",
					null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction.get());
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ApiResponse<?> AdminCommissionMoney(CommissionTransferDTO transfer) {
		UserDetailPojo user = authService.AuthUser(transfer.getUserId().intValue());
		if (user == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID USER ID", null);
		}
		if (!user.is_admin()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "USER ID PERFORMING OPERATION IS NOT AN ADMIN",
					null);
		}
		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getDebitAccountNumber());
		if (!acctComm.getGl_code().equals("SB901")) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT COMMISSION WALLET", null);
		}
		WalletAccount acctDef = walletAccountRepository.findByAccountNo(transfer.getDebitAccountNumber());
		if (!acctDef.isWalletDefault()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT DEFAULT WALLET", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction.get());
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	public ApiResponse<?> ClientCommissionMoney(ClientComTransferDTO transfer) {
		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getDebitAccountNumber());
		if (!acctComm.getGl_code().equals("SB901")) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT COMMISSION WALLET", null);
		}
		WalletAccount acctDef = walletAccountRepository.findByAccountNo(transfer.getDebitAccountNumber());
		if (!acctDef.isWalletDefault()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT DEFAULT WALLET", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction.get());
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
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
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createChargeTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						transfer.getEventChargeId());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction.get());
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	@Override
	public ApiResponse<?> sendMoneyCustomer(WalletTransactionDTO transfer) {
		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
				LocalDate.now(), transfer.getTranCrncy());
		if (!transRef.isEmpty()) {
			Optional<WalletTransaction> ret = transRef.stream()
					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
			if (ret.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
						"Duplicate Payment Reference on the same Day", null);
			}
		}

		Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumber());
		if (!wallet.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE NO DOES NOT EXIST", null);
		}
		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if (!defaultAcct.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = defaultAcct.get().getAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
			} else {
				if (intRec == 2) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
							"Unable to process duplicate transaction", null);
				} else {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createTransaction(String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}
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
			//String compareDebit = tempwallet.GetSecurityTest(debitAcctNo);
			//log.info(compareDebit);
			String secureDebit = reqIPUtils.WayaDecrypt(accountDebit.getHashed_no());
			log.info(secureDebit);
			String[] keyDebit = secureDebit.split(Pattern.quote("|"));
			if ((!keyDebit[1].equals(accountDebit.getAccountNo()))
					|| (!keyDebit[2].equals(accountDebit.getProduct_code()))
					|| (!keyDebit[3].equals(accountDebit.getAcct_crncy_code()))) {
				return "DJGO|DEBIT ACCOUNT DATA INTEGRITY ISSUE";
			}

			log.info(accountCredit.getHashed_no());
			//String compareCredit = tempwallet.GetSecurityTest(creditAcctNo);
			//log.info(compareCredit);
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

			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

			if (!accountCredit.getAcct_ownership().equals("O")) {
				if (accountCredit.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", accountCredit.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
				if (accountCredit.getFrez_code() != null) {
					if (accountCredit.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,n);
			
			n = n + 1;
			
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId,
					email,n);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

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
			// WalletTransactionNotification notify = new
			// WalletTransactionNotification(debitAcctNo, creditAcctNo, String tranMessage,
			// String debitMobileNo, String creditMobileNo)
			log.info("END TRANSACTION");
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createChargeTransaction(String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String eventCharge)
			throws Exception {
		try {
			
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}
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
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(event.getPlaceholder(), event.getCrncyCode(), accountDebit.getSol_id());
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
			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

			if (!accountCredit.getAcct_ownership().equals("O")) {
				if (accountCredit.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", accountCredit.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
				if (accountCredit.getFrez_code() != null) {
					if (accountCredit.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

			String tranId = "";
			if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}
			// MyData tokenData = tokenService.getUserInformation();
			// String email = tokenData != null ? tokenData.getEmail() : "";
			// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

			String tranNarrate = "WALLET-" + tranNarration;
			String tranNarrate1 = "WALLET-" + event.getTranNarration();
			BigDecimal chargeAmt = event.getTranAmt().add(amount);
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), chargeAmt,
					tranType, tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef,
					userId, email,n);
			
			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId,
					email,n);
			
			n = n + 1;
			WalletTransaction tranCharge = new WalletTransaction(tranId, chargeTill.getAccountNo(), event.getTranAmt(),
					tranType, tranNarrate1, LocalDate.now(), tranCrncy, "C", chargeTill.getGl_code(), paymentRef,
					userId, email,n);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			walletTransactionRepository.saveAndFlush(tranCharge);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

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
			log.info("END TRANSACTION");
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createAdminTransaction(String adminUserId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String command) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}

			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletTeller> wteller = walletTellerRepository.findByUserSol(Long.valueOf(adminUserId), tranCrncy,
					"0000");
			if (!wteller.isPresent()) {
				return "DJGO|NO TELLER TILL CREATED";
			}
			WalletTeller teller = wteller.get();
			boolean validate2 = paramValidation.validateDefaultCode(teller.getAdminCashAcct(), "Batch Account");
			if (!validate2) {
				return "DJGO|Batch Account Validation Failed";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(teller.getAdminCashAcct(), teller.getCrncyCode(), teller.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO TELLER TILL ACCOUNT EXIST";
			}
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if (command.toUpperCase().equals("CREDIT")) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else {
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			if (!accountDebit.getAcct_ownership().equals("O")) {
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
			if (!accountDebit.getAcct_ownership().equals("O")) {
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

			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

			if (!accountCredit.getAcct_ownership().equals("O")) {
				if (accountCredit.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", accountCredit.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
				if (accountCredit.getFrez_code() != null) {
					if (accountCredit.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

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

			// MyData tokenData = tokenService.getUserInformation();
			// String email = tokenData != null ? tokenData.getEmail() : "";
			// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,n);
			
			n = n + 1;
			
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId,
					email,n);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

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
			log.info("END TRANSACTION");
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventTransaction(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
			if (!eventInfo.isPresent()) {
				return "DJGO|Event Code Does Not Exist";
			}
			WalletEventCharges charge = eventInfo.get();
			boolean validate2 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
			if (!validate2) {
				return "DJGO|Event Validation Failed";
			}
			WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (eventAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else {
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			if (!accountDebit.getAcct_ownership().equals("O")) {
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
			if (!accountDebit.getAcct_ownership().equals("O")) {
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

			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

			if (!accountCredit.getAcct_ownership().equals("O")) {
				if (accountCredit.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", accountCredit.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
				if (accountCredit.getFrez_code() != null) {
					if (accountCredit.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

			// **********************************************
			// Account Transaction Locks
			// *********************************************

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
			// MyData tokenData = tokenService.getUserInformation();
			// String email = tokenData != null ? tokenData.getEmail() : "";
			// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,n);
			
			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId,
					email,n);
			log.info("TRANSACTION CREATION DEBIT: {} WITH CREDIT: {}", tranDebit.toString(), tranCredit.toString());
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

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
			log.info("END TRANSACTION");
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}
	
	public String createEventRedeem(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
			if (!eventInfo.isPresent()) {
				return "DJGO|Event Code Does Not Exist";
			}
			WalletEventCharges charge = eventInfo.get();
			boolean validate2 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
			if (!validate2) {
				return "DJGO|Event Validation Failed";
			}
			WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (eventAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			if (!accountDebit.getAcct_ownership().equals("O")) {
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
			if (!accountDebit.getAcct_ownership().equals("O")) {
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

			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

			if (!accountCredit.getAcct_ownership().equals("O")) {
				if (accountCredit.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", accountCredit.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
				if (accountCredit.getFrez_code() != null) {
					if (accountCredit.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

			// **********************************************
			// Account Transaction Locks
			// *********************************************

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
			// MyData tokenData = tokenService.getUserInformation();
			// String email = tokenData != null ? tokenData.getEmail() : "";
			// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,n);
			
			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId,
					email,n);
			log.info("TRANSACTION CREATION DEBIT: {} WITH CREDIT: {}", tranDebit.toString(), tranCredit.toString());
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

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
			log.info("END TRANSACTION");
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventCommission(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
			if (!eventInfo.isPresent()) {
				return "DJGO|Event Code Does Not Exist";
			}
			WalletEventCharges charge = eventInfo.get();
			boolean validate2 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
			if (!validate2) {
				return "DJGO|Event Validation Failed";
			}
			WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (eventAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else {
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			if (!accountDebit.getAcct_ownership().equals("O")) {
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
			if (!accountDebit.getAcct_ownership().equals("O")) {
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
			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

			if (!accountCredit.getAcct_ownership().equals("O")) {
				if (accountCredit.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", accountCredit.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
				if (accountCredit.getFrez_code() != null) {
					if (accountCredit.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

			// **********************************************
			// Account Transaction Locks
			// *********************************************

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
			// MyData tokenData = tokenService.getUserInformation();
			// String email = tokenData != null ? tokenData.getEmail() : "";
			// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,n);
			
			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId,
					email,n);
			log.info("TRANSACTION CREATION DEBIT: {} WITH CREDIT: {}", tranDebit.toString(), tranCredit.toString());
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

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
			log.info("END TRANSACTION");
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String BankTransactionPay(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String bk) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}

			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
			if (!eventInfo.isPresent()) {
				return "DJGO|Event Code Does Not Exist";
			}
			WalletEventCharges charge = eventInfo.get();
			boolean validate2 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
			if (!validate2) {
				return "DJGO|Event Validation Failed";
			}
			WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (eventAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else {
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			if (!accountDebit.getAcct_ownership().equals("O")) {
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
			if (!accountDebit.getAcct_ownership().equals("O")) {
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
			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

			if (!accountCredit.getAcct_ownership().equals("O")) {
				if (accountCredit.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";
				
				log.info("Credit Account is: {}", accountCredit.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
				if (accountCredit.getFrez_code() != null) {
					if (accountCredit.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

			// **********************************************
			// Account Transaction Locks
			// *********************************************

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
			// MyData tokenData = tokenService.getUserInformation();
			// String email = tokenData != null ? tokenData.getEmail() : "";
			// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

			String tranNarrate = "WALLET-" + tranNarration + " TO:" + bk;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,n);
			
			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId,
					email,n);
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

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
			log.info("END TRANSACTION");
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

	@Override
	public ApiResponse<?> TranReversePayment(ReverseTransactionDTO reverseDto) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String toDate = dateFormat.format(new Date());
		String tranDate = dateFormat.format(reverseDto.getTranDate());
		SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy");
		Date d1 = sdformat.parse(toDate);
		Date d2 = sdformat.parse(tranDate);
		LocalDate reverseDate = reverseDto.getTranDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		BigDecimal debitAmount = new BigDecimal("0"), creditAmount = new BigDecimal("0");
		ArrayList<String> account = new ArrayList<String>();

		if (d1.compareTo(d2) == 0) {
			List<WalletTransaction> transRe = walletTransactionRepository.findByRevTrans(reverseDto.getTranId(),
					reverseDate, reverseDto.getTranCrncy());
			if (!transRe.isEmpty()) {
				return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "TRANSACTION ALREADY REVERSED", null);
			}
			List<WalletTransaction> transpo = walletTransactionRepository.findByTransaction(reverseDto.getTranId(),
					reverseDate, reverseDto.getTranCrncy());
			if (transpo.isEmpty()) {
				return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "TRANSACTION DOES NOT EXIST", null);
			}
			for (WalletTransaction tran : transpo) {
				if (tran.getPartTranType().equalsIgnoreCase("D")) {
					debitAmount = debitAmount.add(tran.getTranAmount());
				} else {
					creditAmount = creditAmount.add(tran.getTranAmount());
				}
				account.add(tran.getAcctNum());
			}
			int res = debitAmount.compareTo(creditAmount);
			if (res != 0) {
				return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "IMBALANCE TRANSACTION", null);
			}
			String tranId = "";
			if (reverseDto.getTranId().substring(0, 1).equalsIgnoreCase("S")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			for (int i = 0; i < account.size(); i++) {
				int n = 1;
				WalletTransaction trans1 = walletTransactionRepository.findByAcctNumTran(account.get(i),
						reverseDto.getTranId(), reverseDate, reverseDto.getTranCrncy());
				if (trans1.getPartTranType().equalsIgnoreCase("D")) {
					trans1.setPartTranType("C");
				} else {
					trans1.setPartTranType("D");
				}
				trans1.setTranNarrate("REV-" + trans1.getTranNarrate());
				TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("REVERSAL");
				trans1.setRelatedTransId(trans1.getTranId());
				trans1.setTranType(tranType);
				trans1.setTranId(tranId);
				trans1.setTranDate(LocalDate.now());
				PostReverse(trans1,n);
				n++;
			}
			List<WalletTransaction> statement = walletTransactionRepository.findByTransaction(tranId, LocalDate.now(),
					reverseDto.getTranCrncy());
			return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSE SUCCESSFULLY", statement);
		} else {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "TRANSACTION DATE WAS IN PAST", null);
		}
	}

	public void PostReverse(WalletTransaction trans, Integer n) {
		WalletAccount RevAcct = walletAccountRepository.findByAccountNo(trans.getAcctNum());

		MyData tokenData = tokenService.getUserInformation();
		String email = tokenData != null ? tokenData.getEmail() : "";
		String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

		WalletTransaction tranrev = new WalletTransaction(trans.getTranId(), trans.getAcctNum(), trans.getTranAmount(),
				trans.getTranType(), trans.getTranNarrate(), LocalDate.now(), trans.getTranCrncyCode(),
				trans.getPartTranType(), trans.getTranGL(), trans.getPaymentReference(), trans.getRelatedTransId(),
				userId, email,n);
		walletTransactionRepository.saveAndFlush(tranrev);

		if (trans.getPartTranType().equalsIgnoreCase("D")) {
			double clrbalAmtDr = RevAcct.getClr_bal_amt() - trans.getTranAmount().doubleValue();
			double cumbalDrAmtDr = RevAcct.getCum_dr_amt() + trans.getTranAmount().doubleValue();
			RevAcct.setLast_tran_id_dr(trans.getTranId());
			RevAcct.setClr_bal_amt(clrbalAmtDr);
			RevAcct.setCum_dr_amt(cumbalDrAmtDr);
			RevAcct.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(RevAcct);
		} else {
			double clrbalAmtCr = RevAcct.getClr_bal_amt() + trans.getTranAmount().doubleValue();
			double cumbalCrAmtCr = RevAcct.getCum_cr_amt() + trans.getTranAmount().doubleValue();
			RevAcct.setLast_tran_id_cr(trans.getTranId());
			RevAcct.setClr_bal_amt(clrbalAmtCr);
			RevAcct.setCum_cr_amt(cumbalCrAmtCr);
			RevAcct.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(RevAcct);
		}
	}

	@Override
	public ApiResponse<?> TranRevALLReport(Date fromdate, Date todate) {
		LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<WalletTransaction> transaction = walletTransactionRepository.findByReverse(fromDate, toDate);
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSAL REPORT SUCCESSFULLY", transaction);
	}
	
	@Override
	public ApiResponse<?> PaymentTransAccountReport(Date fromdate, Date todate, String accountNo) {
		LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<WalletTransaction> transaction = walletTransactionRepository.findByAccountReverse(fromDate, toDate, accountNo);
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSAL REPORT SUCCESSFULLY", transaction);
	}
	
	@Override
	public ApiResponse<?> PaymentAccountTrans(Date fromdate, Date todate, String wayaNo) {
		LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<WalletTransaction> transaction = walletTransactionRepository.findByOfficialAccount(fromDate, toDate, wayaNo);
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "OFFICIAL ACCOUNT SUCCESSFULLY", transaction);
	}
	
	@Override
	public ApiResponse<?> PaymentOffTrans() {
		List<WalletTransaction> transaction = walletTransactionRepository.findByAccountOfficial();
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "OFFICIAL ACCOUNT SUCCESSFULLY", transaction);
	}

	@Override
	public ApiResponse<?> TranALLReverseReport() {
		List<WalletTransaction> transaction = walletTransactionRepository.findByAllReverse();
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSAL REPORT SUCCESSFULLY", transaction);
	}
	
	public ApiResponse<?> TranChargeReport() {
		List<AccountTransChargeDTO> transaction = tempwallet.TransChargeReport();
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO CHARGE REPORT", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "CHARGE REPORT SUCCESSFULLY", transaction);
	}

	@Override
	public ApiResponse<?> createBulkTransaction(BulkTransactionCreationDTO bulkList) {
		try {
			if (bulkList == null || bulkList.getUsersList().isEmpty())
				return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "Bulk List cannot be null or Empty",
						null);
			String fromAccountNumber = bulkList.getOfficeAccountNo();
			TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(bulkList.getTranType());
			String tranId = createBulkTransaction(fromAccountNumber, bulkList.getTranCrncy(), bulkList.getUsersList(),
					tranType, bulkList.getTranNarration(), bulkList.getPaymentReference());
			String[] tranKey = tranId.split(Pattern.quote("|"));
			if (tranKey[0].equals("DJGO")) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
			}
			Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
			if (!transaction.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
			}
			return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATED SUCCESSFULLY",
					transaction.get());
		} catch (Exception e) {
			log.error("Error in Creating Bulk Account:: {}", e.getMessage());
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);

		}
	}

	@Override
	public ApiResponse<?> createBulkExcelTrans(MultipartFile file) {
		String message;
		BulkTransactionExcelDTO bulkLimt = null;
		if (ExcelHelper.hasExcelFormat(file)) {
			try {
				bulkLimt = ExcelHelper.excelToBulkTransactionPojo(file.getInputStream(), file.getOriginalFilename());
				String tranId = createExcelTransaction(bulkLimt.getUsersList());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
				return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATED SUCCESSFULLY",
						transaction.get());

			} catch (Exception e) {
				throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
		message = "Please upload an excel file!";
		return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, message, null);
	}

	public String createBulkTransaction(String debitAcctNo, String tranCrncy, Set<UserTransactionDTO> usersList,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef) throws Exception {
		try {
			int n = 1;
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			// Does account exist
			WalletAccount accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
			List<WalletAccount> creditList = new ArrayList<WalletAccount>();
			BigDecimal amount = new BigDecimal("0");
			for (UserTransactionDTO mUser : usersList) {
				WalletAccount accountCredit = walletAccountRepository.findByAccountNo(mUser.getCustomerAccountNo());
				if (accountCredit == null)
					return "DJGO|BENEFICIARY ACCOUNT: " + mUser.getCustomerAccountNo() + " DOES NOT EXIST";
				creditList.add(accountCredit);
				amount = amount.add(mUser.getAmount());

				if (!accountCredit.getAcct_ownership().equals("O")) {
					if (accountCredit.isAcct_cls_flg())
						return "DJGO|CREDIT ACCOUNT IS CLOSED";

					log.info("Credit Account is: {}", accountCredit.getAccountNo());
					log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
					if (accountCredit.getFrez_code() != null) {
						if (accountCredit.getFrez_code().equals("C"))
							return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
					}
				}
			}
			if (accountDebit == null || creditList == null) {
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

			for (WalletAccount acct : creditList) {
				log.info(acct.getHashed_no());
				String compareCredit = tempwallet.GetSecurityTest(acct.getAccountNo());
				log.info(compareCredit);
				String secureCredit = reqIPUtils.WayaDecrypt(acct.getHashed_no());
				log.info(secureCredit);
				String[] keyCredit = secureCredit.split(Pattern.quote("|"));
				if ((!keyCredit[1].equals(acct.getAccountNo())) || (!keyCredit[2].equals(acct.getProduct_code()))
						|| (!keyCredit[3].equals(acct.getAcct_crncy_code()))) {
					return "DJGO|CREDIT ACCOUNT DATA INTEGRITY ISSUE";
				}
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
			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			if (!accountDebit.getAcct_ownership().equals("O")) {
				if (accountDebit.isAcct_cls_flg())
					return "DJGO|DEBIT ACCOUNT IS CLOSED";
				log.info("Debit Account is: {}", accountDebit.getAccountNo());
				log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
				if (accountDebit.getFrez_code() != null) {
					if (accountDebit.getFrez_code().equals("D"))
						return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
				}
				
				if (accountDebit.getLien_amt() != 0) {
					double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
				if (userLim.compareTo(amount) == -1) {
					return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
				}
			}

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
			// MyData tokenData = tokenService.getUserInformation();
			// String email = tokenData != null ? tokenData.getEmail() : "";
			// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,n);

			List<WalletTransaction> tranMultCredit = new ArrayList<WalletTransaction>();
			for (UserTransactionDTO mUser : usersList) {
				n = n + 1;
				WalletAccount acctcdt = walletAccountRepository.findByAccountNo(mUser.getCustomerAccountNo());
				WalletTransaction tranCredit = new WalletTransaction(tranId, acctcdt.getAccountNo(), mUser.getAmount(),
						tranType, tranNarrate, LocalDate.now(), tranCrncy, "C", acctcdt.getGl_code(), paymentRef,
						userId, email,n);
				tranMultCredit.add(tranCredit);
				n++;
			}
			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAll(tranMultCredit);

			double clrbalAmtDr = accountDebit.getClr_bal_amt() - amount.doubleValue();
			double cumbalDrAmtDr = accountDebit.getCum_dr_amt() + amount.doubleValue();
			accountDebit.setLast_tran_id_dr(tranId);
			accountDebit.setClr_bal_amt(clrbalAmtDr);
			accountDebit.setCum_dr_amt(cumbalDrAmtDr);
			accountDebit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountDebit);

			for (UserTransactionDTO mUser : usersList) {
				WalletAccount finalCredit = walletAccountRepository.findByAccountNo(mUser.getCustomerAccountNo());
				double clrbalAmtCr = finalCredit.getClr_bal_amt() + mUser.getAmount().doubleValue();
				double cumbalCrAmtCr = finalCredit.getCum_cr_amt() + mUser.getAmount().doubleValue();
				finalCredit.setLast_tran_id_cr(tranId);
				finalCredit.setClr_bal_amt(clrbalAmtCr);
				finalCredit.setCum_cr_amt(cumbalCrAmtCr);
				finalCredit.setLast_tran_date(LocalDate.now());
				walletAccountRepository.saveAndFlush(finalCredit);
			}
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createExcelTransaction(Set<ExcelTransactionCreationDTO> usersList) throws Exception {
		List<WalletTransaction> tranMultCR = new ArrayList<WalletTransaction>();
		List<WalletTransaction> tranMultDR = new ArrayList<WalletTransaction>();
		List<WalletAccount> acctMultDebit = new ArrayList<WalletAccount>();
		List<WalletAccount> acctMultCredit = new ArrayList<WalletAccount>();

		try {

			String tranId = tempwallet.SystemGenerateTranId();

			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}
			int n = 1;
			for (ExcelTransactionCreationDTO mUser : usersList) {
				log.info("Process Transaction: {}", mUser.toString());
				boolean validate = paramValidation.validateDefaultCode(mUser.getTranCrncy(), "Currency");
				if (!validate) {
					return "DJGO|Currency Code Validation Failed";
				}
				TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(mUser.getTranType());
				// Does account exist
				WalletAccount accountDebit = walletAccountRepository.findByAccountNo(mUser.getOfficeAccountNo());
				log.info("DEBIT: {}", accountDebit.toString());
				BigDecimal amount = mUser.getAmount();
				WalletAccount accountCredit = walletAccountRepository.findByAccountNo(mUser.getCustomerAccountNo());
				log.info("CREDIT: {}", accountCredit.toString());
				if (accountDebit == null || accountCredit == null) {
					return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
				}
				// Check for account security
				log.info(accountDebit.getHashed_no());
				String compareDebit = tempwallet.GetSecurityTest(mUser.getOfficeAccountNo());
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
				String compareCredit = tempwallet.GetSecurityTest(mUser.getCustomerAccountNo());
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
				// Token Fetch
				MyData tokenData = tokenService.getUserInformation();
				String email = tokenData != null ? tokenData.getEmail() : "";
				String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
				// **********************************************

				// AUth Security check
				// **********************************************
				if (!accountDebit.getAcct_ownership().equals("O")) {
					if (accountDebit.isAcct_cls_flg())
						return "DJGO|DEBIT ACCOUNT IS CLOSED";
					log.info("Debit Account is: {}", accountDebit.getAccountNo());
					log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
					if (accountDebit.getFrez_code() != null) {
						if (accountDebit.getFrez_code().equals("D"))
							return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
					}
					
					if (accountDebit.getLien_amt() != 0) {
						double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
						if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
							return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
						}
						if (new BigDecimal(oustbal).compareTo(amount) == -1) {
							return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
						}
					}

					BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
					if (userLim.compareTo(amount) == -1) {
						return "DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED";
					}
				}

				if (!accountCredit.getAcct_ownership().equals("O")) {
					if (accountCredit.isAcct_cls_flg())
						return "DJGO|CREDIT ACCOUNT IS CLOSED";

					log.info("Credit Account is: {}", accountCredit.getAccountNo());
					log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
					if (accountCredit.getFrez_code() != null) {
						if (accountCredit.getFrez_code().equals("C"))
							return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
					}
				}

				// **********************************************

				// MyData tokenData = tokenService.getUserInformation();
				// String email = tokenData != null ? tokenData.getEmail() : "";
				// String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

				String tranNarrate = "WALLET-" + mUser.getTranNarration();
				WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount,
						tranType, tranNarrate, LocalDate.now(), mUser.getTranCrncy(), "D", accountDebit.getGl_code(),
						mUser.getPaymentReference(), userId, email,n);
				
				n = n + 1;

				WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(),
						mUser.getAmount(), tranType, tranNarrate, LocalDate.now(), mUser.getTranCrncy(), "C",
						accountCredit.getGl_code(), mUser.getPaymentReference(), userId, email,n);

				// walletTransactionRepository.saveAndFlush(tranDebit);
				// walletTransactionRepository.saveAll(tranMultCredit);
				log.info("Debit Transaction: {}", tranDebit.toString());
				log.info("Credit Transaction: {}", tranCredit.toString());
				tranMultCR.add(tranCredit);
				tranMultDR.add(tranDebit);

				double clrbalAmtDr = accountDebit.getClr_bal_amt() - amount.doubleValue();
				double cumbalDrAmtDr = accountDebit.getCum_dr_amt() + amount.doubleValue();
				accountDebit.setLast_tran_id_dr(tranId);
				accountDebit.setClr_bal_amt(clrbalAmtDr);
				accountDebit.setCum_dr_amt(cumbalDrAmtDr);
				accountDebit.setLast_tran_date(LocalDate.now());
				// walletAccountRepository.saveAndFlush(accountDebit);
				acctMultDebit.add(accountDebit);

				double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
				double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
				accountCredit.setLast_tran_id_cr(tranId);
				accountCredit.setClr_bal_amt(clrbalAmtCr);
				accountCredit.setCum_cr_amt(cumbalCrAmtCr);
				accountCredit.setLast_tran_date(LocalDate.now());
				// walletAccountRepository.saveAndFlush(accountCredit);
				acctMultCredit.add(accountCredit);
				n++;
			}
			walletTransactionRepository.saveAll(tranMultDR);
			walletTransactionRepository.saveAll(tranMultCR);
			walletAccountRepository.saveAll(acctMultDebit);
			walletAccountRepository.saveAll(acctMultCredit);
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	@Override
	public ApiResponse<?> statementReport(Date fromdate, Date todate, String acctNo) {
		LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<WalletTransaction> transaction = walletTransactionRepository.findByStatement(fromDate, toDate, acctNo);
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION REPORT", transaction);
	}

}
