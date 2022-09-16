package com.wayapaychat.temporalwallet.service.impl;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.wayapaychat.temporalwallet.util.Constant.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Autowired
	TransactionCountService transactionCountService;

	@Value("${waya.charges.account}")
	private String chargesAccount;


	private BigDecimal chargesAmount = BigDecimal.valueOf(10.00);

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
		String reference;
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

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message, userToken.getId(),TRANSACTION_HAS_OCCURRED));
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

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message, userToken.getId(),TRANSACTION_HAS_OCCURRED));
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
			String tranId = "";
			if (intRec == 1) {
				if(transfer.getEventId().equals("SMSCHG")){
					tranId = createEventTransactionDebitUserCreditWayaAccount(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
							transfer.getAmount(), tranType, transfer.getTranNarration(), reference, request, tranCategory, userToken);
				}else if (transfer.getEventId().equals("AITCOL")){
					tranId = createEventTransactionForBillsPayment(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
							transfer.getAmount(), tranType, transfer.getTranNarration(), reference, request, tranCategory);
				}else{
					tranId = createEventTransaction(transfer.getEventId(), toAccountNumber, transfer.getTranCrncy(),
							transfer.getAmount(), tranType, transfer.getTranNarration(), reference, request, tranCategory);

				}

				//createEventTransactionDebitUserCreditWayaAccount
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (!transaction.isEmpty()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);
				log.info("Transaction Response::", resp.toString());

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);
				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				String finalTranId = tranId;
				if(StringUtils.isNumeric(toAccountNumber)){
					WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
					WalletUser xUser = walletUserRepository.findByAccount(xAccount);
					String fullName = xUser.getFirstName() + " " + xUser.getLastName();
					CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
							xUser.getEmailAddress(), message, userToken.getId(), transfer.getAmount().toString(), finalTranId,
							tranDate, transfer.getTranNarration()));
					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
							message, userToken.getId()));
				}


//				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
//						transfer.getTranNarration());
//

//				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getMobileNo(),
//						message, userToken.getId(), TRANSACTION_HAS_OCCURRED));
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
	public ResponseEntity<?> EventPaymentSettlement(HttpServletRequest request, EventPaymentSettlementDTO eventPay) {

		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				return EventPaymentSettle(request, eventPay);
			case ProviderType.TEMPORAL:
				return EventPaymentSettle(request, eventPay);
			default:
				return EventPaymentSettle(request, eventPay);
		}
	}

	public ResponseEntity<?> EventPaymentSettle(HttpServletRequest request, EventPaymentSettlementDTO transfer) {
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
		//String toAccountNumber = transfer.getCustomerAccountNumber();

		String merchantDefultAccount = transfer.getMerchantAccountNumber();
		String merchantCommissionAccount = transfer.getWayaCommAccountNumber();

		BigDecimal merchantFee = transfer.getMerchantFee();
		BigDecimal wayaCommissionFee = transfer.getWayaCommissionFee();

		BigDecimal totlaAmount = BigDecimal.valueOf(merchantFee.doubleValue() + wayaCommissionFee.doubleValue());

		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", merchantDefultAccount, merchantFee,
					reference);
//			int intRec2 = tempwallet.PaymenttranInsert(transfer.getEventId(), "", merchantCommissionAccount, wayaCommissionFee,
//					reference);
			String tranId = "";
			if (intRec == 1) {

				tranId = createEventTransactionSettlement(transfer.getEventId(), merchantDefultAccount, merchantCommissionAccount, transfer.getTranCrncy(),
							totlaAmount, merchantFee, wayaCommissionFee, tranType, transfer.getTranNarration(), reference, request, tranCategory);

				//createEventTransactionDebitUserCreditWayaAccount
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

	public ResponseEntity<?> TemporalWalletToOfficialWallet(HttpServletRequest request, TemporalToOfficialWalletDTO transfer) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				return OfficePaymentTemporalWalletToOfficialWallet(request, transfer);
			case ProviderType.TEMPORAL:
				return OfficePaymentTemporalWalletToOfficialWallet(request, transfer);
			default:
				return OfficePaymentTemporalWalletToOfficialWallet(request, transfer);
		}
	}

	@Override
	public ResponseEntity<?> TemporalWalletToOfficialWalletMutiple(HttpServletRequest request, List<TemporalToOfficialWalletDTO> transfer) {
		ArrayList<Object> list = new ArrayList<>();
		ResponseEntity<?> resp = null;
		for(TemporalToOfficialWalletDTO data: transfer){
			resp = TemporalWalletToOfficialWallet(request, data);
			list.add(resp);
		}
		return new ResponseEntity<>(list , HttpStatus.BAD_REQUEST);
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message, userToken.getId(),TRANSACTION_HAS_OCCURRED));
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

	private String getTransactionDate(){
		Date tDate = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String tranDate = dateFormat.format(tDate);
		return tranDate;
	}
//ability to transfer money from the temporal wallet back to waya official account in single or in mass with excel upload
	public ResponseEntity<?> OfficePaymentTemporalWalletToOfficialWallet(HttpServletRequest request, TemporalToOfficialWalletDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());


		String tranDate = getTransactionDate();


		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		System.out.println("MYDATE :::  " + userToken);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}
		String toAccountNumber = transfer.getOfficialAccountNumber();
		String fromAccountNumber = transfer.getCustomerAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>(new ErrorResponse("DEBIT EVENT CAN'T BE THE SAME WITH CREDIT EVENT"), HttpStatus.BAD_REQUEST);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("WAYAPAY", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {

				String tranId = createEventOfficeTransactionModified("WAYAPAY", fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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

//				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
//				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				String fullName = userToken.getFirstName() + " " + userToken.getSurname();

				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());

				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						userToken.getEmail(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, userToken.getPhoneNumber(),
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

	public ApiResponse<?> EventNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer){
		ArrayList<Object> list = new ArrayList<>();
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		for(NonWayaPaymentDTO data: transfer){
			resp = EventNonPayment(request, data);
			list.add(resp.getData());
		}
		resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", list);
		return resp;
	}

	public ApiResponse<?> EventNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		System.out.println("HERE  IS THE ENTRY POINT");

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

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatMoneWayaMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration(), token);

				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, transfer.getFullName(),
						transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
						tranId, tranDate, transfer.getTranNarration()));

				if(transfer.getEmailOrPhoneNo().startsWith("234")){
					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId()));
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
						request,transfer.getFullName());
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
	public ApiResponse<?> EventNonRedeemMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer) {
		ArrayList<Object> list = new ArrayList<>();
		ApiResponse<?> res = null;
		for (NonWayaPaymentDTO data: transfer){
			res = EventNonRedeem(request,data);
		  		list.add(res.getData());
			res = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", list);
			log.info("Transaction Response: {}", list.toString());
		}
		return res;
	}

	@Override
	public ResponseEntity<?> TransferNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer){
		ResponseEntity<?> resp = null;
		ArrayList<Object> rpp = new ArrayList<>();
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());

		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				for(NonWayaPaymentDTO data: transfer){
					resp = NonPayment(request, data);
					rpp.add(resp.getBody());
				}
				return resp;
			case ProviderType.TEMPORAL:
				for(NonWayaPaymentDTO data: transfer){
					resp = NonPayment(request, data);
					rpp.add(resp.getBody());
				}
				return new ResponseEntity<>(rpp, HttpStatus.OK);
			default:
				for(NonWayaPaymentDTO data: transfer){
					resp = NonPayment(request, data);
					rpp.add(resp.getBody());
				}
				return resp;
		}

	}

	@Override
	public ResponseEntity<?> TransferNonPaymentMultipleUpload(HttpServletRequest request, MultipartFile file) {
		NonWayaTransferExcelDTO bulkLimt = null;
		Map<String, ArrayList<ResponseHelper>> responseEntity = null;
		if (ExcelHelper.hasExcelFormat(file)) {
			try {
				responseEntity = MultipleUpload2(request,ExcelHelper.excelToNoneWayaTransferAdmin(file.getInputStream(), file.getOriginalFilename()));

			} catch (Exception e) {
				throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}

		return new ResponseEntity<>(responseEntity, HttpStatus.OK);
	}
	private Map<String, ArrayList<ResponseHelper>> MultipleUpload2(HttpServletRequest request, @Valid NonWayaTransferExcelDTO transferExcelDTO){
		ResponseEntity<?> resp = null;
		ArrayList<ResponseHelper> respList = new ArrayList<>();
		Map<String, ArrayList<ResponseHelper>> map = new HashMap<>();

		if (transferExcelDTO == null || transferExcelDTO.getTransfer().isEmpty())
			throw new CustomException("Transfer List cannot be null or Empty", BAD_REQUEST);

		for (NoneWayaPaymentRequest mTransfer : transferExcelDTO.getTransfer()) {

			NonWayaPaymentDTO data = new NonWayaPaymentDTO();
			data.setAmount(mTransfer.getAmount());
			data.setCustomerDebitAccountNo(mTransfer.getCustomerAccountNumber());
			data.setEmailOrPhoneNo(mTransfer.getEmailOrPhoneNo());
			data.setFullName(mTransfer.getFullName());
			data.setPaymentReference(mTransfer.getPaymentReference());
			data.setTranCrncy(mTransfer.getTranCrncy());
			data.setTranNarration(mTransfer.getTranNarration());
			ResponseEntity<?> responseEntity =  NonPayment(request, data);
			// send using

			respList.add((ResponseHelper) responseEntity.getBody());
		}
		map.put("Response", respList);
		return map;
	}



	@Override
	public ResponseEntity<?> TransferNonPaymentSingleWayaOfficial(HttpServletRequest request, NonWayaPaymentMultipleOfficialDTO transfer) {

		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());

		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				return NonPaymentFromOfficialAccount(request, transfer);
			case ProviderType.TEMPORAL:
				return NonPaymentFromOfficialAccount(request, transfer);
			default:
				return NonPaymentFromOfficialAccount(request, transfer);
		}
	}

	@Override
	public ResponseEntity<?> TransferNonPaymentMultipleWayaOfficial(HttpServletRequest request, List<NonWayaPaymentMultipleOfficialDTO> transfer) {
		ResponseEntity<?> resp = null;
		ArrayList<Object> rpp = new ArrayList<>();
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());

		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				for(NonWayaPaymentMultipleOfficialDTO data: transfer){

					resp = NonPaymentFromOfficialAccount(request, data);
					rpp.add(resp.getBody());
				}
				return new ResponseEntity<>(rpp, HttpStatus.OK);
			case ProviderType.TEMPORAL:
				for(NonWayaPaymentMultipleOfficialDTO data: transfer){
					resp = NonPaymentFromOfficialAccount(request, data);
					rpp.add(resp.getBody());
				}
				return new ResponseEntity<>(rpp, HttpStatus.OK);
			default:
				for(NonWayaPaymentMultipleOfficialDTO data: transfer){
					resp = NonPaymentFromOfficialAccount(request, data);
					rpp.add(resp.getBody());
				}
				return new ResponseEntity<>(rpp, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> TransferNonPaymentWayaOfficialExcel(HttpServletRequest request, MultipartFile file) {

		BulkNonWayaTransferExcelDTO bulkLimt = null;
		Map<String, ArrayList<ResponseHelper>> responseEntity = null;
		if (ExcelHelper.hasExcelFormat(file)) {
			try {
			 responseEntity = MultipleUpload(request,ExcelHelper.excelToNoneWayaTransferPojo(file.getInputStream(), file.getOriginalFilename()));

			} catch (Exception e) {
				throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}

		return new ResponseEntity<>(responseEntity, HttpStatus.OK);
	}

//	public ByteArrayInputStream createExcelSheet(boolean isNoneWaya) {
//		List<String> HEADERS = isNoneWaya ? ExcelHelper.PRIVATE_TRANSFER_HEADERS :
//				ExcelHelper.PRIVATE_USER_HEADERS;
//		return ExcelHelper.createExcelSheet(HEADERS);
//	}
	public ByteArrayInputStream createExcelSheet(String isOnBhalfNoneWaya) {

		String HEADERS = isOnBhalfNoneWaya;
		switch (HEADERS){
			case "PRIVATE_USER_HEADERS" :
				return ExcelHelper.createExcelSheet(ExcelHelper.PRIVATE_USER_HEADERS);
			case "PRIVATE_TRANSFER_HEADERS" :
				return ExcelHelper.createExcelSheet(ExcelHelper.PRIVATE_TRANSFER_HEADERS);
			default:
				return ExcelHelper.createExcelSheet(ExcelHelper.TRANSFER_HEADERS);
		}
//        List<String> HEADERS = isOnBhalfNoneWaya ? ExcelHelper.ON_BEHALF_OF_USER :
//                ExcelHelper.PRIVATE_USER_HEADERS;
//        return ExcelHelper.createExcelSheet(HEADERS);
	}

	private Map<String, ArrayList<ResponseHelper>> MultipleUpload(HttpServletRequest request, @Valid BulkNonWayaTransferExcelDTO transferExcelDTO){
		ResponseEntity<?> resp = null;
		ArrayList<ResponseHelper> respList = new ArrayList<>();
		Map<String, ArrayList<ResponseHelper>> map = new HashMap<>();

		if (transferExcelDTO == null || transferExcelDTO.getTransfer().isEmpty())
			throw new CustomException("Transfer List cannot be null or Empty", BAD_REQUEST);

		for (NonWayaPaymentMultipleOfficialDTO mTransfer : transferExcelDTO.getTransfer()) {
			resp = NonPaymentFromOfficialAccount(request, mTransfer);

			respList.add((ResponseHelper) resp.getBody());
 		}
		map.put("Response", respList);
		return map;
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

//	public ResponseEntity<?> NonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer) {
//		ResponseEntity<?>  responseEntity = null;
//		try{
//			transfer.forEach(data ->{
//				responseEntity = NonPayment(request, data);
//			});
//
//		}catch (Exception ex){
//			throw new CustomException(" error here ", HttpStatus.EXPECTATION_FAILED);
//		}
//		return new ResponseEntity<>()
//	}

	private String getCurrentDate(){
		Date tDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		return dateFormat.format(tDate);
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
		log.info("NONPAY transactionToken :: " + transactionToken);
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
						request, tranCategory, false);
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

				log.info("NONPAY :: " + nonpay);
				walletNonWayaPaymentRepo.save(nonpay);


				String tranDate = getCurrentDate();

				String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration(), transactionToken);

				String noneWaya = formatMoneWayaMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration(), transactionToken);

				if (!StringUtils.isNumeric(transfer.getEmailOrPhoneNo())) {
					log.info("EMAIL: " + transfer.getEmailOrPhoneNo());

					CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
							tranId, tranDate, transfer.getTranNarration()));

					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
							userToken.getPhoneNumber(), noneWaya, userToken.getId()));

					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId(),NON_WAYA_PAYMENT_REQUEST));
				} else {
					log.info("PHONE: " + transfer.getEmailOrPhoneNo());
					
					CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
							userToken.getEmail(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
							tranDate, transfer.getTranNarration()));

					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), noneWaya, userToken.getId()));

					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId(),NON_WAYA_PAYMENT_REQUEST));
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATED", transaction),
						HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp);

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

	public ResponseEntity<?> NonPaymentFromOfficialAccount(HttpServletRequest request, NonWayaPaymentMultipleOfficialDTO transfer) {

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
		String debitAccountNumber = transfer.getOfficialAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVAILED ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("", debitAccountNumber, "NONWAYAPT", transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createEventTransactionNew(transfer.getOfficialAccountNumber(), "NONWAYAPT",
						transfer.getTranCrncy(), transfer.getAmount(), tranType, transfer.getTranNarration(), reference,
						request, tranCategory, true);
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
						tranId, transfer.getOfficialAccountNumber(), transfer.getAmount(), transfer.getTranNarration(),
						transfer.getTranCrncy(), transfer.getPaymentReference(), userToken.getId().toString(),
						userToken.getEmail(), PaymentStatus.PENDING, transfer.getFullName());
				walletNonWayaPaymentRepo.save(nonpay);

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration(), transactionToken);

				String noneWaya = formatMoneWayaMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration(), transactionToken);

				if (!StringUtils.isNumeric(transfer.getEmailOrPhoneNo())) {
					log.info("EMAIL: " + transfer.getEmailOrPhoneNo());

					CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
							tranId, tranDate, transfer.getTranNarration()));

					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
							userToken.getPhoneNumber(), noneWaya, userToken.getId()));

					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), message, userToken.getId(),NON_WAYA_PAYMENT_REQUEST));
				} else {
					log.info("PHONE: " + transfer.getEmailOrPhoneNo());

					CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
							userToken.getEmail(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
							tranDate, transfer.getTranNarration()));

					CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
							transfer.getEmailOrPhoneNo(), noneWaya, userToken.getId()));

					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
							userToken.getId().toString(), message, userToken.getId(),NON_WAYA_PAYMENT_REQUEST));
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

	public ResponseEntity<?> getListOfNonWayaTransfers(HttpServletRequest request, String userId, int page, int  size) {

		try{
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			MyData userToken = tokenService.getTokenUser(token);
			if (userToken == null) {
				return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
			}


			Pageable paging = PageRequest.of(page, size);
			Page<WalletNonWayaPayment> walletNonWayaPaymentPage = walletNonWayaPaymentRepo.findAllByCreatedBy(userId,paging);
			List<WalletNonWayaPayment> walletNonWayaPaymentList = walletNonWayaPaymentPage.getContent();
			Map<String, Object> response = new HashMap<>();

			response.put("nonWayaList", walletNonWayaPaymentList);
			response.put("currentPage", walletNonWayaPaymentPage.getNumber());
			response.put("totalItems", walletNonWayaPaymentPage.getTotalElements());
			response.put("totalPages", walletNonWayaPaymentPage.getTotalPages());

			return new ResponseEntity<>(new SuccessResponse("Data Retrieved", response),
					HttpStatus.CREATED);

		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> listOfNonWayaTransfers(HttpServletRequest request, int page, int  size) {

		try{
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			MyData userToken = tokenService.getTokenUser(token);
			if (userToken == null) {
				return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
			}


			Pageable paging = PageRequest.of(page, size);
			Page<WalletNonWayaPayment> walletNonWayaPaymentPage = walletNonWayaPaymentRepo.findAllDetails(paging);
			List<WalletNonWayaPayment> walletNonWayaPaymentList = walletNonWayaPaymentPage.getContent();
			Map<String, Object> response = new HashMap<>();

			response.put("nonWayaList", walletNonWayaPaymentList);
			response.put("currentPage", walletNonWayaPaymentPage.getNumber());
			response.put("totalItems", walletNonWayaPaymentPage.getTotalElements());
			response.put("totalPages", walletNonWayaPaymentPage.getTotalPages());

			return new ResponseEntity<>(new SuccessResponse("Data Retrieved", response),
					HttpStatus.CREATED);

		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
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
						request, tranCategory, false);
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

				Date tDate = new Date();
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
						() -> customNotification.pushInApp(token, fullName, userToken.getId().toString(), message, userToken.getId(),TRANSACTION_HAS_OCCURRED));

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
						tranNarration, paymentReference, request, tranCategory, false);
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

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				String message = formatMessage(amount, tranId, tranDate, tranCrncy, tranNarration, transactionToken);
				CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, fullName, emailAddress,
						message, userToken.getId(), amount.toString(), tranId, tranDate, tranNarration));
				CompletableFuture.runAsync(
						() -> customNotification.pushSMS(token, fullName, phoneNo, message, userToken.getId()));
				CompletableFuture.runAsync(
						() -> customNotification.pushInApp(token, fullName, userToken.getId().toString(), message, userToken.getId(),TRANSACTION_HAS_OCCURRED));

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
			// check if Transaction is still valid

			WalletNonWayaPayment data1 = walletNonWayaPaymentRepo
					.findByToken(transfer.getToken()).orElse(null);
			if (data1.getStatus().equals(PaymentStatus.REJECT)) {
				return new ResponseEntity<>(new ErrorResponse("TOKEN IS NO LONGER VALID"), HttpStatus.BAD_REQUEST);
			}else if(data1.getStatus().equals(PaymentStatus.PAYOUT)){
				return new ResponseEntity<>(new ErrorResponse("TRANSACTION HAS BEEN PAYED OUT"), HttpStatus.BAD_REQUEST);
			}else if(data1.getStatus().equals(PaymentStatus.EXPIRED)){
				return new ResponseEntity<>(new ErrorResponse("TOKEN FOR THIS TRANSACTION HAS EXPIRED"), HttpStatus.BAD_REQUEST);
			}

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
							redeem.getEmailOrPhone(), message, userToken.getId(),TRANSACTION_HAS_OCCURRED));
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
					//BigDecimal amount, String tranId, String tranDate
					String message = formatMessageRedeem(redeem.getTranAmount(), payRef);
					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, redeem.getFullName(),
							redeem.getEmailOrPhone(), message, userToken.getId(),TRANSACTION_PAYOUT));
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
						String message = formatMessengerRejection(redeem.getTranAmount(), payRef);
						CompletableFuture.runAsync(() -> customNotification.pushInApp(token, redeem.getFullName(),
								redeem.getEmailOrPhone(), message, userToken.getId(),TRANSACTION_REJECTED));
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

		WalletNonWayaPayment check = walletNonWayaPaymentRepo
				.findByToken(transfer.getTokenId()).orElse(null);
		if (check == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID PIN.PLEASE CHECK IT"), HttpStatus.BAD_REQUEST);
		}else if (check.getStatus().equals(PaymentStatus.REJECT)){
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN OR TOKEN HAS EXPIRED AFTER 30DAYs"), HttpStatus.BAD_REQUEST);
		}

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

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message, userToken.getId(),TRANSACTION_HAS_OCCURRED));
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

	public ResponseEntity<?> CommissionPayment(HttpServletRequest request, EventPaymentDTO transfer){
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

				Date tDate = new Date();
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
				// send commission to
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message, userToken.getId(),COMMISSION));
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


	public ResponseEntity<?> BankTransferPaymentOfficial(HttpServletRequest request, BankPaymentOfficialDTO transfer){
		Provider provider = switchWalletService.getActiveProvider(); //
		System.out.println("provider :: {} " + provider);
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				return BankPaymentOffice(request, transfer);
			case ProviderType.TEMPORAL:
				return BankPaymentOffice(request, transfer);
			default:
				return BankPaymentOffice(request, transfer);
		}
	}
	public ResponseEntity<?> BankPaymentOffice(HttpServletRequest request, BankPaymentOfficialDTO transfer) {
		log.info("BankPayment :: {} " + transfer);
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
		log.info("after TransactionGenerate  :: {} " + reference);
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("WITHDRAW");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());
		log.info("after CategoryType and  TransactionTypeEnum :: {} " + tranType);

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("", "", toAccountNumber, transfer.getAmount(), reference);
			log.info("after PaymenttranInsert :: {} " + intRec);
			if (intRec == 1) {
				String tranId = BankTransactionPayOffice("WEMABK", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference, transfer.getBankName(),
						request, tranCategory, transfer.getSenderName(), transfer.getReceiverName());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}

				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				log.info("WalletTransaction :: " + transaction.get() );

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction.get()), HttpStatus.CREATED);

				Date tDate = Calendar.getInstance().getTime();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				log.info("toAccountNumber :: {} " + toAccountNumber);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				log.info("WalletAccount :: {} " + xAccount);


				String email = userToken.getEmail();
				String phone = userToken.getPhoneNumber();
				String fullName = userToken.getFirstName() + " " + userToken.getSurname();
				String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						email, message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, phone,
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, phone,
						message, userToken.getId(),BANK_TRANSACTION));
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
		System.out.println("provider :: {} " + provider);
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
		log.info("BankPayment :: {} " + transfer);
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
		log.info("after TransactionGenerate  :: {} " + reference);
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("WITHDRAW");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());
		log.info("after CategoryType and  TransactionTypeEnum :: {} " + tranType);

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("BANKPMT", "", toAccountNumber, transfer.getAmount(), reference);
			log.info("after PaymenttranInsert :: {} " + intRec);
			if (intRec == 1) {
				String tranId = BankTransactionPay("BANKPMT", toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference, transfer.getBankName(),
						request, tranCategory, transfer.getSenderName(), transfer.getReceiverName());
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}


				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				log.info("WalletTransaction :: " + transaction.get() );

				if (!transaction.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction.get()), HttpStatus.CREATED);

				String tranDate = getCurrentDate();

				log.info("toAccountNumber :: {} " + toAccountNumber);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				log.info("WalletAccount :: {} " + xAccount);

				WalletUser xUser = walletUserRepository.findByAccount(xAccount);
				log.info("WalletUser :: {} " + xUser);
				String fullName = xUser.getFirstName() + " " + xUser.getLastName();

				String email = xUser.getEmailAddress();
				String phone = xUser.getMobileNo();

				String description = "Withdrawal " + " - to " + fullName;
				String sender = userToken.getSurname()+ " " + userToken.getFirstName();

				String message = formatNewMessage(transfer.getAmount(), tranId, new Date().toString()
						, transfer.getTranCrncy(),
						transfer.getTranNarration(),transfer.getSenderName(), transfer.getReceiverName(), xAccount.getClr_bal_amt(), description, transfer.getBankName());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						email, message, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, phone,
						message, userToken.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message, userToken.getId(),CategoryType.WITHDRAW.name()));

				// notify debit account
				// nofify web hook
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


	private void WebhookNotification(){
		// get the user account and the callback Url
		// apend the call back url to the proxy endpoint
		// save response back from aggregator.
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
		ResponseEntity<?> mm = null;
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			 mm = makeTransfer(request, command, transfer);
		case ProviderType.TEMPORAL:
			 mm = makeTransfer(request, command, transfer);
		default:
			 mm = makeTransfer(request, command, transfer);
		}

		transfer.setBenefAccountNumber(chargesAccount);
		transfer.setAmount(chargesAmount);
		debitTransactionFee(request,transfer);
		return mm;
	}



	public ResponseEntity<?> makeTransfer(HttpServletRequest request, String command,
			TransferTransactionDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		// check if user is a marchent



		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>(new ErrorResponse("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("WAYATRAN", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYATRAN",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				//String tranDate = dateFormat.format(tDate);

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

	public void debitTransactionFee(HttpServletRequest request, TransferTransactionDTO transfer){

		/**
		 * 	deduct transaction charges
		 * 	take 10 from transfer.getDebitAccountNumber();
		 * 	and creadit Waya official wallet
		 * transfer.setBenefAccountNumber();
		 */
		sendMoney(request,transfer);
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
			return MoneyTransfer(request, transfer, false);
		case ProviderType.TEMPORAL:
			return MoneyTransfer(request, transfer, false);
		default:
			return MoneyTransfer(request, transfer, false);
		}
	}

	@Override
	public ResponseEntity<?> sendMoneyToSimulatedUser(HttpServletRequest request, List<TransferSimulationDTO> transfer) {
		// check that only admin can perform this action

		ResponseEntity<?> resp = null;
		ArrayList<Object> rpp = new ArrayList<>();
		try{
			for (TransferSimulationDTO data: transfer){
				resp = MoneyTransferSimulation(request,data,true);
				rpp.add(resp.getBody());
			}
			return resp;
		}catch (Exception ex){
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<?> MoneyTransferSimulation(HttpServletRequest request, TransferSimulationDTO transfer, boolean isSimulated) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);

		if(!userToken.isCorporate()){
			return new ResponseEntity<>(new ErrorResponse("ONLY ADMIN CAN PERFORM THIS ACTION"), HttpStatus.BAD_REQUEST);
		}

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
			int intRec = tempwallet.PaymenttranInsert("WAYASIMU", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYASIMU", fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				if(!isSimulated){

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
					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
							message1, userToken.getId(),SIMULATED_TRANSACTION));

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
					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, xUser.getUserId().toString(),
							message2, userToken.getId(),SIMULATED_TRANSACTION));
				}

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

	public ResponseEntity<?> MoneyTransfer(HttpServletRequest request, TransferTransactionDTO transfer, boolean isSimulated) {

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
			int intRec = tempwallet.PaymenttranInsert("WAYATRAN", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYATRAN",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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

				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION SUCCESSFULLY", transaction), HttpStatus.CREATED);

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				if(!isSimulated){

						WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
						WalletUser xUser = walletUserRepository.findByAccount(xAccount);
						String xfullName = xUser.getFirstName() + " " + xUser.getLastName();
						Long xUserId = xUser.getUserId();

						WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
						WalletUser yUser = walletUserRepository.findByAccount(yAccount);
						String yfullName = yUser.getFirstName() + " " + yUser.getLastName();
						Long userId = yUser.getUserId();

						String description = "From" + xfullName + " - to" + yfullName;


						String message1 = formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
								transfer.getTranNarration(), xUserId.toString(), xAccount.getClr_bal_amt(), description);


						CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
								xUser.getEmailAddress(), message1, xUserId, transfer.getAmount().toString(), tranId,
								tranDate, transfer.getTranNarration()));
						CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
								message1, xUserId));
						CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, null,
								"", message1, xUserId, transfer.getTransactionCategory()));


						String message2 = formatSMSRecipient(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
								transfer.getTranNarration(), xUserId.toString(), yAccount.getClr_bal_amt(), description);

						CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
								yUser.getEmailAddress(), message2, userId, transfer.getAmount().toString(), tranId,
								tranDate, transfer.getTranNarration()));
						CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
								message2, userId));
						CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, userId.toString(),
								"", message2, null, transfer.getTransactionCategory()));

				}

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
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.EXPECTATION_FAILED);
		}
		return resp;
	}

//
//	private void processNotification(WalletAccount xAccount, WalletAccount yAccount, WalletUser xUser, WalletUser yUser, String token, String xfullName, String yfullName, String tranId,
//	BigDecimal amount, String tranDate, String tranCrncy, String tranNarration, String transactionCategory){
//		String description = "From" + xfullName + " - to" + yfullName;
//
//		String message1 = formatDebitMessage(amount, tranId, tranDate, tranCrncy,
//				tranNarration, xUserId.toString(), xAccount.getClr_bal_amt(), description);
//
//
//		CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
//				xUser.getEmailAddress(), message1, xUserId, transfer.getAmount().toString(), tranId,
//				tranDate, transfer.getTranNarration()));
//		CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
//				message1, xUserId));
//		CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName,  userId.toString(),
//				"",message1, xUserId, transactionCategory));
//
//
//		String message2 = formatSMSRecipient(amount, tranId, tranDate, tranCrncy,
//				tranNarration, xUserId.toString(), yAccount.getClr_bal_amt(), description);
//
//		CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
//				yUser.getEmailAddress(), message2, userId, transfer.getAmount().toString(), tranId,
//				tranDate, transfer.getTranNarration()));
//		CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
//				message2, userId));
//		CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, userId.toString(),
//				"", message2, null, transfer.getTransactionCategory()));
//	}

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
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION SUCCESSFULl", transaction), HttpStatus.CREATED);
				log.info("Transaction Response: {}", resp.toString());

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message, userToken.getId(),VIRTUAL_TRANSACTION));

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

//		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
//				LocalDate.now(), transfer.getTranCrncy());
//		if (!transRef.isEmpty()) {
//			Optional<WalletTransaction> ret = transRef.stream()
//					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
//			if (ret.isPresent()) {
//				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
//						"Duplicate Payment Reference on the same Day", null);
//			}
//		}

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("WAYAOFFTOOFF", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYAOFFTOOFF", fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction);
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

		String reference = "";
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}

		String fromAccountNumber = transfer.getOfficeDebitAccount();
		String toAccountNumber = transfer.getCustomerCreditAccount();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

//		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
//				LocalDate.now(), transfer.getTranCrncy());
//
//		if (!transRef.isEmpty()) {
//			Optional<WalletTransaction> ret = transRef.stream()
//					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
//			if (ret.isPresent()) {
//				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
//						"Duplicate Payment Reference on the same Day", null);
//			}
//		}

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("WAYAOFFTOCUS", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					reference);
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYAOFFTOCUS",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), reference,
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				if(StringUtils.isNumeric(toAccountNumber)){
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
					CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
							message2, userToken.getId(),tranCategory.name()));
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
	public ApiResponse<?> OfficialUserTransferSystem(Map<String, String > mapp,String token, HttpServletRequest request, OfficeUserTransferDTO transfer) {

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
		CategoryType tranCategory = CategoryType.valueOf("REVERSAL");

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
			int intRec = tempwallet.PaymenttranInsert("WAYAOFFTOCUS", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYAOFFTOCUS",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
						transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
						request, tranCategory);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction);
				if (!transaction.isPresent()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);

				Date tDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				String tranDate = dateFormat.format(tDate);

				WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);

				String emailAddress = mapp.get("receiverEmail");
				String yfullName = yAccount.getAcct_name();

				String message2 = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
						transfer.getTranNarration());
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
						emailAddress, message2, userToken.getId(), transfer.getAmount().toString(), tranId,
						tranDate, transfer.getTranNarration()));

				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, null,
						message2, userToken.getId(),tranCategory.name()));
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
	public ApiResponse<?> OfficialUserTransfer(HttpServletRequest request, List<OfficeUserTransferDTO> transfer) {
		ApiResponse<?> response = null;
		ArrayList<Object> resObjects = new ArrayList<>();
		for(OfficeUserTransferDTO data: transfer){
			response = OfficialUserTransfer(request, data);

			resObjects.add(response.getData());
		}

		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", resObjects);
	}

	public ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer) {
		log.info("inside AdminsendMoney: {}", transfer);
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID TOKEN", null);
		}
		log.info("Send Money: userToken {}", userToken);
		UserDetailPojo user = authService.AuthUser(transfer.getUserId().intValue());
		if (user == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID USER ID", null);
		}
		log.info("UserDetailPojo: user {} ", user);
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
			int intRec = tempwallet.PaymenttranInsert("WAYAADMTOCUS", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYAADMTOCUS", fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction.get());

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, "0",
						message1, xUser.getUserId(),ADMIN_TRANSACTION));
				//String token, String name, String recipient, String message, Long userId, String category

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
						message2, 0L,ADMIN_TRANSACTION));

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
	public ApiResponse<?> AdminSendMoneyMultiple(HttpServletRequest request, List<AdminLocalTransferDTO> transfer) {
		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		try{
			for (AdminLocalTransferDTO data: transfer){
				resp = AdminsendMoney(request, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION DONE SUCCESSFULLY", resp.getData());
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
			int intRec = tempwallet.PaymenttranInsert("WAYAADMTOCOMTODEFULT", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYAADMTOCOMTODEFULT",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction.get());

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
						message1, userToken.getId(),COMMISSION));

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
						message2, userToken.getId(),COMMISSION));

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
			int intRec = tempwallet.PaymenttranInsert("WAYAADMTOCUMTODEFULT", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYAADMTOCUMTODEFULT",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction.get());

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
						message1, userToken.getId(),COMMISSION));

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
						message2, userToken.getId(),COMMISSION));

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
				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction.get());

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
						message1, userToken.getId(),TRANSFER));

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
						message2, userToken.getId(),TRANSFER));

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
			int intRec = tempwallet.PaymenttranInsert("WAYATRAN", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYATRAN",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction);

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
						message1, userToken.getId(),transfer.getTranNarration()));

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
						message2, userToken.getId(),transfer.getTranNarration()));

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

		ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID ACCOUNT NO", null);
		try {
			int intRec = tempwallet.PaymenttranInsert("WAYAADMTOCUS", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYAADMTOCUS",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction);

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
						message1, userToken.getId(), TRANSFER));

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
						message2, userToken.getId(),TRANSFER));

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
			int intRec = tempwallet.PaymenttranInsert("WAYATRAN", fromAccountNumber, toAccountNumber, transfer.getAmount(),
					transfer.getPaymentReference());
			if (intRec == 1) {
				String tranId = createTransaction(token, "WAYATRAN",fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
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

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction);

				Date tDate = new Date();
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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
						message1, userToken.getId(),TRANSFER));

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
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
						message2, userToken.getId(),TRANSFER));

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

	public String createTransaction(String token, String eventId, String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) throws Exception {
		BigDecimal tranAmCharges = BigDecimal.valueOf(0.0);
		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
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

				// CHECK

//				double total = 0.0;
//				if(eventInfo.isPresent()){
//				WalletEventCharges walletEventCharges =	eventInfo.get();
//				total =	accountDebit.getClr_bal_amt() + walletEventCharges.getTranAmt().doubleValue();
//
//				}
//				System.out.println("TOTAL :::: " + total);

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
				// 600,000  -1, 0, or 1 as this {@code BigDecimal} is numerically  less than, equal to, or greater than {@code val}.
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
			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory, senderName, receiverName);

			n = n + 1;

			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory, senderName, receiverName);
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

			if (eventInfo.isPresent()) {
				WalletEventCharges charge = eventInfo.get();
				WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);

				WalletAccount accountDebitTeller = walletAccountRepository
						.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id()).orElse(null);

			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountDebit = accountDebitTeller;
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
				tranAmCharges = charge.getTranAmt();
			} else {
				System.out.println(debitAcctNo + " Charge here " + charge.getTranAmt());
				accountCredit = accountDebitTeller;
				accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
				tranAmCharges = charge.getTranAmt();
			}

			if(!eventId.equals("WAYAOFFTOCUS")){
				WalletAccount finalAccountCredit = accountCredit;
				WalletAccount finalAccountDebit = accountDebit;
				BigDecimal finalTranAmCharges = tranAmCharges;
				CompletableFuture.runAsync(() -> doDeductTransactionCharges(tokenData, senderName, receiverName, paymentRef, "Wallet Transfer", tranCrncy, tranCategory, TransactionTypeEnum.CHARGES, finalTranAmCharges, finalAccountDebit, finalAccountCredit));
			}

		}
			// credit merchant wallet

			log.info("END TRANSACTION");
			// HttpServletRequest request

			String receiverAcct = accountCredit.getAccountNo();
			String receiverName2 = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));


			if(StringUtils.isNumeric(accountDebit.getAccountNo())){
				WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
				Long xUserId = xUser.getUserId();

				if(xUserId !=null){
					CompletableFuture.runAsync(() -> transactionCountService.makeCount(String.valueOf(xUserId), paymentRef));
				}
			}


			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	private void creditMerchantForIncomingFund(Long userId){
		UserDetailPojo user = authService.AuthUser(userId.intValue());
		if (user !=null && user.is_corporate()) {

			//  fund user Commission wallet

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
			String senderName = accountDebit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, "TRANSFER", token, senderName));

			WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
			if(xUser !=null){
				CompletableFuture.runAsync(() -> transactionCountService.makeCount(xUser.getUserId().toString(), paymentRef));
			}

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
			// notifity debit account


			double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
			accountCredit.setLast_tran_id_cr(tranId);
			accountCredit.setClr_bal_amt(clrbalAmtCr);
			accountCredit.setCum_cr_amt(cumbalCrAmtCr);
			accountCredit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountCredit);
			// notifiy credit account


			log.info("END TRANSACTION");
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			String senderName = accountDebit.getAcct_name();

			String finalTranId = tranId;
			CompletableFuture.runAsync(() ->transactionCountService.makeCount(adminUserId, finalTranId));


			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, "TRANSFER", token,senderName));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventTransactionNew(String debitEvent, String creditEvent, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory, boolean isOffical) {

		System.out.println("this is debitEvent :: " + debitEvent);
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
			if(!isOffical){
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
			}else{
				debitAcctNo = debitEvent;
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

			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			// Update transaction table
			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					mPartran, tranCategory,senderName,receiverName);

			mPartran = mPartran + 1;

			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					mPartran, tranCategory,senderName,receiverName);

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
			String receiverName2 = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));

			WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
			if(xUser !=null){
				CompletableFuture.runAsync(() -> transactionCountService.makeCount(xUser.getUserId().toString(), paymentRef));
			}
			return tranId;
		} catch (Exception ex) {
			log.error(ex.getMessage());
			throw new CustomException(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	public String createEventTransactionSettlement(String eventId, String merchantAcctNo, String wayaCommAcctNo, String tranCrncy,
												   BigDecimal amount, BigDecimal merchantAmount,BigDecimal wayaCommAmount,
										 TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
										 CategoryType tranCategory) throws Exception {

		String tranDate = getTransactionDate();
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, merchantAcctNo);
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
			WalletAccount creditMerchantAcct = walletAccountRepository.findByAccountNo(merchantAcctNo);
			if (creditMerchantAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}
			WalletAccount creditWayaCommissionAcct = walletAccountRepository.findByAccountNo(wayaCommAcctNo);
			if (creditWayaCommissionAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}

			WalletAccount eventAcct = walletAccountRepository.findByAccountNo(merchantAcctNo);
			if (eventAcct == null) {
				return "DJGO|CUSTOMER ACCOUNT DOES NOT EXIST";
			}
			// Does account exist
			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
					.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
			if (!accountDebitTeller.isPresent()) {
				return "DJGO|NO EVENT ACCOUNT";
			}

			WalletAccount accountDebit = accountDebitTeller.get();
//			WalletAccount accountCredit = null;
//			if (charge.isChargeWaya()) {
//				accountDebit = accountDebitTeller.get();
//				accountCredit = walletAccountRepository.findByAccountNo(merchantAcctNo);
//			} else if(charge.isChargeCustomer()){
//				accountCredit = accountDebitTeller.get();
//				accountDebit = walletAccountRepository.findByAccountNo(merchantAcctNo);
//			}

			if (accountDebit == null || creditWayaCommissionAcct == null || creditMerchantAcct == null) {
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

			log.info(creditWayaCommissionAcct.getHashed_no());
			if (!accountDebit.getAcct_ownership().equals("O")) {
				String compareCredit = tempwallet.GetSecurityTest(wayaCommAcctNo);
				log.info(compareCredit);
			}
			String secureCredit = reqIPUtils.WayaDecrypt(creditWayaCommissionAcct.getHashed_no());
			log.info(secureCredit);
			String[] keyCredit = secureCredit.split(Pattern.quote("|"));
			if ((!keyCredit[1].equals(creditWayaCommissionAcct.getAccountNo()))
					|| (!keyCredit[2].equals(creditWayaCommissionAcct.getProduct_code()))
					|| (!keyCredit[3].equals(creditWayaCommissionAcct.getAcct_crncy_code()))) {
				return "DJGO|CREDIT ACCOUNT DATA INTEGRITY ISSUE" +creditWayaCommissionAcct.getAccountNo();
			}


			log.info(creditMerchantAcct.getHashed_no());
			if (!accountDebit.getAcct_ownership().equals("O")) {
				String compareCredit = tempwallet.GetSecurityTest(merchantAcctNo);
				log.info(compareCredit);
			}
			String secureCreditM = reqIPUtils.WayaDecrypt(creditMerchantAcct.getHashed_no());
			log.info(secureCreditM);
			String[] keyCreditM = secureCreditM.split(Pattern.quote("|"));
			if ((!keyCreditM[1].equals(creditMerchantAcct.getAccountNo()))
					|| (!keyCreditM[2].equals(creditMerchantAcct.getProduct_code()))
					|| (!keyCreditM[3].equals(creditMerchantAcct.getAcct_crncy_code()))) {
				return "DJGO|CREDIT ACCOUNT DATA INTEGRITY ISSUE" + creditMerchantAcct.getAccountNo();
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

			if (!creditMerchantAcct.getAcct_ownership().equals("O")) {
				if (creditMerchantAcct.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", creditMerchantAcct.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", creditMerchantAcct.getFrez_code());
				if (creditMerchantAcct.getFrez_code() != null) {
					if (creditMerchantAcct.getFrez_code().equals("C"))
						return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
				}
			}

			if (!creditWayaCommissionAcct.getAcct_ownership().equals("O")) {
				if (creditWayaCommissionAcct.isAcct_cls_flg())
					return "DJGO|CREDIT ACCOUNT IS CLOSED";

				log.info("Credit Account is: {}", creditWayaCommissionAcct.getAccountNo());
				log.info("Credit Account Freeze Code is: {}", creditWayaCommissionAcct.getFrez_code());
				if (creditWayaCommissionAcct.getFrez_code() != null) {
					if (creditWayaCommissionAcct.getFrez_code().equals("C"))
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
			String senderName = accountDebit.getAcct_name();
			String receiverNameCommission = creditWayaCommissionAcct.getAcct_name();

			String receiverName = creditMerchantAcct.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCreditMerhAcct = new WalletTransaction(tranId, creditMerchantAcct.getAccountNo(), merchantAmount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", creditMerchantAcct.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);
			log.info("TRANSACTION CREATION DEBIT: {} WITH CREDIT: {}", tranDebit.toString(), tranCreditMerhAcct.toString());

			WalletTransaction tranCreditWayaComm = new WalletTransaction(tranId, creditWayaCommissionAcct.getAccountNo(), wayaCommAmount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", creditWayaCommissionAcct.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverNameCommission);
			log.info("TRANSACTION CREATION DEBIT: {} WITH CREDIT: {}", tranDebit.toString(), tranCreditMerhAcct.toString());

			walletTransactionRepository.saveAndFlush(tranDebit);
			walletTransactionRepository.saveAndFlush(tranCreditMerhAcct);
			walletTransactionRepository.saveAndFlush(tranCreditWayaComm);
			tempwallet.updateTransaction(paymentRef, amount, tranId);

			double clrbalAmtDr = accountDebit.getClr_bal_amt() - amount.doubleValue();
			double cumbalDrAmtDr = accountDebit.getCum_dr_amt() + amount.doubleValue();
			accountDebit.setLast_tran_id_dr(tranId);
			accountDebit.setClr_bal_amt(clrbalAmtDr);
			accountDebit.setCum_dr_amt(cumbalDrAmtDr);
			accountDebit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountDebit);
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);

			String message = formatDebitMessage(amount, tranId, tranDate, tranCrncy,tranNarrate);
			WalletAccount finalAccountDebit = accountDebit;
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalAccountDebit.getUser().getFirstName(),
					finalAccountDebit.getUser().getMobileNo(), message, finalAccountDebit.getUser().getUserId(),WAYA_POS_SETTLEMENT));

			double clrbalAmtCr = creditMerchantAcct.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCr = creditMerchantAcct.getCum_cr_amt() + amount.doubleValue();
			creditMerchantAcct.setLast_tran_id_cr(tranId);
			creditMerchantAcct.setClr_bal_amt(clrbalAmtCr);
			creditMerchantAcct.setCum_cr_amt(cumbalCrAmtCr);
			creditMerchantAcct.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(creditMerchantAcct);

			String message2 = formatNewMessage(merchantAmount, tranId, tranDate, tranCrncy, tranNarrate);
			WalletAccount finalAccountCredit = creditMerchantAcct;
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalAccountCredit.getUser().getFirstName(),
					finalAccountCredit.getUser().getMobileNo(), message2, finalAccountCredit.getUser().getUserId(),WAYA_POS_SETTLEMENT));


			double clrbalAmtCrC = creditWayaCommissionAcct.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCrC = creditWayaCommissionAcct.getCum_cr_amt() + amount.doubleValue();
			creditWayaCommissionAcct.setLast_tran_id_cr(tranId);
			creditWayaCommissionAcct.setClr_bal_amt(clrbalAmtCrC);
			creditWayaCommissionAcct.setCum_cr_amt(cumbalCrAmtCrC);
			creditWayaCommissionAcct.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(creditWayaCommissionAcct);

			String messageComm = formatNewMessage(wayaCommAmount, tranId, tranDate, tranCrncy, tranNarrate);
			WalletAccount finalAccountCreditComm = creditWayaCommissionAcct;
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalAccountCreditComm.getUser().getFirstName(),
					finalAccountCredit.getUser().getMobileNo(), messageComm, finalAccountCreditComm.getUser().getUserId(),WAYA_POS_SETTLEMENT));


			log.info("END TRANSACTION");

			String debitAcct = accountDebit.getAccountNo();
			String debitName2 = accountDebit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, debitAcct, paymentRef,
					new Date(), tranType.getValue(), userId, debitName2, tranCategory.getValue(), token,senderName));


			String receiverAcct = creditMerchantAcct.getAccountNo();
			String receiverName2 = creditMerchantAcct.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(merchantAmount, receiverAcct, paymentRef+1,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));

			String receiverAcctComm = creditWayaCommissionAcct.getAccountNo();
			String receiverNameComm = creditWayaCommissionAcct.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(wayaCommAmount, receiverAcctComm, paymentRef+2,
					new Date(), tranType.getValue(), userId, receiverNameComm, tranCategory.getValue(), token,senderName));


			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventTransaction(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) throws Exception {
		String tranDate = getTransactionDate();
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
			if (charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else if(charge.isChargeCustomer()){
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}else{
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
			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);
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
			// HttpServletRequest request

			String token = request.getHeader(SecurityConstants.HEADER_STRING);

			String contentType = "";
			if(eventId.equals("NONWAYAPT")){
				contentType = NON_WAYA_PAYMENT_REQUEST;
			}else {
				contentType = WAYA_PAYMENT_REQUEST;
			}

			String message = formatDebitMessage(amount, tranId, tranDate, tranCrncy,tranNarrate);

			String finalContentType1 = contentType;
			WalletUser xUserData = new WalletUser();
			if(StringUtils.isNumeric(accountDebit.getAccountNo())){
				WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
				if(xUser !=null){
					xUserData = xUser;
				}
				WalletUser finalUserData = xUserData;
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalUserData.getFirstName(),
						"0", message, finalUserData.getUserId(), finalContentType1));
			}

			double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
			accountCredit.setLast_tran_id_cr(tranId);
			accountCredit.setClr_bal_amt(clrbalAmtCr);
			accountCredit.setCum_cr_amt(cumbalCrAmtCr);
			accountCredit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountCredit);

			String message2 = formatNewMessage(amount, tranId, tranDate, tranCrncy, tranNarrate);
			String finalContentType = contentType;


			if(StringUtils.isNumeric(accountCredit.getAccountNo())){
				WalletUser xUser = walletUserRepository.findByAccount(accountCredit);
				if(xUser !=null){
					xUserData = xUser;
				}
				WalletUser finalUserData2 = xUserData;
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalUserData2.getFirstName(),
						finalUserData2.getUserId().toString() ,message2, 0L, finalContentType));
			}


			log.info("END TRANSACTION");

			String receiverAcct = accountCredit.getAccountNo();
			String receiverName2 = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));

			WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
			if(xUser !=null){

				CompletableFuture.runAsync(() -> transactionCountService.makeCount(xUser.getUserId().toString(), paymentRef));
			}

			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}



	public String createEventTransactionForBillsPayment(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
										 TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
										 CategoryType tranCategory) {
		String tranDate = getTransactionDate();
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
			if (charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else if(charge.isChargeCustomer()){
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}else{
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

				if (BigDecimal.valueOf(accountDebit.getClr_bal_amt()).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}

				if (BigDecimal.valueOf(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
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
					if (BigDecimal.valueOf(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (BigDecimal.valueOf(oustbal).compareTo(amount) == -1) {
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

			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);
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

			String token = request.getHeader(SecurityConstants.HEADER_STRING);

			String contentType = "";
			if (eventId.equals("AITCOL")){
				contentType = BILLS_PAYNENT;
			}else{
				contentType = eventId;
			}

			String message = formatDebitMessage(amount, tranId, tranDate, tranCrncy,tranNarrate);

			String finalContentType1 = contentType;
			WalletUser xUserData = new WalletUser();
			if(StringUtils.isNumeric(accountDebit.getAccountNo())){
				WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
				if(xUser !=null){
					xUserData = xUser;
				}
				WalletUser finalUserData = xUserData;
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalUserData.getFirstName(),
						finalUserData.getUserId().toString(), message, finalUserData.getUserId(), finalContentType1));
			}

			double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
			accountCredit.setLast_tran_id_cr(tranId);
			accountCredit.setClr_bal_amt(clrbalAmtCr);
			accountCredit.setCum_cr_amt(cumbalCrAmtCr);
			accountCredit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountCredit);

			log.info("END TRANSACTION");

			String receiverAcct = accountDebit.getAccountNo();
			String receiverName2 = accountDebit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));

			WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
			if(xUser !=null){

				CompletableFuture.runAsync(() -> transactionCountService.makeCount(xUser.getUserId().toString(), paymentRef));
			}

			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}




	public String createEventTransactionDebitUserCreditWayaAccount(String eventId, String debitAcctNo, String tranCrncy, BigDecimal amount,
										 TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
										 CategoryType tranCategory, MyData userToken) throws Exception {
		String tranDate = getTransactionDate();
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, eventId);
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
			WalletAccount eventAcct = walletAccountRepository.findByAccountNo(debitAcctNo);
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

			accountCredit = accountDebitTeller.get();
			accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);

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
			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);
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

			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);

			WalletAccount xAccount = walletAccountRepository.findByAccountNo(accountDebit.getAccountNo());
			WalletUser xUser = walletUserRepository.findByAccount(xAccount);
			String fullName = xUser.getFirstName() + " " + xUser.getLastName();

			String message = formatDebitMessage(amount, tranId, tranDate, tranCrncy,tranNarrate);


			String finalTranId = tranId;
			CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
					xUser.getEmailAddress(), message, userToken.getId(), amount.toString(), finalTranId,
					tranDate, tranNarrate));
			CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
					message, userToken.getId()));
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
					message, userToken.getId(), TRANSFER));


//			String messageDebit = formatNewMessage(amount, tranId, tranDate, tranCrncy,tranNarrate);
//
//			WalletAccount finalAccountDebit = accountDebit;
//			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalAccountDebit.getUser().getFirstName(),
//					finalAccountDebit.getUser().getMobileNo(), messageDebit, finalAccountDebit.getUser().getUserId(),NON_WAYA_PAYMENT_REQUEST));

			double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
			accountCredit.setLast_tran_id_cr(tranId);
			accountCredit.setClr_bal_amt(clrbalAmtCr);
			accountCredit.setCum_cr_amt(cumbalCrAmtCr);
			accountCredit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountCredit);

//			String message2 = formatNewMessage(amount, tranId, tranDate, tranCrncy, tranNarrate);
//			WalletAccount finalAccountCredit = accountCredit;
//			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, finalAccountCredit.getUser().getFirstName(),
//					finalAccountCredit.getUser().getMobileNo(), message2, finalAccountCredit.getUser().getUserId(),NON_WAYA_PAYMENT_REQUEST));


			String message2 = formatNewMessage(amount, tranId, tranDate, tranCrncy, tranNarrate);

			CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
					xUser.getEmailAddress(), message2, userToken.getId(), amount.toString(), finalTranId,
					tranDate, tranNarrate));
			CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
					message, userToken.getId()));
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
					message, userToken.getId(), TRANSFER));

			log.info("END TRANSACTION");

			String receiverAcct = accountDebit.getAccountNo();
			String receiverName2 = accountDebit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));

			WalletUser xUser3 = walletUserRepository.findByAccount(accountDebit);
			if(xUser3 !=null){
				CompletableFuture.runAsync(() -> transactionCountService.makeCount(xUser3.getUserId().toString(), paymentRef));
			}



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
			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);
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
			String receiverName2 = accountCredit.getAcct_name();



			CompletableFuture.runAsync(() -> transactionCountService.makeCount(userId, paymentRef));

			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}
////ability to transfer money from the temporal wallet back to waya official account in single or in mass with excel upload
	public String createEventOfficeTransactionModified(String eventId, String fromAccountNumber, String toAccountNumber, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory) throws Exception {
		try {
			int n = 1;
			log.info("START TRANSACTION");
			String tranCount = tempwallet.transactionCount(paymentRef, toAccountNumber);
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
			// Does account exist
//			Optional<WalletAccount> accountDebitTeller = walletAccountRepository
//					.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), "0000");
//			if (!accountDebitTeller.isPresent()) {
//				return "DJGO|NO EVENT ACCOUNT";
//			}

			//For credit Event only
//			Optional<WalletEventCharges> eventCredit = walletEventRepository.findByEventId(creditEventId);
//			if (!eventInfo.isPresent()) {
//				return "DJGO|Event Code Does Not Exist";
//			}
//			WalletEventCharges chargeCredit = eventCredit.get();
//			boolean validate3 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
//			if (!validate3) {
//				return "DJGO|Event Validation Failed";
//			}
//
//			// Does account exist
//			Optional<WalletAccount> accountCreditTeller = walletAccountRepository
//					.findByUserPlaceholder(chargeCredit.getPlaceholder(), chargeCredit.getCrncyCode(), "0000");
//			if (!accountCreditTeller.isPresent()) {
//				return "DJGO|NO EVENT ACCOUNT";
//			}

			WalletAccount accountDebit = walletAccountRepository.findByAccountNo(fromAccountNumber);
			WalletAccount accountCredit = walletAccountRepository.findByAccountNo(toAccountNumber);

			//WalletAccount accountDebit = null;
//			WalletAccount accountCredit = null;
//			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
//				accountDebit = accountDebitTeller.get();
//			}
//			else {
//				accountCredit = accountDebitTeller.get();;
//			}
			
//			if (!chargeCredit.isChargeCustomer() && chargeCredit.isChargeWaya()) {
//				accountCredit = accountCreditTeller.get();
//			} else {
//				accountDebit = accountCreditTeller.get();
//			}
//
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
			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);
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


			CompletableFuture.runAsync(() -> transactionCountService.makeCount(userId, paymentRef));

			log.info("END TRANSACTION");
			// HttpServletRequest request
			String token1 = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName2 = accountCredit.getAcct_name();


			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token1,senderName));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	public String createEventRedeem(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request, String senderName)
			throws Exception {
		String tranDate = getTransactionDate();
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
			// send notification to debit account
			// send receipt to dedit account
			String token = request.getHeader(SecurityConstants.HEADER_STRING);

			String message1 = formatDebitMessage(amount, tranId, tranDate, tranCrncy,tranNarrate);
			WalletAccount finalAccountDebit = accountDebit;
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, "fullName", "",
					message1, finalAccountDebit.getUser().getUserId(),TRANSFER));

			double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
			double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
			accountCredit.setLast_tran_id_cr(tranId);
			accountCredit.setClr_bal_amt(clrbalAmtCr);
			accountCredit.setCum_cr_amt(cumbalCrAmtCr);
			accountCredit.setLast_tran_date(LocalDate.now());
			walletAccountRepository.saveAndFlush(accountCredit);
			// send notification to credit account
			// send receipt to credit account
			String message2 = formatNewMessage(amount, tranId, tranDate, tranCrncy, tranNarrate);
			WalletAccount finalAccountCredit = accountCredit;
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, "", "",
					message2, finalAccountCredit.getUser().getUserId(),TRANSACTION_HAS_OCCURRED));

			log.info("END TRANSACTION");
			// HttpServletRequest request

			String receiverAcct = accountCredit.getAccountNo();
			String receiverName = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName, "TRANSFER", token,senderName));


			// get the userID of the sender
			CompletableFuture.runAsync(() -> transactionCountService.makeCount(userId, paymentRef));


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
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {   // && charge.isChargeWaya()
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
					return "DJGO|DEBIT ACCOUNT IS CLOSED || BLOCKED";
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
			String senderName = accountDebit.getAcct_name();
			String receiverName = accountCredit.getAcct_name();

			String tranNarrate = "WALLET-" + tranNarration;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);
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
			String receiverName2 = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName2, tranCategory.getValue(), token,senderName));




			sendInApp( token,  accountCredit,  tranId,  new Date().toString(), tranCrncy, amount, tokenData, tranNarration,tranCategory);


			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	private void sendInApp(String token, WalletAccount accountCredit, String tranId, String tranDate, String tranCrncy, BigDecimal amount, MyData tokenData, String tranNarration, CategoryType categoryType){
		//WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber.getAccountNo());
		WalletUser xUser = walletUserRepository.findByAccount(accountCredit);
		String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

		String message1 = formatCreditMessage(amount, tranId, tranDate, tranCrncy,
				tranNarration);
		CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
				xUser.getEmailAddress(), message1, tokenData.getId(), amount.toString(), tranId,
				tranDate, tranNarration));
		CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
				message1, xUser.getId()));
		CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, xUser.getUserId().toString(),
				message1, tokenData.getId(),COMMISSION));

	}
//BankPaymentOffice
public String BankTransactionPayOffice(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
								 TransactionTypeEnum tranType, String tranNarration, String paymentRef, String bk,
								 HttpServletRequest request, CategoryType tranCategory, String senderName, String receiverName) throws Exception {
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
				n, tranCategory,senderName,receiverName);

		n = n + 1;
		WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
				tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
				n, tranCategory,senderName,receiverName);

		//  ################ TERSEER ########################
//			tranCredit.setReceiverName(receiverName);
//			tranCredit.setSenderName(senderName);
//			tranDebit.setSenderName(senderName);
//			tranDebit.setReceiverName(receiverName);

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
		String receiverName1 = accountCredit.getAcct_name();
		CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
				new Date(), tranType.getValue(), userId, receiverName1, tranCategory.getValue(), token,senderName));
		return tranId;
	} catch (Exception e) {
		e.printStackTrace();
		return ("DJGO|" + e.getMessage());
	}

}

	public String BankTransactionPay(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, String bk,
			HttpServletRequest request, CategoryType tranCategory, String senderName, String receiverName) throws Exception {
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
			BigDecimal tranAmCharges = BigDecimal.ZERO;
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
				tranAmCharges = charge.getTranAmt();
			} else {
				System.out.println( creditAcctNo + " Charge here " + charge.getTranAmt());
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
				tranAmCharges = charge.getTranAmt();
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

				Double totalAmount = accountDebit.getClr_bal_amt() + charge.getTranAmt().doubleValue();

				if (new BigDecimal(totalAmount).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}

				if (new BigDecimal(totalAmount).compareTo(BigDecimal.ONE) != 1) {
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

				System.out.println("INSIDE MAIN METHOD :: " + tokenData);
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
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			//  ################ TERSEER ########################
//			tranCredit.setReceiverName(receiverName);
//			tranCredit.setSenderName(senderName);
//			tranDebit.setSenderName(senderName);
//			tranDebit.setReceiverName(receiverName);

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

			// deduct charges

			WalletAccount finalAccountCredit = accountCredit;
			WalletAccount finalAccountDebit = accountDebit;
			BigDecimal finalTranAmCharges = tranAmCharges;
			CompletableFuture.runAsync(() -> doDeductTransactionCharges(tokenData ,senderName, receiverName, paymentRef, bk, tranCrncy, tranCategory, tranType, finalTranAmCharges, finalAccountDebit, finalAccountCredit));
			// HttpServletRequest request
			String token = request.getHeader(SecurityConstants.HEADER_STRING);
			String receiverAcct = accountCredit.getAccountNo();
			String receiverName1 = accountCredit.getAcct_name();
			CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
					new Date(), tranType.getValue(), userId, receiverName1, tranCategory.getValue(), token,senderName));
			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}

	private  String doDeductTransactionCharges(MyData tokenData, String senderName, String receiverName, String paymentRef, String bk, String tranCrncy, CategoryType tranCategory, TransactionTypeEnum tranType, BigDecimal chargesAmount, WalletAccount accountDebit, WalletAccount accountCredit){
		try{
			int n = 1;
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
			if (AmtVal.compareTo(chargesAmount) == -1) {
				return "DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED";
			}

			if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(chargesAmount) == -1) {
				return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
			}

			if (new BigDecimal(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
				return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
			}
		}
		// Token Fetch

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
				if (new BigDecimal(oustbal).compareTo(chargesAmount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
			}
			System.out.println("INSIDE MAIN METHOD 2222:: " + tokenData);
			BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
			if (userLim.compareTo(chargesAmount) == -1) {
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



		String tranNarrate = "WALLET-" + "transaction charges" + " TO:" + bk;
		WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), chargesAmount, tranType,
				tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
				n, tranCategory,senderName,receiverName);
			System.out.println("tranDebit ==: : " + tranDebit);

		n = n + 1;
		WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), chargesAmount, tranType,
				tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
				n, tranCategory,senderName,receiverName);

			System.out.println("tranCredit ==: : " + tranCredit);

		//  ################ TERSEER ########################
//			tranCredit.setReceiverName(receiverName);
//			tranCredit.setSenderName(senderName);
//			tranDebit.setSenderName(senderName);
//			tranDebit.setReceiverName(receiverName);

		walletTransactionRepository.saveAndFlush(tranDebit);
		walletTransactionRepository.saveAndFlush(tranCredit);
		tempwallet.updateTransaction(paymentRef, chargesAmount, tranId);

		double clrbalAmtDr = accountDebit.getClr_bal_amt() - chargesAmount.doubleValue();
		double cumbalDrAmtDr = accountDebit.getCum_dr_amt() + chargesAmount.doubleValue();
		accountDebit.setLast_tran_id_dr(tranId);
		accountDebit.setClr_bal_amt(clrbalAmtDr);
		accountDebit.setCum_dr_amt(cumbalDrAmtDr);
		accountDebit.setLast_tran_date(LocalDate.now());
		walletAccountRepository.saveAndFlush(accountDebit);

		double clrbalAmtCr = accountCredit.getClr_bal_amt() + chargesAmount.doubleValue();
		double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + chargesAmount.doubleValue();
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

	private void updatePaymentRequestStatus(String reference){
		Optional<WalletNonWayaPayment> walletNonWayaPayment = walletNonWayaPaymentRepo.findByToken(reference);
		if(walletNonWayaPayment.isPresent()){
			WalletNonWayaPayment walletNonWayaPayment1 = walletNonWayaPayment.get();
			walletNonWayaPayment1.setStatus(PaymentStatus.RESERVED);
			walletNonWayaPaymentRepo.save(walletNonWayaPayment1);
		}
		Optional<WalletPaymentRequest> walletPaymentRequest = walletPaymentRequestRepo.findByReference(reference);
		if(walletPaymentRequest.isPresent()){
			WalletPaymentRequest walletPaymentRequest1 = walletPaymentRequest.get();
			walletPaymentRequest1.setStatus(PaymentRequestStatus.RESERVED);
			walletPaymentRequestRepo.save(walletPaymentRequest1);
		}
	}

	@Override
	public ResponseEntity<?> EventReversePaymentRequest(HttpServletRequest request, EventPaymentRequestReversal eventPay) {

//		UserDetailPojo user = authService.AuthUser(Integer.parseInt(eventPay.getSenderId()));
//		if(user == null){
//			return new ResponseEntity<>(new ErrorResponse("IMBALANCE TRANSACTION"), HttpStatus.BAD_REQUEST);
//		}


		WalletAccount walletAccount = getAcount(Long.valueOf(eventPay.getSenderId()));
		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventPay.getEventId());
		if (!eventInfo.isPresent()){
			return new ResponseEntity<>(new ErrorResponse("EVENT DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
		}
		WalletEventCharges charge = eventInfo.get();
		WalletAccount accountDebitTeller = walletAccountRepository
				.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), walletAccount.getSol_id()).orElse(null);

		OfficeUserTransferDTO officeTransferDTO = new OfficeUserTransferDTO();
		officeTransferDTO.setAmount(eventPay.getAmount());
		officeTransferDTO.setCustomerCreditAccount(walletAccount.getAccountNo());
		officeTransferDTO.setOfficeDebitAccount(accountDebitTeller.getAccountNo());
		officeTransferDTO.setTranType(TransactionTypeEnum.REVERSAL.getValue());
		officeTransferDTO.setTranNarration(eventPay.getTranNarration());
		officeTransferDTO.setTranCrncy(eventPay.getTranCrncy());
		officeTransferDTO.setPaymentReference(eventPay.getPaymentReference());
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}

		log.info("WALLET PROVIDER: " + provider.getName());
		ApiResponse<?> res;
		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				res = OfficialUserTransfer(request, officeTransferDTO);
				if (!res.getStatus()) {
					return new ResponseEntity<>(res, HttpStatus.EXPECTATION_FAILED);
				}
				CompletableFuture.runAsync(() -> updatePaymentRequestStatus(eventPay.getPaymentRequestReference()));
				return new ResponseEntity<>(res, HttpStatus.OK);
			case ProviderType.TEMPORAL:
				res = OfficialUserTransfer(request, officeTransferDTO);
				if (!res.getStatus()) {
					return new ResponseEntity<>(res, HttpStatus.EXPECTATION_FAILED);
				}
				CompletableFuture.runAsync(() -> updatePaymentRequestStatus(eventPay.getPaymentRequestReference()));
				return new ResponseEntity<>(res, HttpStatus.OK);
			default:
				res = OfficialUserTransfer(request, officeTransferDTO);
				if (!res.getStatus()) {
					return new ResponseEntity<>(res, HttpStatus.EXPECTATION_FAILED);
				}
				CompletableFuture.runAsync(() -> updatePaymentRequestStatus(eventPay.getPaymentRequestReference()));
				return new ResponseEntity<>(res, HttpStatus.OK);
		}
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
				PostReverse(request, trans1, n);
				n++;
			}
			List<WalletTransaction> statement = walletTransactionRepository.findByTransaction(tranId, LocalDate.now(),
					reverseDto.getTranCrncy());
			return new ResponseEntity<>(new SuccessResponse("REVERSE SUCCESSFULLY", statement), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(new SuccessResponse("TRANSACTION DATE WAS IN PAST", null), HttpStatus.CREATED);
		}

	}

	public void PostReverse(HttpServletRequest request, WalletTransaction trans, Integer n) {
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


		WalletUser xUser = walletUserRepository.findByAccount(RevAcct);

		if(xUser !=null){
			String fullName = xUser.getFirstName() + " " + xUser.getLastName();

			String token = request.getHeader(SecurityConstants.HEADER_STRING);

			String message2 = formatNewMessage(trans.getTranAmount(), trans.getTranId(), trans.getTranDate().toString(), trans.getTranCrncyCode(), trans.getTranNarrate());

			CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
					xUser.getEmailAddress(), message2, tokenData.getId(), trans.getTranAmount().toString(), trans.getTranId(),
					trans.getTranDate().toString(), trans.getTranNarrate()));
			CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
					message2, tokenData.getId()));
			CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
					message2, tokenData.getId(), TRANSACTION_HAS_OCCURRED));
		}

		log.info("END TRANSACTION");

	}

	public ResponseEntity<?> TranReversePaymentRevised(HttpServletRequest request, ReverseTransactionDTO reverseDto)
			throws ParseException {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
			case ProviderType.MAINMIFO:
				return ReversePaymentRevised(request, reverseDto);
			case ProviderType.TEMPORAL:
				return ReversePaymentRevised(request, reverseDto);
			default:
				return ReversePaymentRevised(request, reverseDto);
		}
	}

	public ResponseEntity<?> ReversePaymentRevised(HttpServletRequest request, ReverseTransactionDTO reverseDto)
			throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

		LocalDate reverseDate = reverseDto.getTranDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		BigDecimal debitAmount = new BigDecimal("0"), creditAmount = new BigDecimal("0");
		ArrayList<String> account = new ArrayList<String>();

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
				PostReverse(request, trans1, n);
				n++;
			}
			List<WalletTransaction> statement = walletTransactionRepository.findByTransaction(tranId, LocalDate.now(),
					reverseDto.getTranCrncy());

			// send notification

			return new ResponseEntity<>(new SuccessResponse("REVERSE SUCCESSFULLY", statement), HttpStatus.CREATED);


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
	public ApiResponse<?> PaymentOffTrans(int page, int size, String fillter) {
		Pageable pagable = PageRequest.of(page,size);
		Page<WalletTransaction> walletTransactionPage = walletTransactionRepository.findByAccountOfficial3(pagable,fillter);
		List<WalletTransaction> transaction = walletTransactionPage.getContent();
		Map<String, Object> response = new HashMap<>();


		response.put("transaction", transaction);
		response.put("currentPage", walletTransactionPage.getNumber());
		response.put("totalItems", walletTransactionPage.getTotalElements());
		response.put("totalPages", walletTransactionPage.getTotalPages());

		if (transaction.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "OFFICIAL ACCOUNT SUCCESSFULLY", response);
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

	public List<TransWallet> statementReport2(Date fromdate, Date todate, String acctNo) {
		List<TransWallet> transaction2 = tempwallet.GetTransactionType2(acctNo,fromdate,todate);

		return transaction2;
	}

	@Override
	public ApiResponse<List<AccountStatementDTO>> ReportTransaction2(String accountNo) {
		return null;
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
				PostReverse(request,trans1, n);
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
		System.out.println("fromEventIdBankAccount :: " + eventId );
		WalletEventCharges event = walletEventRepository.findByEventId(eventId)
				.orElseThrow(() -> new NoSuchElementException("EVENT ID NOT AVAILABLE FOR EventId FIRST:" + eventId));
		System.out.println("fromEventIdBankAccount event:: " + event );
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

	public String formatMoneWayaMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy, String narration,
										String tokenId) {

		String message = "" + "\n";
		message = message + "" + "A transaction has occurred with token id: " + tokenId
				+ "  on your account see details below." + "\n";
		message = message + "" + "Amount :" + amount + "\n";
		message = message + "" + "tranId :" + tranId + "\n";
		message = message + "" + "tranDate :" + tranDate + "\n";
		message = message + "" + "Currency :" + tranCrncy + "\n";
		message = message + "" + "Narration :" + narration + "\n";
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

	public String formatNewMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
								   String narration, String sender, String reciever, double availableBalance, String description, String bank) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "A bank withdrawl has occurred"
				+ " see details below" + "\n";
		message = message + "" + "Amount :" + amount + "\n";
		message = message + "" + "tranId :" + tranId + "\n";
		message = message + "" + "tranDate :" + tranDate + "\n";
		message = message + "" + "Currency :" + tranCrncy + "\n";
		message = message + "" + "Narration :" + narration + "\n";
		message = message + "" + "Desc :" + description + "\n";
		message = message + "" + "Avail Bal :" + availableBalance + "\n";
		message = message + "" + "Reciever :" + reciever + "\n";
		message = message + "" + "Bank :" + bank + "\n";
		return message;
	}

	public String formatSMSRecipient(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
								   String narration, String sender, double availableBalance, String description) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "A credit transaction has occurred"
				+ "  on your account see details below" + "\n";
		message = message + "" + "Amount :" + amount + "\n";
		message = message + "" + "tranId :" + tranId + "\n";
		message = message + "" + "tranDate :" + tranDate + "\n";
		message = message + "" + "Currency :" + tranCrncy + "\n";
		message = message + "" + "Narration :" + narration + "\n";
		message = message + "" + "Desc :" + description + "\n";
		message = message + "" + "Avail Bal :" + availableBalance + "\n";
		return message;
	}

	public String formatNewMessageReversal(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
								   String narration) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "A reversal transaction has occurred"
				+ "  on your account see details below" + "\n";
		message = message + "" + "Amount :" + amount + "\n";
		message = message + "" + "tranId :" + tranId + "\n";
		message = message + "" + "tranDate :" + tranDate + "\n";
		message = message + "" + "Currency :" + tranCrncy + "\n";
		message = message + "" + "Narration :" + narration + "\n";
		return message;
	}

	public String formatCreditMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
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

	public String formatDebitMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
									 String narration, String sender, double availableBalance, String description) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "A debit transaction has occurred"
				+ "  on your account see details below" + "\n";
		message = message + "" + "Amount :" + amount + "\n";
		message = message + "" + "tranId :" + tranId + "\n";
		message = message + "" + "tranDate :" + tranDate + "\n";
		message = message + "" + "Currency :" + tranCrncy + "\n";
		message = message + "" + "Narration :" + narration + "\n";
		message = message + "" + "Desc :" + description + "\n";
		message = message + "" + "Avail Bal :" + availableBalance + "\n";
		message = message + "" + "Sender :" + sender + "\n";
		return message;
	}

	public String formatMessagePIN(String pin) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "Kindly confirm the reserved transaction with received pin: " + pin;
		return message;
	}

	public String formatMessageRedeem(BigDecimal amount, String tranId) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "Transaction payout has occurred"
				+ " on your account Amount =" + amount + " Transaction Id = " + tranId;
		return message;
	}

	public String formatMessengerRejection(BigDecimal amount, String tranId) {

		String message = "" + "\n";
		message = message + "" + "Message :" + "Transaction has been request"
				+ " on your account Amount =" + amount + " Transaction Id = " + tranId;
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
		System.out.println(" getAcount ::  " + userId);
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

	public WalletAccount getOfficialAccount(String accountNo) {
		System.out.println(" getOfficialAcount ::  " + accountNo);
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
		if (account == null) {
			throw new CustomException("INVALID USER ID", HttpStatus.BAD_REQUEST);
		}
		return account;
	}


	public WalletPaymentRequest getWalletPaymentRequest(PaymentRequest request) {
		WalletPaymentRequest wayauser = new WalletPaymentRequest();
		wayauser.setReceiverEmail(request.getReceiverEmail());
		wayauser.setReceiverPhoneNumber(request.getReceiverName());
		wayauser.setReceiverId( request.getReceiverId());
		wayauser.setSenderId(request.getSenderId());
		wayauser.setAmount(request.getAmount());
		wayauser.setDeleted(request.isDeleted());
		wayauser.setStatus(request.getStatus());
		wayauser.setRejected(request.isRejected());
		wayauser.setWayauser(request.isWayauser());
		wayauser.setReason(request.getReason());
		wayauser.setReference(request.getReference());
		wayauser.setCreatedAt(request.getCreatedAt());
		wayauser.setCategory(request.getTransactionCategory());
		return wayauser;
	}

	@Override
	public ResponseEntity<?> WayaPaymentRequestUsertoUser(HttpServletRequest request, WayaPaymentRequest transfer) {
		try{

			if (transfer.getPaymentRequest().getStatus().name().equals("PENDING")) {
				WalletPaymentRequest mPayRequest = walletPaymentRequestRepo
						.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
				if (mPayRequest != null) {
					throw new CustomException("Reference ID already exist", HttpStatus.BAD_REQUEST);
				}
	//			WalletPaymentRequest spay = getWalletPaymentRequest(transfer.getPaymentRequest());
				WalletPaymentRequest spay = new WalletPaymentRequest(transfer.getPaymentRequest());
				WalletPaymentRequest mPay = walletPaymentRequestRepo.save(spay);
				return new ResponseEntity<>(new SuccessResponse("SUCCESS", mPay), HttpStatus.CREATED);
			} else if (transfer.getPaymentRequest().getStatus().name().equals("PAID")) {

				WalletPaymentRequest mPayRequest = walletPaymentRequestRepo
						.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
				log.info("mPayRequest reSPONSE :: {}", mPayRequest);
				if (mPayRequest == null) {
					throw new CustomException("Reference ID does not exist", HttpStatus.BAD_REQUEST);
				}
				if (mPayRequest.getStatus().name().equals("PENDING") && (mPayRequest.isWayauser())) {
					log.info(" INSIDE IS WAYA IS TRUE: {}", transfer);
					WalletAccount creditAcct = getAcount(Long.valueOf(mPayRequest.getSenderId()));
					log.info(" INSIDE creditAcct : {}", creditAcct);
					WalletAccount debitAcct = getAcount(Long.valueOf(mPayRequest.getReceiverId()));
					log.info(" INSIDE debitAcct : {}", debitAcct);

					TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct.getAccountNo(),
							creditAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN", mPayRequest.getReason(),
							mPayRequest.getReference(), mPayRequest.getCategory().getValue());
					try{
						ResponseEntity<?> res = sendMoney(request, txt);
						log.info(" SEND MONEY RESPONSE : {}", res);
						if (res.getStatusCodeValue() == 200 || res.getStatusCodeValue() == 201) {

							log.info("Send Money: {}", transfer);
							mPayRequest.setStatus(PaymentRequestStatus.PAID);
							walletPaymentRequestRepo.save(mPayRequest);
							return res;
//						return new ResponseEntity<>(new SuccessResponse("SUCCESS", res), HttpStatus.CREATED);
						}
					}catch (Exception e){
						log.info("Send Money Error: {}", e.getMessage());
						throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
					}

				} else if (mPayRequest.getStatus().name().equals("PENDING") && (!mPayRequest.isWayauser())) {
					log.info(" INSIDE IS WAYA IS TRUE: {}", transfer);
					System.out.println("Inside here == " + transfer.getPaymentRequest());
					PaymentRequest mPay = transfer.getPaymentRequest();
					WalletAccount debitAcct = null;
					WalletAccount creditAcct = null;
					// if this request comes from an official transfer.getPaymentRequest().isWayaOfficial() ||
					if (!StringUtils.isNumeric(mPayRequest.getSenderId())) {
						creditAcct = getOfficialAccount(mPayRequest.getSenderId());
						debitAcct = getAcount(Long.valueOf(mPay.getReceiverId()));
						OfficeUserTransferDTO transferDTO = new OfficeUserTransferDTO(creditAcct.getAccountNo(),
								debitAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN", mPayRequest.getReason(),
								mPayRequest.getReference());
						ApiResponse<?> res = OfficialUserTransfer( request,transferDTO);
						System.out.println("RES :: " + res);
						if(res.getStatus()){
							mPayRequest.setReceiverId(mPay.getReceiverId());
							mPayRequest.setStatus(PaymentRequestStatus.PAID);
							walletPaymentRequestRepo.save(mPayRequest);

							return new ResponseEntity<>(res.getData(), HttpStatus.CREATED);
						}else{
							throw new CustomException(res.getMessage(), HttpStatus.EXPECTATION_FAILED);
						}

					}else{
						creditAcct = getAcount(Long.valueOf(mPayRequest.getSenderId()));
						debitAcct = getAcount(Long.valueOf(mPay.getReceiverId()));


						TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct.getAccountNo(),
								creditAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN", mPayRequest.getReason(),
								mPayRequest.getReference(), mPay.getTransactionCategory().getValue());

						try{
							ResponseEntity<?> res = sendMoney(request, txt);

							if (res.getStatusCodeValue() == 200 || res.getStatusCodeValue() == 201) {
								log.info("Send Money: {}", transfer);
								mPayRequest.setReceiverId(mPay.getReceiverId());
								mPayRequest.setStatus(PaymentRequestStatus.PAID);
								walletPaymentRequestRepo.save(mPayRequest);
								return res;
							}
						}catch (Exception e){
							throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
						}

					}

				} else {
					return new ResponseEntity<>(new ErrorResponse("Error occurred"), HttpStatus.NOT_FOUND);
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
				return new ResponseEntity<>(new ErrorResponse("Error occurred"), HttpStatus.NOT_FOUND);
			}
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return null;
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
				log.info("Authorized Transaction Token: {}", tokenResponse.toString());

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

	public ResponseEntity<?>  getTotalNoneWayaPaymentRequest(String userId){
		long count = walletNonWayaPaymentRepo.findAllByTotal(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  getReservedNoneWayaPaymentRequest(String userId){
		long count = walletNonWayaPaymentRepo.findAllByReserved(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  getPayoutNoneWayaPaymentRequest(String userId){
		long count = walletNonWayaPaymentRepo.findAllByPayout(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  getPendingNoneWayaPaymentRequest(String userId){
		long count = walletNonWayaPaymentRepo.findAllByPending(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  getExpierdNoneWayaPaymentRequest(String userId){
		long count = walletNonWayaPaymentRepo.findAllByExpired(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}


	// Amount

	public ResponseEntity<?>  getTotalNoneWayaPaymentRequestAmount(String userId){
		BigDecimal count = walletNonWayaPaymentRepo.findAllByTotalAmount(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  getReservedNoneWayaPaymentRequestAmount(String userId){
		BigDecimal count = walletNonWayaPaymentRepo.findAllByReservedAmount(userId);

		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  getPayoutNoneWayaPaymentRequestAmount(String userId){
		BigDecimal count = walletNonWayaPaymentRepo.findAllByPayoutAmount(userId);
		Map<String, BigDecimal> amount = new HashMap<>();
		amount.put("amount", count);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
	}

	public ResponseEntity<?>  getPendingNoneWayaPaymentRequestAmount(String userId){
		BigDecimal count = walletNonWayaPaymentRepo.findAllByPendingAmount(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  getExpierdNoneWayaPaymentRequestAmount(String userId){
		BigDecimal count = walletNonWayaPaymentRepo.findAllByExpiredAmount(userId);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?>  debitTransactionAmount(){
		//WalletTransactionRepository
		BigDecimal count = walletTransactionRepository.findByAllDTransaction();
		Map<String, BigDecimal> amount = new HashMap<>();
		amount.put("amount", count);
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
	}

	public ResponseEntity<?>  creditTransactionAmount(){
		BigDecimal amount = walletTransactionRepository.findByAllCTransaction();
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
	}

	public ResponseEntity<?>  debitAndCreditTransactionAmount(){
		BigDecimal count = walletTransactionRepository.findByAllDTransaction();
		BigDecimal amount = walletTransactionRepository.findByAllCTransaction();
		BigDecimal total = BigDecimal.valueOf(count.doubleValue() + amount.doubleValue());
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", total), HttpStatus.OK);
	}


	public ResponseEntity<?>  creditTransactionAmountOffical(){
		BigDecimal amount = walletTransactionRepository.findByAllCTransactionOfficial();
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
	}
	public ResponseEntity<?>  debitTransactionAmountOffical(){
		BigDecimal amount = walletTransactionRepository.findByAllDTransactionOfficial();
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
	}

	public ResponseEntity<?>  debitAndCreditTransactionAmountOfficial(){
		BigDecimal count = walletTransactionRepository.findByAllCTransactionOfficial();
		BigDecimal amount = walletTransactionRepository.findByAllDTransactionOfficial();
		BigDecimal total = BigDecimal.valueOf(count.doubleValue() + amount.doubleValue());
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", total), HttpStatus.OK);
	}



}
