package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.dto.AccountTransChargeDTO;
import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminWalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.dto.BulkTransactionCreationDTO;
import com.wayapaychat.temporalwallet.dto.BulkTransactionExcelDTO;
import com.wayapaychat.temporalwallet.dto.ClientComTransferDTO;
import com.wayapaychat.temporalwallet.dto.ClientWalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.CommissionHistoryDTO;
import com.wayapaychat.temporalwallet.dto.CommissionTransferDTO;
import com.wayapaychat.temporalwallet.dto.DirectTransactionDTO;
import com.wayapaychat.temporalwallet.dto.EventOfficePaymentDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.ExcelTransactionCreationDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaBenefDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaPayPIN;
import com.wayapaychat.temporalwallet.dto.NonWayaPaymentDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaRedeemDTO;
import com.wayapaychat.temporalwallet.dto.OTPResponse;
import com.wayapaychat.temporalwallet.dto.OfficeTransferDTO;
import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.PaymentRequest;
import com.wayapaychat.temporalwallet.dto.ReversePaymentDTO;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.UserTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAccountStatement;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WayaPaymentRequest;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WayaPaymentQRCode;
import com.wayapaychat.temporalwallet.dto.WayaRedeemQRCode;
import com.wayapaychat.temporalwallet.dto.WayaTradeDTO;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletAcountVirtual;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletNonWayaPayment;
import com.wayapaychat.temporalwallet.entity.WalletPaymentRequest;
import com.wayapaychat.temporalwallet.entity.WalletQRCodePayment;
import com.wayapaychat.temporalwallet.entity.WalletTeller;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.PaymentRequestStatus;
import com.wayapaychat.temporalwallet.enumm.PaymentStatus;
import com.wayapaychat.temporalwallet.enumm.ProviderType;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.notification.CustomNotification;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TransWallet;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletAcountVirtualRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletNonWayaPaymentRepository;
import com.wayapaychat.temporalwallet.repository.WalletPaymentRequestRepository;
import com.wayapaychat.temporalwallet.repository.WalletQRCodePaymentRepository;
import com.wayapaychat.temporalwallet.repository.WalletTellerRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.ExcelHelper;
import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
import com.wayapaychat.temporalwallet.util.ReqIPUtils;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransAccountServiceImpl implements TransAccountService {

	@Autowired
	WalletUserRepository walletUserRepository;

	@Autowired
	WalletAccountRepository walletAccountRepository;

	@Autowired
	WalletAcountVirtualRepository walletAcountVirtualRepository;

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
	private SwitchWalletService switchWalletService;

	@Autowired
	private TokenImpl tokenService;

	@Autowired
	ExternalServiceProxyImpl userDataService;

	@Autowired
	WalletNonWayaPaymentRepository walletNonWayaPaymentRepo;

	@Autowired
	CustomNotification customNotification;

	@Autowired
	WalletQRCodePaymentRepository walletQRCodePaymentRepo;

	@Autowired
	WalletPaymentRequestRepository walletPaymentRequestRepo;

	@Autowired
	ExternalServiceProxyImpl externalServiceProxy;
	
	@Autowired
	AuthProxy authProxy;

	@Override
	public ResponseEntity<?> adminTransferForUser(HttpServletRequest request, String command,
			AdminUserTransferDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return adminTransfer(request, command, transfer);
		case ProviderType.TEMPORAL:
			return adminTransfer(request, command, transfer);
		default:
			return adminTransfer(request, command, transfer);
		}

	}

	public ResponseEntity<?> adminTransfer(HttpServletRequest request, String command, AdminUserTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("ADMINTIL", "", toAccountNumber, transfer.getAmount(), reference);
			if (intRec == 1) {
				String tranId = createAdminTransaction(transfer.getAdminUserId(), toAccountNumber,
						transfer.getTranCrncy(), transfer.getAmount(), tranType, transfer.getTranNarration(),
						transfer.getPaymentReference(), command, request);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	public ApiResponse<?> cashTransferByAdmin(HttpServletRequest request, String command,
			WalletAdminTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID", null);
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
		String toAccountNumber = defaultAcct.get().getAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("ADMINTIL", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createAdminTransaction(transfer.getAdminUserId(), toAccountNumber,
						transfer.getTranCrncy(), transfer.getAmount(), tranType, transfer.getTranNarration(),
						transfer.getPaymentReference(), command, request);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
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
	public ResponseEntity<?> EventTransferPayment(HttpServletRequest request, EventPaymentDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return EventPayment(request, transfer);
		case ProviderType.TEMPORAL:
			return EventPayment(request, transfer);
		default:
			return EventPayment(request, transfer);
		}
	}

	public ResponseEntity<?> EventPayment(HttpServletRequest request, EventPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createEventTransaction(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference, request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}
	
	@Override
	public ResponseEntity<?> EventOfficePayment(HttpServletRequest request, EventOfficePaymentDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return OfficePayment(request, transfer);
		case ProviderType.TEMPORAL:
			return OfficePayment(request, transfer);
		default:
			return OfficePayment(request, transfer);
		}
	}
	
	public ResponseEntity<?> OfficePayment(HttpServletRequest request, EventOfficePaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}
		String toAccountNumber = transfer.getCreditEventId();
		String fromAccountNumber = transfer.getDebitEventId();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>(new ErrorResponse("DEBIT EVENT CAN'T BE THE SAME WITH CREDIT EVENT"), HttpStatus.BAD_REQUEST);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {
				
				String tranId = createEventOfficeTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference, request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
		
	}

	public ApiResponse<?> EventNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		}

		String toAccountNumber = transfer.getCustomerDebitAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("NONWAYAPT", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventTransaction("NONWAYAPT", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, transfer.getFullName(),
						transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
						tranId, tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
						transfer.getEmailOrPhoneNo(), message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
						transfer.getEmailOrPhoneNo(), message, userToken.getId()));
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

	public ApiResponse<?> EventNonRedeem(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED TOKEN", null);
		}
		String toAccountNumber = transfer.getCustomerDebitAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("NONWAYAPT", "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventRedeem("NONWAYAPT", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, transfer.getFullName(),
						transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
						tranId, tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
						transfer.getEmailOrPhoneNo(), message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
						transfer.getEmailOrPhoneNo(), message, userToken.getId()));
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
	public ResponseEntity<?> TransferNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return NonPayment(request, transfer);
		case ProviderType.TEMPORAL:
			return NonPayment(request, transfer);
		default:
			return NonPayment(request, transfer);
		}
	}

	public ResponseEntity<?> NonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {

		log.info("Transaction Request Creation: {}", transfer.toString());
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}

		String transactionToken = tempwallet.generateToken();
		String debitAccountNumber = transfer.getCustomerDebitAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVAILED ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("", debitAccountNumber, "NONWAYAPT", transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createEventTransactionNew(transfer.getCustomerDebitAccountNo(), "NONWAYAPT",
						transfer.getTranCrncy(), transfer.getAmount(), tranType, transfer.getTranNarration(), reference,
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {

					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {

					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				WalletNonWayaPayment nonpay = new WalletNonWayaPayment(transactionToken, transfer.getEmailOrPhoneNo(),
						tranId, transfer.getCustomerDebitAccountNo(), transfer.getAmount(), transfer.getTranNarration(),
						transfer.getTranCrncy(), transfer.getPaymentReference(), userToken.getId().toString(),
						userToken.getEmail(), PaymentStatus.PENDING, transfer.getFullName());
				walletNonWayaPaymentRepo.save(nonpay);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration(), transactionToken);

				if (!StringUtils.isNumeric(transfer.getEmailOrPhoneNo())) {
					log.info("EMAIL: " + transfer.getEmailOrPhoneNo());

					CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
							tranId, tranDate, transfer.getTranNarration()));

					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
							userToken.getPhoneNumber(), message, userToken.getId()));

					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId()));
				} else {
					log.info("PHONE: " + transfer.getEmailOrPhoneNo());
					
					CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
							userToken.getEmail(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
							tranDate, transfer.getTranNarration()));

					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId()));

					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId()));
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATED", transaction),
						HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	public ResponseEntity<?> TransferNonRedeem(HttpServletRequest request, NonWayaBenefDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		WalletUser user = walletUserRepository.findByUserId(transfer.getMerchantId());
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT ID"), HttpStatus.BAD_REQUEST);
		}
		String fullName = user.getCust_name();
		String emailAddress = user.getEmailAddress();
		String phoneNo = user.getMobileNo();

		List<WalletAccount> account = user.getAccount();
		String beneAccount = null;
		for (WalletAccount mAccount : account) {
			if (mAccount.isWalletDefault()) {
				beneAccount = mAccount.getAccountNo();
			}
		}

		String transactionToken = tempwallet.generateToken();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		// ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
		// "INVAILED ACCOUNT NO", null);
		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVAILED ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("NONWAYAPT", "", beneAccount, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventTransactionNew("NONWAYAPT", beneAccount, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1],
					// null);
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION
					// FAILED TO CREATE", null);
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration(), transactionToken);
				CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, fullName, emailAddress,
						message, userToken.getId(), transfer.getAmount().toString(), tranId, tranDate,
						transfer.getTranNarration()));
				CompletableFuture.runAsync(
						() -> customNotification.pushSMS(token, fullName, phoneNo, message, userToken.getId()));
				CompletableFuture.runAsync(
						() -> customNotification.pushInApp(token, fullName, phoneNo, message, userToken.getId()));

				// resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION
				// CREATE", transaction);
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATED", transaction),
						HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

			} else {
				if (intRec == 2) {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unable to
					// process duplicate transaction", null);
					return new ResponseEntity<>(new ErrorResponse("Unable to process duplicate transaction"),
							HttpStatus.BAD_REQUEST);
				} else {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database
					// Error", null);
					return new ResponseEntity<>(new ErrorResponse("Unknown Database Error"), HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ResponseEntity<?> TransferNonReject(HttpServletRequest request, String beneAccount, BigDecimal amount,
			String tranCrncy, String tranNarration, String paymentReference) {

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		WalletAccount account = walletAccountRepository.findByAccountNo(beneAccount);
		if (account == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		String fullName = account.getUser().getCust_name();
		String emailAddress = account.getUser().getEmailAddress();
		String phoneNo = account.getUser().getMobileNo();

		String transactionToken = tempwallet.generateToken();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		// ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
		// "INVAILED ACCOUNT NO", null);
		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVAILED ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("NONWAYAPT", "", beneAccount, amount, paymentReference);
			if (intRec == 1) {
				String tranId = createEventTransactionNew("NONWAYAPT", beneAccount, tranCrncy, amount, tranType,
						tranNarration, paymentReference, request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1],
					// null);
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				if (!transaction.isPresent()) {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION
					// FAILED TO CREATE", null);
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatMessage(amount, tranId, tranDate, tranCrncy, tranNarration, transactionToken);
				CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, fullName, emailAddress,
						message, userToken.getId(), amount.toString(), tranId, tranDate, tranNarration));
				CompletableFuture.runAsync(
						() -> customNotification.pushSMS(token, fullName, phoneNo, message, userToken.getId()));
				CompletableFuture.runAsync(
						() -> customNotification.pushInApp(token, fullName, phoneNo, message, userToken.getId()));

				// resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION
				// CREATE", transaction);
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATED", transaction),
						HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

			} else {
				if (intRec == 2) {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unable to
					// process duplicate transaction", null);
					return new ResponseEntity<>(new ErrorResponse("Unable to process duplicate transaction"),
							HttpStatus.BAD_REQUEST);
				} else {
					// return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database
					// Error", null);
					return new ResponseEntity<>(new ErrorResponse("Unknown Database Error"), HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	@Override
	public ResponseEntity<?> NonWayaPaymentRedeem(HttpServletRequest request, NonWayaRedeemDTO transfer) {
		try {
			// To fetch the token used
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			MyData userToken = tokenService.getTokenUser(token);
			if (userToken == null) {
				return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
			}
			WalletNonWayaPayment redeem = walletNonWayaPaymentRepo
					.findByTransaction(transfer.getToken(), transfer.getTranCrncy()).orElse(null);
			if (redeem == null) {
				return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN.PLEASE CHECK IT"), HttpStatus.BAD_REQUEST);
			}

			String messageStatus = null;
			if (redeem.getStatus().name().equals("PENDING")) {
				if (transfer.getTranStatus().equals("RESERVED")) {
					messageStatus = "TRANSACTION RESERVED: Kindly note that confirm PIN has been sent";
					redeem.setStatus(PaymentStatus.RESERVED);
					String pinToken = tempwallet.generatePIN();
					redeem.setConfirmPIN(pinToken);
					redeem.setUpdatedAt(LocalDateTime.now());
					redeem.setMerchantId(transfer.getMerchantId());
					String message = formatMessagePIN(pinToken);
					CompletableFuture.runAsync(() -> customNotification.pushEMAIL(token, redeem.getFullName(),
							redeem.getEmailOrPhone(), message, userToken.getId()));
					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, redeem.getFullName(),
							redeem.getEmailOrPhone(), message, userToken.getId()));
					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, redeem.getFullName(),
							redeem.getEmailOrPhone(), message, userToken.getId()));
					walletNonWayaPaymentRepo.save(redeem);
				} else {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION MUST BE RESERVED FIRST"),
							HttpStatus.BAD_REQUEST);
				}
			} else if (redeem.getStatus().name().equals("RESERVED")) {
				if (transfer.getTranStatus().equals("PAYOUT")) {
					messageStatus = "TRANSACTION PAYOUT.";
					redeem.setStatus(PaymentStatus.PAYOUT);
					redeem.setUpdatedAt(LocalDateTime.now());
					redeem.setRedeemedEmail(userToken.getEmail());
					redeem.setRedeemedBy(userToken.getId().toString());
					redeem.setRedeemedAt(LocalDateTime.now());
					walletNonWayaPaymentRepo.save(redeem);
					String tranNarrate = "REDEEM " + redeem.getTranNarrate();
					String payRef = "REDEEM" + redeem.getPaymentReference();
					NonWayaBenefDTO merchant = new NonWayaBenefDTO(redeem.getMerchantId(), redeem.getTranAmount(),
							redeem.getCrncyCode(), tranNarrate, payRef);
					TransferNonRedeem(request, merchant);
				} else {
					if (transfer.getTranStatus().equals("REJECT")) {
						messageStatus = "TRANSACTION REJECT.";
						redeem.setStatus(PaymentStatus.REJECT);
						redeem.setUpdatedAt(LocalDateTime.now());
						redeem.setRedeemedEmail(userToken.getEmail());
						redeem.setRedeemedBy(userToken.getId().toString());
						redeem.setRedeemedAt(LocalDateTime.now());
						String tranNarrate = "REJECT " + redeem.getTranNarrate();
						String payRef = "REJECT" + redeem.getPaymentReference();
						walletNonWayaPaymentRepo.save(redeem);
						TransferNonReject(request, redeem.getDebitAccountNo(), redeem.getTranAmount(),
								redeem.getCrncyCode(), tranNarrate, payRef);
					} else {
						return new ResponseEntity<>(new ErrorResponse("UNABLE TO CONFIRMED TRANSACTION WITH PIN SENT"),
								HttpStatus.BAD_REQUEST);
					}
				}
			} else {
				return new ResponseEntity<>(new ErrorResponse("UNABLE TO PAYOUT.PLEASE CHECK YOUR TOKEN"),
						HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(new SuccessResponse(messageStatus, null), HttpStatus.CREATED);

		} catch (Exception ex) {
			log.error(ex.getMessage());
			throw new CustomException(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request, NonWayaPayPIN transfer) {
		WalletNonWayaPayment redeem = walletNonWayaPaymentRepo
				.findByTokenPIN(transfer.getTokenId(), transfer.getTokenPIN()).orElse(null);
		if (redeem == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID PIN.PLEASE CHECK IT"), HttpStatus.BAD_REQUEST);
		}

		if (redeem.getMerchantId().compareTo(transfer.getMerchantId()) != 0) {
			return new ResponseEntity<>(
					new ErrorResponse("TWO MERCHANT CAN'T PROCESS NON-WAYA TRANSACTION. PLEASE CONTACT ADMIN"),
					HttpStatus.BAD_REQUEST);
		}
		NonWayaRedeemDTO waya = new NonWayaRedeemDTO(redeem.getMerchantId(), redeem.getTranAmount(),
				redeem.getCrncyCode(), redeem.getTokenId(), "PAYOUT");
		NonWayaPaymentRedeem(request, waya);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", null), HttpStatus.CREATED);
	}

	public ApiResponse<?> EventBuySellPayment(HttpServletRequest request, WayaTradeDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		String toAccountNumber = transfer.getBenefAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventTransaction(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
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
	public ResponseEntity<?> EventCommissionPayment(HttpServletRequest request, EventPaymentDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return CommissionPayment(request, transfer);
		case ProviderType.TEMPORAL:
			return CommissionPayment(request, transfer);
		default:
			return CommissionPayment(request, transfer);
		}
	}

	public ResponseEntity<?> CommissionPayment(HttpServletRequest request, EventPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}
		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getCustomerAccountNumber());
		if (!acctComm.getProduct_code().equals("SB901")) {
			return new ResponseEntity<>(new ErrorResponse("NOT COMMISSION WALLET"), HttpStatus.BAD_REQUEST);
		}
		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		CategoryType tranCategory = CategoryType.valueOf("COMMISSION");
		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createEventCommission(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference, request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}

				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	public ResponseEntity<?> BankTransferPayment(HttpServletRequest request, BankPaymentDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return BankPayment(request, transfer);
		case ProviderType.TEMPORAL:
			return BankPayment(request, transfer);
		default:
			return BankPayment(request, transfer);
		}
	}

	public ResponseEntity<?> BankPayment(HttpServletRequest request, BankPaymentDTO transfer) {

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}

		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("WITHDRAW");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("BANKPMT", "", toAccountNumber, transfer.getAmount(), reference);
			if (intRec == 1) {
				String tranId = BankTransactionPay("BANKPMT", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference, transfer.getBankName(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	@Override
	public ApiResponse<TransactionRequest> transferUserToUser(HttpServletRequest request, String command,
			TransactionRequest transfer) {
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
		Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
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
	public ResponseEntity<?> makeWalletTransaction(HttpServletRequest request, String command,
			TransferTransactionDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return makeTransfer(request, command, transfer);
		case ProviderType.TEMPORAL:
			return makeTransfer(request, command, transfer);
		default:
			return makeTransfer(request, command, transfer);
		}
	}
	
	public ResponseEntity<?> makeTransfer(HttpServletRequest request, String command,
			TransferTransactionDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>(new ErrorResponse("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), 
							HttpStatus.BAD_REQUEST);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"), 
							HttpStatus.BAD_REQUEST);
				}

				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", 
						transaction), HttpStatus.CREATED);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, xUser.getMobileNo(),
						message2, userToken.getId()));
			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"), HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"), HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	@Override
	public ResponseEntity<?> sendMoney(HttpServletRequest request, TransferTransactionDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return MoneyTransfer(request, transfer);
		case ProviderType.TEMPORAL:
			return MoneyTransfer(request, transfer);
		default:
			return MoneyTransfer(request, transfer);
		}
	}

	public ResponseEntity<?> MoneyTransfer(HttpServletRequest request, TransferTransactionDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);

		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>(new ErrorResponse("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT"), HttpStatus.BAD_REQUEST);
		}

		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}

		if (fromAccountNumber.trim().equals(toAccountNumber.trim())) {
			log.info(toAccountNumber + "|" + fromAccountNumber);
			return new ResponseEntity<>(new ErrorResponse("DEBIT AND CREDIT ON THE SAME ACCOUNT"),
					HttpStatus.BAD_REQUEST);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference, request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}

				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	@Override
	public ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request, DirectTransactionDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return PaymentMoney(request, transfer);
		case ProviderType.TEMPORAL:
			return PaymentMoney(request, transfer);
		default:
			return PaymentMoney(request, transfer);
		}

	}

	public ResponseEntity<?> PaymentMoney(HttpServletRequest request, DirectTransactionDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		if (!transfer.getSecureKey()
				.equals("yYSowX0uQVUZpNnkY28fREx0ayq+WsbEfm2s7ukn4+RHw1yxGODamMcLPH3R7lBD+Tmyw/FvCPG6yLPfuvbJVA==")) {
			return new ResponseEntity<>(new ErrorResponse("INVAILED KEY"), HttpStatus.BAD_REQUEST);
		}

		WalletAcountVirtual mvirt = walletAcountVirtualRepository.findByIdAccount(transfer.getVId(),
				transfer.getVAccountNo());
		if (mvirt == null) {
			return new ResponseEntity<>(new ErrorResponse("INVAILED VIRTUAL ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		Long userId = Long.parseLong(mvirt.getUserId());
		log.info("USER ID: " + userId);
		WalletUser user = walletUserRepository.findByUserId(userId);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("INVAILED VIRTUAL ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		Optional<WalletAccount> account = walletAccountRepository.findByDefaultAccount(user);
		if (!account.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("NO DEFAULT WALLET FOR VIRTUAL ACCOUNT"),
					HttpStatus.BAD_REQUEST);
		}
		WalletAccount mAccount = account.get();
		String toAccountNumber = mAccount.getAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("BANK");
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createEventTransaction(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
						message, userToken.getId()));

				BigDecimal newAmount = mvirt.getActualBalance().add(transfer.getAmount());
				mvirt.setActualBalance(newAmount);
				walletAcountVirtualRepository.save(mvirt);

			} else {
				if (intRec == 2) {
					return new ResponseEntity<>(new ErrorResponse("UNABLE TO PROCESS DUPLICATE TRANSACTION REFERENCE"),
							HttpStatus.BAD_REQUEST);
				} else {
					return new ResponseEntity<>(new ErrorResponse("UNKNOWN DATABASE ERROR. PLEASE CONTACT ADMIN"),
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	@Override
	public ResponseEntity<?> PostExternalMoney(HttpServletRequest request, CardRequestPojo transfer, Long userId) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return userDataService.getCardPayment(request, transfer, userId);
		case ProviderType.TEMPORAL:
			return userDataService.getCardPayment(request, transfer, userId);
		default:
			return userDataService.getCardPayment(request, transfer, userId);
		}
	}

	public ApiResponse<?> OfficialMoneyTransfer(HttpServletRequest request, OfficeTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		String fromAccountNumber = transfer.getOfficeDebitAccount();
		String toAccountNumber = transfer.getOfficeCreditAccount();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

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
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
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

	public ApiResponse<?> OfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		String fromAccountNumber = transfer.getOfficeDebitAccount();
		String toAccountNumber = transfer.getCustomerCreditAccount();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

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
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
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

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
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

	public ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

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
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
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

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));

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

	public ApiResponse<?> AdminCommissionMoney(HttpServletRequest request, CommissionTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		UserDetailPojo user = authService.AuthUser(transfer.getUserId().intValue());
		if (user == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID USER ID", null);
		}
		if (!user.is_admin()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "USER ID PERFORMING OPERATION IS NOT AN ADMIN",
					null);
		}
		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getDebitAccountNumber());
		if (!acctComm.getProduct_code().equals("SB901")) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT COMMISSION WALLET", null);
		}
		WalletAccount acctDef = walletAccountRepository.findByAccountNo(transfer.getBenefAccountNumber());
		if (!acctDef.isWalletDefault()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT DEFAULT WALLET", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
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

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));

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

	public ApiResponse<?> ClientCommissionMoney(HttpServletRequest request, ClientComTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getDebitAccountNumber());
		if (!acctComm.getProduct_code().equals("SB901")) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT COMMISSION WALLET", null);
		}
		WalletAccount acctDef = walletAccountRepository.findByAccountNo(transfer.getBenefAccountNumber());
		if (!acctDef.isWalletDefault()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NOT DEFAULT WALLET", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
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

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));

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
	public ApiResponse<?> sendMoneyCharge(HttpServletRequest request, WalletTransactionChargeDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createChargeTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						transfer.getEventChargeId(), request);
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

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));

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
	public ApiResponse<?> sendMoneyCustomer(HttpServletRequest request, WalletTransactionDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

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
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));

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
	public ApiResponse<?> AdminSendMoneyCustomer(HttpServletRequest request, AdminWalletTransactionDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		// check for admin
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

		Optional<WalletUser> wallet = walletUserRepository
				.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumberOrUserId());
		if (!wallet.isPresent()) {
			Long userId = Long.valueOf(transfer.getEmailOrPhoneNumberOrUserId());
			wallet = walletUserRepository.findUserId(userId);
			if (!wallet.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE OR ID DOES NOT EXIST",
						null);
			}
		}

		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if (!defaultAcct.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = defaultAcct.get().getAccountNo();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);

				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));

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
	public ApiResponse<?> ClientSendMoneyCustomer(HttpServletRequest request, ClientWalletTransactionDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}

		// check for admin
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

		Optional<WalletUser> wallet = walletUserRepository
				.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumberOrUserId());
		if (!wallet.isPresent()) {
			Long userId = Long.valueOf(transfer.getEmailOrPhoneNumberOrUserId());
			wallet = walletUserRepository.findUserId(userId);
			if (!wallet.isPresent()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE OR ID DOES NOT EXIST",
						null);
			}
		}

		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if (!defaultAcct.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = defaultAcct.get().getAccountNo();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

				String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
						xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getMobileNo(),
						message1, userToken.getId()));

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				WalletUser yUser = walletUserRepository.findByAccount(yAccount);
				String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getMobileNo(),
						message2, userToken.getId()));

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
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) throws Exception {
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
			// To fetch BankAcccount and Does it exist
			WalletAccount accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
			WalletAccount accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security
			log.info(accountDebit.getHashed_no());
			// String compareDebit = tempwallet.GetSecurityTest(debitAcctNo);
			// log.info(compareDebit);
			String secureDebit = reqIPUtils.WayaDecrypt(accountDebit.getHashed_no());
			log.info(secureDebit);
			String[] keyDebit = secureDebit.split(Pattern.quote("|"));
			if ((!keyDebit[1].equals(accountDebit.getAccountNo()))
					|| (!keyDebit[2].equals(accountDebit.getProduct_code()))
					|| (!keyDebit[3].equals(accountDebit.getAcct_crncy_code()))) {
				return "DJGO|DEBIT ACCOUNT DATA INTEGRITY ISSUE";
			}

			log.info(accountCredit.getHashed_no());
			// String compareCredit = tempwallet.GetSecurityTest(creditAcctNo);
			// log.info(compareCredit);
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
				log.info("" + userId);
				WalletUser user = walletUserRepository.findByUserId(userId);
				if (user == null) {
					return "DJGO|USER ID " + userId + " DOES NOT EXIST IN WALLET DATABASE";
				}
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);

			n = n + 1;

			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);
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

			// To send sms and email notification

			// To generate transaction receipt

			log.info("END TRANSACTION");
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, tranCategory.getValue(), token));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createChargeTransaction(String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String eventCharge,
			HttpServletRequest request) throws Exception {
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
					userId, email, n);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n);

			n = n + 1;
			WalletTransaction tranCharge = new WalletTransaction(tranId, chargeTill.getAccountNo(), event.getTranAmt(),
					tranType, tranNarrate1, LocalDate.now(), tranCrncy, "C", chargeTill.getGl_code(), paymentRef,
					userId, email, n);
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
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, "TRANSFER", token));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createAdminTransaction(String adminUserId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String command,
			HttpServletRequest request) throws Exception {
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n);

			n = n + 1;

			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n);
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
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, "TRANSFER", token));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventTransactionNew(String debitEvent, String creditEvent, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) {
		try {
			int mPartran = 1;
			log.info("START DEBIT-CREDIT TRANSACTION");
			// Check for entry to avoid duplicate transaction
			String tranCount = tempwallet.transactionCount(paymentRef, creditEvent);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}

			// validate transaction currency
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}

			final String debitAcctNo, creditAcctNo;

			if (StringUtils.isNumeric(debitEvent)) {
				debitAcctNo = debitEvent;
			} else {
				debitAcctNo = fromEventIdBankAccount(debitEvent).getAccountNo();
			}

			if (StringUtils.isNumeric(creditEvent)) {
				creditAcctNo = creditEvent;
			} else {
				creditAcctNo = fromEventIdBankAccount(creditEvent).getAccountNo();
			}
			// To fetch BankAcccount and Does it exist
			WalletAccount accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
			WalletAccount accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);

			// Validate BankAccount, Amount, security and token
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

			// To generate transaction id
			String tranId = "";
			if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("BANK")
					|| tranType.getValue().equalsIgnoreCase("REVERSAL")
					|| tranType.getValue().equalsIgnoreCase("UTILITY_PAYMENT")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}

			// Update transaction table
			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					mPartran, tranCategory);

			mPartran = mPartran + 1;

			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					mPartran, tranCategory);

			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCredit);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

			// Update Account table
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
			log.info("END DEBIT-CREDIT TRANSACTION");
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, tranCategory.getValue(), token));
			return tranId;
		} catch (Exception ex) {
			log.error(ex.getMessage());
			throw new CustomException(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	public String createEventTransaction(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) throws Exception {
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);
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
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, tranCategory.getValue(), token));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}
	
	public String createEventOfficeTransaction(String debitEventId, String creditEventId, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, creditEventId);
			if (!tranCount.isBlank()) {
				return "tranCount";
			}
			boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
			if (!validate) {
				return "DJGO|Currency Code Validation Failed";
			}
			Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(debitEventId);
			if (!eventInfo.isPresent()) {
				return "DJGO|Event Code Does Not Exist";
			}
			WalletEventCharges charge = eventInfo.get();
			boolean validate2 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
			if (!validate2) {
				return "DJGO|Event Validation Failed";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), "0000");
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			
			//For credit Event only
			Optional<WalletEventCharges> eventCredit = walletEventRepository.findByEventId(creditEventId);
			if (!eventInfo.isPresent()) {
				return "DJGO|Event Code Does Not Exist";
			}
			WalletEventCharges chargeCredit = eventCredit.get();
			boolean validate3 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
			if (!validate3) {
				return "DJGO|Event Validation Failed";
			}
			
			// Does account exist
			Optional<WalletAccount> accountCreditTeller = walletAccountRepository
					.findByUserPlaceholder(chargeCredit.getPlaceholder(), chargeCredit.getCrncyCode(), "0000");
			if (!accountCreditTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			
			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
			} else {
				accountCredit = accountDebitTeller.get();
			}
			
			if (!chargeCredit.isChargeCustomer() && chargeCredit.isChargeWaya()) {
				accountCredit = accountCreditTeller.get();
			} else {
				accountDebit = accountCreditTeller.get();
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
				String compareCredit = tempwallet.GetSecurityTest(accountCredit.getAccountNo());
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);
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
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, tranCategory.getValue(), token));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventRedeem(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request)
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n);
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
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, "TRANSFER", token));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventCommission(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) throws Exception {
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);
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
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, tranCategory.getValue(), token));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String BankTransactionPay(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String bk,
			HttpServletRequest request, CategoryType tranCategory) throws Exception {
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory);
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
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, tranCategory.getValue(), token));
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
	public ResponseEntity<?> TranReversePayment(HttpServletRequest request, ReverseTransactionDTO reverseDto)
			throws ParseException {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			return ReversePayment(request, reverseDto);
		case ProviderType.TEMPORAL:
			return ReversePayment(request, reverseDto);
		default:
			return ReversePayment(request, reverseDto);
		}
	}

	public ResponseEntity<?> ReversePayment(HttpServletRequest request, ReverseTransactionDTO reverseDto)
			throws ParseException {
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
				return new ResponseEntity<>(new ErrorResponse("TRANSACTION ALREADY REVERSED"), HttpStatus.BAD_REQUEST);
			}
			List<WalletTransaction> transpo = walletTransactionRepository.findByTransaction(reverseDto.getTranId(),
					reverseDate, reverseDto.getTranCrncy());
			if (transpo.isEmpty()) {
				return new ResponseEntity<>(new ErrorResponse("TRANSACTION DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
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
				return new ResponseEntity<>(new ErrorResponse("IMBALANCE TRANSACTION"), HttpStatus.BAD_REQUEST);
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
				CategoryType tranCategory = CategoryType.valueOf("REVERSAL");
				trans1.setRelatedTransId(trans1.getTranId());
				trans1.setTranType(tranType);
				trans1.setTranId(tranId);
				trans1.setTranDate(LocalDate.now());
				trans1.setTranCategory(tranCategory);
				PostReverse(trans1, n);
				n++;
			}
			List<WalletTransaction> statement = walletTransactionRepository.findByTransaction(tranId, LocalDate.now(),
					reverseDto.getTranCrncy());
			return new ResponseEntity<>(new SuccessResponse("REVERSE SUCCESSFULLY", statement), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(new SuccessResponse("TRANSACTION DATE WAS IN PAST", null), HttpStatus.CREATED);
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
				userId, email, n, trans.getTranCategory());
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
		List<WalletTransaction> transaction = walletTransactionRepository.findByAccountReverse(fromDate, toDate,
				accountNo);
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSAL REPORT SUCCESSFULLY", transaction);
	}

	@Override
	public ApiResponse<?> PaymentAccountTrans(Date fromdate, Date todate, String wayaNo) {
		LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<WalletTransaction> transaction = walletTransactionRepository.findByOfficialAccount(fromDate, toDate,
				wayaNo);
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
	public ApiResponse<?> PaymentTransFilter(String account) {
		List<TransWallet> transaction = tempwallet.GetTransactionType(account);
		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO TRANSACTION TYPE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "LIST TRANSACTION TYPE", transaction);
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
	public ApiResponse<?> createBulkTransaction(HttpServletRequest request, BulkTransactionCreationDTO bulkList) {
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
	public ApiResponse<?> createBulkExcelTrans(HttpServletRequest request, MultipartFile file) {
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
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n);

			List<WalletTransaction> tranMultCredit = new ArrayList<WalletTransaction>();
			for (UserTransactionDTO mUser : usersList) {
				n = n + 1;
				WalletAccount acctcdt = walletAccountRepository.findByAccountNo(mUser.getCustomerAccountNo());
				WalletTransaction tranCredit = new WalletTransaction(tranId, acctcdt.getAccountNo(), mUser.getAmount(),
						tranType, tranNarrate, LocalDate.now(), tranCrncy, "C", acctcdt.getGl_code(), paymentRef,
						userId, email, n);
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

	public String createExcelTransaction(Set<ExcelTransactionCreationDTO> transList) throws Exception {
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
			for (ExcelTransactionCreationDTO mUser : transList) {
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
						mUser.getPaymentReference(), userId, email, n);

				n = n + 1;

				WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(),
						mUser.getAmount(), tranType, tranNarrate, LocalDate.now(), mUser.getTranCrncy(), "C",
						accountCredit.getGl_code(), mUser.getPaymentReference(), userId, email, n);

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

	@Override
	public ApiResponse<?> VirtuPaymentReverse(HttpServletRequest request, ReversePaymentDTO reverseDto)
			throws ParseException {

		if (!reverseDto.getSecureKey()
				.equals("yYSowX0uQVUZpNnkY28fREx0ayq+WsbEfm2s7ukn4+RHw1yxGODamMcLPH3R7lBD+Tmyw/FvCPG6yLPfuvbJVA==")) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED KEY", null);
		}
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
				PostReverse(trans1, n);
				n++;
			}
			List<WalletTransaction> statement = walletTransactionRepository.findByTransaction(tranId, LocalDate.now(),
					reverseDto.getTranCrncy());
			return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSE SUCCESSFULLY", statement);
		} else {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "TRANSACTION DATE WAS IN PAST", null);
		}
	}

	@Override
	public ApiResponse<?> CommissionPaymentHistory() {
		List<CommissionHistoryDTO> listCom = tempwallet.GetCommissionHistory();
		if (listCom.isEmpty() || listCom == null) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO COMMISSION", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "COMMISSION LIST", listCom);
	}

	public WalletAccount fromEventIdBankAccount(String eventId) {
		WalletEventCharges event = walletEventRepository.findByEventId(eventId)
				.orElseThrow(() -> new NoSuchElementException("EVENT ID NOT AVAILABLE FOR EventId :" + eventId));

		boolean validate2 = paramValidation.validateDefaultCode(event.getPlaceholder(), "Batch Account");
		if (!validate2) {
			throw new CustomException("Event Placeholder Validation Failed", HttpStatus.BAD_REQUEST);
		}

		WalletAccount account = walletAccountRepository
				.findByUserPlaceholder(event.getPlaceholder(), event.getCrncyCode(), "0000")
				.orElseThrow(() -> new NoSuchElementException("EVENT ID NOT AVAILABLE FOR EventId :" + eventId));
		return account;
	}

	public String formatMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy, String narration,
			String tokenId) {

		String message = "" + "\n";
		message = message + "" + "A transaction has occurred with token id: " + tokenId
				+ "  on your account see details below." + "\n";
		return message;
	}

	public String formatNewMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
			String narration) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "A credit transaction has occurred"
				+ "  on your account see details below" + "\n";
		message = message + "" + "Amount :" + amount + "\n";
		message = message + "" + "tranId :" + tranId + "\n";
		message = message + "" + "tranDate :" + tranDate + "\n";
		message = message + "" + "Currency :" + tranCrncy + "\n";
		message = message + "" + "Narration :" + narration + "\n";
		return message;
	}

	public String formatDebitMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
			String narration) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "A debit transaction has occurred"
				+ "  on your account see details below" + "\n";
		message = message + "" + "Amount :" + amount + "\n";
		message = message + "" + "tranId :" + tranId + "\n";
		message = message + "" + "tranDate :" + tranDate + "\n";
		message = message + "" + "Currency :" + tranCrncy + "\n";
		message = message + "" + "Narration :" + narration + "\n";
		return message;
	}

	public String formatMessagePIN(String pin) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "Kindly confirm the reserved transaction with received pin: " + pin;
		return message;
	}

	@Override
	public ResponseEntity<?> WayaQRCodePayment(HttpServletRequest request, WayaPaymentQRCode transfer) {
		String refNo = tempwallet.generateRefNo();
		if (refNo.length() < 12) {
			refNo = StringUtils.leftPad(refNo, 12, "0");
		}
		refNo = "QR-" + transfer.getPayeeId() + "-" + refNo;
		WalletQRCodePayment qrcode = new WalletQRCodePayment(transfer.getName(), transfer.getAmount(),
				transfer.getReason(), refNo, LocalDate.now(), PaymentStatus.PENDING, transfer.getPayeeId(),
				transfer.getCrncyCode());
		WalletQRCodePayment mPay = walletQRCodePaymentRepo.save(qrcode);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", mPay), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> WayaQRCodePaymentRedeem(HttpServletRequest request, WayaRedeemQRCode transfer) {
		WalletQRCodePayment mPay = walletQRCodePaymentRepo.findByReferenceNo(transfer.getReferenceNo(), LocalDate.now())
				.orElse(null);
		if (mPay.getAmount().compareTo(transfer.getAmount()) == 0) {
			if (mPay.getStatus().name().equals("PENDING")) {
				String debitAcct = getAcount(transfer.getPayerId()).getAccountNo();
				String creditAcct = getAcount(mPay.getPayeeId()).getAccountNo();
				TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct, creditAcct, transfer.getAmount(),
						"TRANSFER", mPay.getCrncyCode(), "QR-CODE PAYMENT", mPay.getReferenceNo(),
						transfer.getTransactionCategory());
				return sendMoney(request, txt);

			}
		} else {
			return new ResponseEntity<>(new ErrorResponse("MISMATCH AMOUNT"), HttpStatus.BAD_REQUEST);
		}
		return null;
	}

	public WalletAccount getAcount(Long userId) {
		WalletUser user = walletUserRepository.findByUserId(userId);
		if (user == null) {
			throw new CustomException("INVALID USER ID", HttpStatus.BAD_REQUEST);
		}
		WalletAccount account = walletAccountRepository.findByDefaultAccount(user).orElse(null);
		if (account == null) {
			throw new CustomException("INVALID USER ID", HttpStatus.BAD_REQUEST);
		}
		return account;
	}

	@Override
	public ResponseEntity<?> WayaPaymentRequestUsertoUser(HttpServletRequest request, WayaPaymentRequest transfer) {
		if (transfer.getPaymentRequest().getStatus().name().equals("PENDING")) {
			WalletPaymentRequest mPayRequest = walletPaymentRequestRepo
					.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
			if (mPayRequest != null) {
				throw new CustomException("Reference ID already exist", HttpStatus.BAD_REQUEST);
			}
			WalletPaymentRequest spay = new WalletPaymentRequest(transfer.getPaymentRequest());
			WalletPaymentRequest mPay = walletPaymentRequestRepo.save(spay);
			return new ResponseEntity<>(new SuccessResponse("SUCCESS", mPay), HttpStatus.CREATED);
		} else if (transfer.getPaymentRequest().getStatus().name().equals("PAID")) {
			WalletPaymentRequest mPayRequest = walletPaymentRequestRepo
					.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
			if (mPayRequest == null) {
				throw new CustomException("Reference ID does not exist", HttpStatus.BAD_REQUEST);
			}
			if (mPayRequest.getStatus().name().equals("PENDING") && (mPayRequest.isWayauser())) {
				WalletAccount creditAcct = getAcount(Long.valueOf(mPayRequest.getSenderId()));
				WalletAccount debitAcct = getAcount(Long.valueOf(mPayRequest.getReceiverId()));
				TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct.getAccountNo(),
						creditAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN", mPayRequest.getReason(),
						mPayRequest.getReference(), mPayRequest.getCategory().getValue());
				ResponseEntity<?> res = sendMoney(request, txt);
				if (res.getStatusCodeValue() != 200 && res.getStatusCodeValue() != 201) {
					return res;
				}
				log.info("Send Money: {}", transfer);
				mPayRequest.setStatus(PaymentRequestStatus.PAID);
				walletPaymentRequestRepo.save(mPayRequest);
				return res;
			} else if (mPayRequest.getStatus().name().equals("PENDING") && (!mPayRequest.isWayauser())) {
				PaymentRequest mPay = transfer.getPaymentRequest();
				WalletAccount creditAcct = getAcount(Long.valueOf(mPayRequest.getSenderId()));
				WalletAccount debitAcct = getAcount(Long.valueOf(mPay.getReceiverId()));
				TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct.getAccountNo(),
						creditAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN", mPayRequest.getReason(),
						mPayRequest.getReference(), mPay.getTransactionCategory().getValue());
				ResponseEntity<?> res = sendMoney(request, txt);
				if (res.getStatusCodeValue() != 200 && res.getStatusCodeValue() != 201) {
					return res;
				}
				log.info("Send Money: {}", transfer);
				mPayRequest.setReceiverId(mPay.getReceiverId());
				mPayRequest.setStatus(PaymentRequestStatus.PAID);
				walletPaymentRequestRepo.save(mPayRequest);
				return res;
			} else {
				throw new CustomException("Reference ID already paid", HttpStatus.BAD_REQUEST);
			}
		} else if (transfer.getPaymentRequest().getStatus().name().equals("REJECT")) {
			WalletPaymentRequest mPayRequest = walletPaymentRequestRepo
					.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
			if (mPayRequest == null) {
				throw new CustomException("Reference ID does not exist", HttpStatus.BAD_REQUEST);
			}
			mPayRequest.setStatus(PaymentRequestStatus.REJECTED);
			mPayRequest.setRejected(true);
			WalletPaymentRequest mPay = walletPaymentRequestRepo.save(mPayRequest);
			return new ResponseEntity<>(new SuccessResponse("SUCCESS", mPay), HttpStatus.CREATED);
		} else {
			throw new CustomException("UnKnown Payment Status", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> PostOTPGenerate(HttpServletRequest request, String emailPhone) {
		try {
			OTPResponse tokenResponse = authProxy.postOTPGenerate(emailPhone);
			if(tokenResponse == null)
				throw new CustomException("Unable to delivered OTP", HttpStatus.BAD_REQUEST);
			
			if (!tokenResponse.isStatus())
				  return new ResponseEntity<>(new ErrorResponse(tokenResponse.getMessage()), HttpStatus.BAD_REQUEST);
			
			if (tokenResponse.isStatus())
				log.info("Authorizated Transaction Token: {}", tokenResponse.toString());
			
			return new ResponseEntity<>(new SuccessResponse("SUCCESS", tokenResponse), HttpStatus.CREATED);
			
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> PostOTPVerify(HttpServletRequest request, WalletRequestOTP transfer) {
		try {
			OTPResponse tokenResponse = authProxy.postOTPVerify(transfer);
			if(tokenResponse == null)
				throw new CustomException("Unable to delivered OTP", HttpStatus.BAD_REQUEST);
			
			if (!tokenResponse.isStatus())
				  return new ResponseEntity<>(new ErrorResponse(tokenResponse.getMessage()), HttpStatus.BAD_REQUEST);
			
			if (tokenResponse.isStatus())
				log.info("Verify Transaction Token: {}", tokenResponse.toString());
			
			return new ResponseEntity<>(new SuccessResponse("SUCCESS", tokenResponse), HttpStatus.CREATED);
			
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

}
