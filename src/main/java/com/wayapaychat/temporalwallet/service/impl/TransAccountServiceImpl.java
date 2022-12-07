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
import com.wayapaychat.temporalwallet.entity.*;
import com.wayapaychat.temporalwallet.enumm.*;
import com.wayapaychat.temporalwallet.proxy.MifosWalletProxy;
import com.wayapaychat.temporalwallet.repository.*;
import com.wayapaychat.temporalwallet.service.*;
import com.wayapaychat.temporalwallet.util.*;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.notification.CustomNotification;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TransWallet;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransAccountServiceImpl implements TransAccountService {
	private final WalletUserRepository walletUserRepository;
	private final WalletAccountRepository walletAccountRepository;
	private final WalletAcountVirtualRepository walletAcountVirtualRepository;
	private final ReqIPUtils reqIPUtils;
	private final TemporalWalletDAO tempwallet;
	private final WalletTransactionRepository walletTransactionRepository;
	private final ParamDefaultValidation paramValidation;
	private final WalletEventRepository walletEventRepository;
	private final SwitchWalletService switchWalletService;
	private final TokenImpl tokenService;
	private final ExternalServiceProxyImpl userDataService;
	private final WalletNonWayaPaymentRepository walletNonWayaPaymentRepo;
	private final CustomNotification customNotification;
	private final WalletQRCodePaymentRepository walletQRCodePaymentRepo;
	private final WalletPaymentRequestRepository walletPaymentRequestRepo;
	private final ExternalServiceProxyImpl externalServiceProxy;
	private final AuthProxy authProxy;
	private final TransactionCountService transactionCountService;
	private final UserAccountService userAccountService;
	private final UserPricingRepository userPricingRepository;
	private final MifosWalletProxy mifosWalletProxy;
	private final CoreBankingService coreBankingService;
	private final ModelMapper modelMapper;


	@Autowired
	public TransAccountServiceImpl(WalletUserRepository walletUserRepository, WalletAccountRepository walletAccountRepository, WalletAcountVirtualRepository walletAcountVirtualRepository, ReqIPUtils reqIPUtils, TemporalWalletDAO tempwallet, WalletTransactionRepository walletTransactionRepository, ParamDefaultValidation paramValidation, WalletEventRepository walletEventRepository, SwitchWalletService switchWalletService, TokenImpl tokenService, ExternalServiceProxyImpl userDataService, WalletNonWayaPaymentRepository walletNonWayaPaymentRepo, CustomNotification customNotification, WalletQRCodePaymentRepository walletQRCodePaymentRepo, WalletPaymentRequestRepository walletPaymentRequestRepo, ExternalServiceProxyImpl externalServiceProxy, AuthProxy authProxy, TransactionCountService transactionCountService, UserAccountService userAccountService, UserPricingRepository userPricingRepository, MifosWalletProxy mifosWalletProxy, CoreBankingService coreBankingService, ModelMapper modelMapper) {
		this.walletUserRepository = walletUserRepository;
		this.walletAccountRepository = walletAccountRepository;
		this.walletAcountVirtualRepository = walletAcountVirtualRepository;
		this.reqIPUtils = reqIPUtils;
		this.tempwallet = tempwallet;
		this.walletTransactionRepository = walletTransactionRepository;
		this.paramValidation = paramValidation;
		this.walletEventRepository = walletEventRepository;
		this.switchWalletService = switchWalletService;
		this.tokenService = tokenService;
		this.userDataService = userDataService;
		this.walletNonWayaPaymentRepo = walletNonWayaPaymentRepo;
		this.customNotification = customNotification;
		this.walletQRCodePaymentRepo = walletQRCodePaymentRepo;
		this.walletPaymentRequestRepo = walletPaymentRequestRepo;
		this.externalServiceProxy = externalServiceProxy;
		this.authProxy = authProxy;
		this.transactionCountService = transactionCountService;
		this.userAccountService = userAccountService;
		this.userPricingRepository = userPricingRepository;
		this.coreBankingService = coreBankingService;
		this.mifosWalletProxy = mifosWalletProxy;
		this.modelMapper = modelMapper;
	}


	@Override
	public ResponseEntity<?> adminTransferForUser(HttpServletRequest request, String command,
			AdminUserTransferDTO transfer) {
		String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("ADMINTIL");
		TransferTransactionDTO transferTransactionDTO;
		if(command.toUpperCase().equals("CREDIT")){
			transferTransactionDTO = new TransferTransactionDTO( nonWayaDisbursementAccount, transfer.getCustomerAccountNumber(), transfer.getAmount(),
					TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}else{
			transferTransactionDTO = new TransferTransactionDTO( transfer.getCustomerAccountNumber(), nonWayaDisbursementAccount, transfer.getAmount(),
					TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}

		return coreBankingService.transfer( transferTransactionDTO,  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

	}

	public ResponseEntity<?> cashTransferByAdmin(HttpServletRequest request, String command,
			WalletAdminTransferDTO transfer) {
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			throw new CustomException("INVALID", HttpStatus.NOT_FOUND);
		}

		Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumber());
		if (wallet.isEmpty()) {
			throw new CustomException("EMAIL OR PHONE NO DOES NOT EXIST", HttpStatus.NOT_FOUND);
		}
		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if (defaultAcct.isEmpty()) {
			throw new CustomException("NO ACCOUNT NUMBER EXIST", HttpStatus.NOT_FOUND);
		}
		String toAccountNumber = defaultAcct.get().getAccountNo();

		String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("ADMINTIL");
		TransferTransactionDTO transferTransactionDTO;
		if(command.toUpperCase().equals("CREDIT")){
			transferTransactionDTO = new TransferTransactionDTO( nonWayaDisbursementAccount, toAccountNumber, transfer.getAmount(),
					TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}else{
			transferTransactionDTO = new TransferTransactionDTO( toAccountNumber, nonWayaDisbursementAccount, transfer.getAmount(),
					TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}

		return coreBankingService.transfer( transferTransactionDTO,  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");
	}

	@Override
	public ResponseEntity<?> EventTransferPayment(HttpServletRequest request, EventPaymentDTO transfer, boolean isMifos) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(transfer.getEventId());
		if(eventInfo.isEmpty()){
			return new ResponseEntity<>(new ErrorResponse("ERROR PROCESSING TRANSACTION"), HttpStatus.BAD_REQUEST);
		}

		String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber(transfer.getEventId());
		TransferTransactionDTO transferTransactionDTO;
		if(eventInfo.get().isChargeWaya()){
			transferTransactionDTO = new TransferTransactionDTO( nonWayaDisbursementAccount, transfer.getCustomerAccountNumber(), transfer.getAmount(), 
											TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(), 
															transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}
		else{
			transferTransactionDTO = new TransferTransactionDTO( transfer.getCustomerAccountNumber(), nonWayaDisbursementAccount, transfer.getAmount(), 
											TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(), 
															transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}
		
		return coreBankingService.transfer( transferTransactionDTO,  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

	}

	@Override
	public ResponseEntity<?> EventPaymentSettlement(HttpServletRequest request, EventPaymentSettlementDTO eventPay) {

		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
			case ProviderType.MIFOS:
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

		String reference;
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}

		String merchantDefultAccount = transfer.getMerchantAccountNumber();
		String merchantCommissionAccount = transfer.getWayaCommAccountNumber();

		BigDecimal merchantFee = transfer.getMerchantFee();
		BigDecimal wayaCommissionFee = transfer.getWayaCommissionFee();

		BigDecimal totlaAmount = BigDecimal.valueOf(merchantFee.doubleValue() + wayaCommissionFee.doubleValue());

		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

		ResponseEntity<?> resp; //= new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert(transfer.getEventId(), "", merchantDefultAccount, merchantFee,
					reference);

			String tranId;
			if (intRec == 1) {

				tranId = createEventTransactionSettlement(transfer.getEventId(), merchantDefultAccount, merchantCommissionAccount, transfer.getTranCrncy(),
							totlaAmount, merchantFee, wayaCommissionFee, tranType, transfer.getTranNarration(), reference, request, tranCategory);

				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ResponseEntity<>(new ErrorResponse(tranKey[1]), HttpStatus.BAD_REQUEST);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (transaction.isEmpty()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction), HttpStatus.CREATED);
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
			log.error("Error occurred - GET WALLET TRANSACTION :"+ ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}


	@Override
	public ResponseEntity<?> TemporalWalletToOfficialWalletMutiple(HttpServletRequest request, List<TemporalToOfficialWalletDTO> transfer) {
		ArrayList<Object> list = new ArrayList<>();
		ResponseEntity<?> resp;
		for(TemporalToOfficialWalletDTO data: transfer){
			resp = TemporalWalletToOfficialWallet(request, data);
			list.add(resp);
		}
		return new ResponseEntity<>(list , HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> EventOfficePayment(HttpServletRequest request, EventOfficePaymentDTO transfer) {

		log.info("Transaction Request Creation: {}", transfer.toString());

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String toAccountNumber = transfer.getCreditEventId();
		String fromAccountNumber = transfer.getDebitEventId();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>(new ErrorResponse("DEBIT EVENT CAN'T BE THE SAME WITH CREDIT EVENT"), HttpStatus.BAD_REQUEST);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());


		TransferTransactionDTO	transferTransactionDTO = new TransferTransactionDTO( fromAccountNumber, toAccountNumber, transfer.getAmount(),
				tranType.getValue(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), tranCategory.getValue());

		return coreBankingService.transfer( transferTransactionDTO,  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

	}

	private String getTransactionDate(){
		Date tDate = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(tDate);
	}

	public ResponseEntity<?> TemporalWalletToOfficialWallet(HttpServletRequest request, TemporalToOfficialWalletDTO transfer) {

	//ability to transfer money from the temporal wallet back to waya official account in single or in mass with excel upload

		log.info("Transaction Request Creation: {}", transfer.toString());

		String toAccountNumber = transfer.getOfficialAccountNumber();
		String fromAccountNumber = transfer.getCustomerAccountNumber();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>(new ErrorResponse("DEBIT EVENT CAN'T BE THE SAME WITH CREDIT EVENT"), HttpStatus.BAD_REQUEST);
		}

		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

 		TransferTransactionDTO	transferTransactionDTO = new TransferTransactionDTO( fromAccountNumber, toAccountNumber, transfer.getAmount(),
					TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), tranCategory.getValue());

		return coreBankingService.transfer( transferTransactionDTO,  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

	}

	public ResponseEntity<?> EventNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer){
		ArrayList<Object> list = new ArrayList<>();
		ResponseEntity<?> resp;
		for(NonWayaPaymentDTO data: transfer){
			resp = EventNonPayment(request, data);
			list.add(resp.getBody());
		}
		resp = new ResponseEntity<>(list, HttpStatus.CREATED);
		return resp;
	}

	public ResponseEntity<?> EventNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());


		String toAccountNumber = transfer.getCustomerDebitAccountNo();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
		// debit customer || credit Official Account

		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId("DISBURSE_NONWAYAPT");
		if(eventInfo.isEmpty()){
			throw new CustomException("ERROR PROCESSING TRANSACTION", HttpStatus.NOT_FOUND);
		}

		String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("NONWAYAPT");
		TransferTransactionDTO transferTransactionDTO;
		if(eventInfo.get().isChargeWaya()){
			transferTransactionDTO = new TransferTransactionDTO(nonWayaDisbursementAccount, toAccountNumber,  transfer.getAmount(),
					tranType.name(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}
		else{
			transferTransactionDTO = new TransferTransactionDTO( toAccountNumber, nonWayaDisbursementAccount, transfer.getAmount(),
					TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
					transfer.getPaymentReference(), CategoryType.TRANSFER.getValue());
		}

		return coreBankingService.transfer( transferTransactionDTO,  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

	}

	public ResponseEntity<?> EventNonRedeem(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			throw new CustomException("INVAILED TOKEN", HttpStatus.NOT_FOUND);
		}
		String toAccountNumber = transfer.getCustomerDebitAccountNo();

		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId("DISBURSE_NONWAYAPT");
		if(eventInfo.isEmpty()){
			throw new CustomException("ERROR PROCESSING TRANSACTION", HttpStatus.NOT_FOUND);
		}

	 	String noneWayaAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( noneWayaAccount, toAccountNumber, transfer.getAmount(),
				TransactionTypeEnum.CARD.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), CategoryType.TRANSFER.getValue()),  "NONWAYAPT");


		String tranId = transfer.getPaymentReference();
		String tranDate = getTransactionDate();

		String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
				transfer.getTranNarration());
		CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, transfer.getFullName(),
				transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
				tranId, tranDate, transfer.getTranNarration()));
		CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
				transfer.getEmailOrPhoneNo(), message, userToken.getId()));
 		return debitResponse;
	}

	@Override
	public ResponseEntity<?> EventNonRedeemMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer) {
		ArrayList<Object> list = new ArrayList<>();
		ResponseEntity<?> res = null;
		for (NonWayaPaymentDTO data: transfer){
			res = EventNonRedeem(request,data);
		  		list.add(res.getBody());
			res = new ResponseEntity<>(list,HttpStatus.OK);
			log.info("Transaction Response: {}", list.toString());
		}
		return res;
	}

	@Override
	public ResponseEntity<?> TransferNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer){
		ResponseEntity<?> resp = null;
		ArrayList<Object> rpp = new ArrayList<>();
		for(NonWayaPaymentDTO data: transfer){
			resp = NonPayment(request, data);
			rpp.add(resp.getBody());
		}
		log.info(rpp.toString());
		return resp;

	}

	@Override
	public ResponseEntity<?> TransferNonPaymentMultipleUpload(HttpServletRequest request, MultipartFile file) {

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
			ResponseEntity<?> responseEntity = NonPayment(request, data);
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
			case ProviderType.MIFOS:
				return NonPaymentFromOfficialAccount(request, transfer);
			case ProviderType.TEMPORAL:
				return NonPaymentFromOfficialAccount(request, transfer);
			default:
				return NonPaymentFromOfficialAccount(request, transfer);
		}
	}

	@Override
	public ResponseEntity<?> TransferNonPaymentMultipleWayaOfficial(HttpServletRequest request, List<NonWayaPaymentMultipleOfficialDTO> transfer) {
		ResponseEntity<?> resp;
		ArrayList<Object> rpp = new ArrayList<>();
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());

		switch (provider.getName()) {
			case ProviderType.MIFOS:
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

		switch (isOnBhalfNoneWaya){
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
		ResponseEntity<?> resp;
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
	public ResponseEntity<?> transferToNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		MyData userToken = (MyData)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");
		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( transfer.getCustomerDebitAccountNo(),  nonWayaDisbursementAccount, transfer.getAmount(), 
												TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(), 
																transfer.getPaymentReference(), CategoryType.TRANSFER.getValue()),  "NONWAYAPT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}

		String transactionToken = tempwallet.generateToken();
		WalletNonWayaPayment nonpay =  new WalletNonWayaPayment(transactionToken, transfer.getEmailOrPhoneNo(),
												transfer.getPaymentReference(), transfer.getCustomerDebitAccountNo(), transfer.getAmount(), transfer.getTranNarration(),
												transfer.getTranCrncy(), transfer.getPaymentReference(), userToken.getId().toString(),
												userToken.getEmail(), PaymentStatus.PENDING, transfer.getFullName());
		walletNonWayaPaymentRepo.save(nonpay);


		String tranDate = getCurrentDate();
		String tranId = transfer.getPaymentReference();
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken);
		String noneWaya = formatMoneWayaMessage(transfer.getAmount(), transfer.getPaymentReference(), tranDate, transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken);

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

		return debitResponse;

	}


	public ResponseEntity<?> NonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
		// Debit Customer and credit Waya
		return transferToNonPayment(request, transfer);
	}

	private String getCurrentDate(){
		Date tDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(tDate);
	}



	public ResponseEntity<?> NonPaymentFromOfficialAccount(HttpServletRequest request, NonWayaPaymentMultipleOfficialDTO transfer) {

		log.info("Transaction Request Creation: {}", transfer.toString());
		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String transactionToken = tempwallet.generateToken();
		String debitAccountNumber = transfer.getOfficialAccountNumber();


		String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");
		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( debitAccountNumber,  nonWayaDisbursementAccount, transfer.getAmount(),
				TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), CategoryType.TRANSFER.getValue()),  "NONWAYAPT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}


		String tranDate = getCurrentDate();
		String tranId = transfer.getPaymentReference();

		String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken);
		String noneWaya = formatMoneWayaMessage(transfer.getAmount(), transfer.getPaymentReference(), tranDate, transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken);

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

		return debitResponse;
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
			log.error("Error occurred - GET WALLET TRANSACTION :"+ ex.getMessage());
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
			log.error("GET WALLET TRANSACTION {} " + ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}


	public ResponseEntity<?> TransferNonRedeem(HttpServletRequest request, NonWayaBenefDTO transfer) {
		log.info("Transaction Request Creation: {}", transfer.toString());

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			throw new CustomException("INVALID TOKEN", HttpStatus.BAD_REQUEST);
		}

		WalletUser user = walletUserRepository.findByUserId(transfer.getMerchantId());
		if (user == null) {throw new CustomException("INVALID MERCHANT ID", HttpStatus.BAD_REQUEST);
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

		// Debit NONWAYAPT can credit Customer account

		String noneWayaAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( noneWayaAccount, beneAccount, transfer.getAmount(),
				TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), CategoryType.TRANSFER.getValue()),  "NONWAYAPT");


		String tranDate = getTransactionDate();
		String tranId = transfer.getPaymentReference();

		String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
				transfer.getTranNarration(), transactionToken);
		CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, fullName, emailAddress,
				message, userToken.getId(), transfer.getAmount().toString(), tranId, tranDate,
				transfer.getTranNarration()));
		CompletableFuture.runAsync(
				() -> customNotification.pushSMS(token, fullName, phoneNo, message, userToken.getId()));
		CompletableFuture.runAsync(
				() -> customNotification.pushInApp(token, fullName, userToken.getId().toString(), message, userToken.getId(),TRANSACTION_HAS_OCCURRED));

		return debitResponse;
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


		String noneWayaAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( noneWayaAccount, beneAccount, amount,
				TransactionTypeEnum.TRANSFER.getValue(), "NGN", "TransferNonReject",
				paymentReference, CategoryType.TRANSFER.getValue()),  "NONWAYAPT");


		String tranDate = getTransactionDate();
		String tranId = paymentReference;

		String message = formatMessage(amount, tranId, tranDate, tranCrncy, tranNarration, transactionToken);
		CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, fullName, emailAddress,
				message, userToken.getId(), amount.toString(), tranId, tranDate, tranNarration));
		CompletableFuture.runAsync(
				() -> customNotification.pushSMS(token, fullName, phoneNo, message, userToken.getId()));
		CompletableFuture.runAsync(
				() -> customNotification.pushInApp(token, fullName, userToken.getId().toString(), message, userToken.getId(),TRANSACTION_HAS_OCCURRED));
		return debitResponse;
	}

	@Override
	public ResponseEntity<?> NonWayaPaymentRedeem(HttpServletRequest request, NonWayaRedeemDTO transfer) {
		try {
			// check if Transaction is still valid

			WalletNonWayaPayment data1 = walletNonWayaPaymentRepo
					.findByToken(transfer.getToken()).orElse(null);
			if (Objects.requireNonNull(data1).getStatus().equals(PaymentStatus.REJECT)) {
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

			String messageStatus;
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
					ResponseEntity<?> ress= TransferNonRedeem(request, merchant);
					log.info(ress.toString());
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
						ResponseEntity<?> res = TransferNonReject(request, redeem.getDebitAccountNo(), redeem.getTranAmount(),
								redeem.getCrncyCode(), tranNarrate, payRef);
						log.info("res =" + res);
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
						request, tranCategory, false);
				String[] tranKey = tranId.split(Pattern.quote("|"));
				if (tranKey[0].equals("DJGO")) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
				}
				log.info("Transaction ID Response: {}", tranId);
				Optional<List<WalletTransaction>> transaction = walletTransactionRepository
						.findByTranIdIgnoreCase(tranId);

				if (transaction.isEmpty()) {
					return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
				}

				resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION CREATE", transaction);
				log.info("Transaction Response: {}", resp.toString());

				String tranDate = getTransactionDate();

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

		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getCustomerAccountNumber());
		if (!acctComm.getProduct_code().equals("SB901")) {
			return new ResponseEntity<>(new ErrorResponse("NOT COMMISSION WALLET"), HttpStatus.BAD_REQUEST);
		}

		String toAccountNumber = transfer.getCustomerAccountNumber();

		// take money from commission wallet to user commission wallet
		String officialCommissionAccount = coreBankingService.getEventAccountNumber(transfer.getEventId());

		return coreBankingService.transfer( new TransferTransactionDTO( officialCommissionAccount, toAccountNumber, transfer.getAmount(),
				TransactionTypeEnum.CARD.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), CategoryType.COMMISSION.getValue()),  "COMMPMT");
	}


	public ResponseEntity<?> BankTransferPaymentOfficial(HttpServletRequest request, BankPaymentOfficialDTO transfer){
		Provider provider = switchWalletService.getActiveProvider(); //
		System.out.println("provider :: {} " + provider);
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
			case ProviderType.MIFOS:
				return BankPaymentOffice(request, transfer);
			case ProviderType.TEMPORAL:
				return BankPaymentOffice(request, transfer);
			default:
				return BankPaymentOffice(request, transfer);
		}


	}

	@Override
	public ResponseEntity<?> BankTransferPaymentOfficialMultiple(HttpServletRequest request, List<BankPaymentOfficialDTO> transfer) {

		ResponseEntity<?> response;
		ArrayList<Object> resObjects = new ArrayList<>();
		for(BankPaymentOfficialDTO data: transfer){
			response = BankTransferPaymentOfficial(request, data);

			resObjects.add(response.getBody());
		}

		return new ResponseEntity<>(new SuccessResponse("TRANSACTION SUCCESSFUL",resObjects), HttpStatus.OK);
		//return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", resObjects);

	}

	public ResponseEntity<?> BankPaymentOffice(HttpServletRequest request, BankPaymentOfficialDTO transfer) {

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
		}

		String reference;
		reference = tempwallet.TransactionGenerate();
		if (reference.equals("")) {
			reference = transfer.getPaymentReference();
		}
		log.info("after TransactionGenerate  :: {} " + reference);
		String toAccountNumber = transfer.getCustomerAccountNumber();
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("WITHDRAW");
		CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());


		ResponseEntity<?> resp; // = new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT NO"), HttpStatus.BAD_REQUEST);
		try {
			int intRec = tempwallet.PaymenttranInsert("", "", toAccountNumber, transfer.getAmount(), reference);
			log.info("after PaymenttranInser " + intRec);
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

				if (transaction.isEmpty()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
				}
				resp = new ResponseEntity<>(new SuccessResponse("TRANSACTION CREATE", transaction.get()), HttpStatus.CREATED);

				String tranDate = getTransactionDate();

				log.info("toAccountNumber " + toAccountNumber);

				WalletAccount xAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
				log.info("WalletAccount " + xAccount);

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
			log.error("GET WALLET TRANSACTION " + ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return resp;
	}

	public ResponseEntity<?> BankTransferPayment(HttpServletRequest request, BankPaymentDTO transfer) {

		MyData userToken = (MyData)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String wayaDisbursementAccount = coreBankingService.getEventAccountNumber(transfer.getEventId());

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( transfer.getCustomerAccountNumber(),  wayaDisbursementAccount, transfer.getAmount(),
				TransactionTypeEnum.TRANSFER.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), CategoryType.TRANSFER.getValue()),  "BANKPMT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}

		String tranDate = getCurrentDate();
		String tranId = transfer.getPaymentReference();
		String token = request.getHeader(SecurityConstants.HEADER_STRING);

		log.info("BankPayment  " + transfer);
		WalletAccount xAccount = walletAccountRepository.findByAccountNo(transfer.getCustomerAccountNumber());
		WalletUser xUser = walletUserRepository.findByAccount(xAccount);
		String fullName = xUser.getFirstName() + " " + xUser.getLastName();
		String email = xUser.getEmailAddress();
		String phone = xUser.getMobileNo();

		String description = "Withdrawal " + " - to " + fullName;

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

		return debitResponse;
	}

	public ApiResponse<Page<WalletTransaction>> findAllTransaction(int page, int size) {
		// Pageable paging = PageRequest.of(page, size);
		// Page<WalletTransaction> transaction =
		// walletTransactionRepository.findAll(paging);
		Page<WalletTransaction> transaction = walletTransactionRepository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
	}

	public ApiResponse<List<WalletTransaction>> findClientTransaction(String tranId) {
		Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
		if (transaction.isEmpty()) {
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
		if (account.isEmpty()) {
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
	public ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber) {
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
		if (account == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		}
		Pageable sortedByName = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<WalletTransaction> transaction = walletTransactionRepository.findAllByAcctNum(accountNumber, sortedByName);

		if (transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
	}

	@Override
	public ResponseEntity<?> makeWalletTransaction(HttpServletRequest request, String command,
			TransferTransactionDTO transfer) {

		log.info("command" + command);
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

		TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
		transactionDTO.setTranType(transfer.getTranType());
		transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
		transactionDTO.setTranNarration(transfer.getTranNarration());
		transactionDTO.setTranCrncy(transfer.getTranCrncy());
		transactionDTO.setPaymentReference(transfer.getPaymentReference());
		transactionDTO.setDebitAccountNumber(transfer.getDebitAccountNumber());
		transactionDTO.setBenefAccountNumber(toAccountNumber);
		transactionDTO.setAmount(transfer.getAmount());

		ResponseEntity<?> debitResponse = coreBankingService.transfer(transactionDTO, "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;

	}


	@Override
	public ResponseEntity<?> sendMoney(HttpServletRequest request, TransferTransactionDTO transfer) {

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

		if (fromAccountNumber.trim().equals(toAccountNumber.trim())) {
			log.info(toAccountNumber + "|" + fromAccountNumber);
			return new ResponseEntity<>(new ErrorResponse("DEBIT AND CREDIT ON THE SAME ACCOUNT"),
					HttpStatus.BAD_REQUEST);
		}

		TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
		transactionDTO.setTranType(transfer.getTranType());
		transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
		transactionDTO.setTranNarration(transfer.getTranNarration());
		transactionDTO.setTranCrncy(transfer.getTranCrncy());
		transactionDTO.setPaymentReference(transfer.getPaymentReference());
		transactionDTO.setDebitAccountNumber(transfer.getDebitAccountNumber());
		transactionDTO.setBenefAccountNumber(toAccountNumber);
		transactionDTO.setAmount(transfer.getAmount());

		ResponseEntity<?> debitResponse = coreBankingService.transfer(transactionDTO, "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;

	}

	@Override
	public ResponseEntity<?> sendMoneyToSimulatedUser(HttpServletRequest request, List<TransferSimulationDTO> transfer) {
		// check that only admin can perform this action

		ResponseEntity<?> resp = null;
		ArrayList<Object> rpp = new ArrayList<>();
		try{
			for (TransferSimulationDTO data: transfer){
				resp = MoneyTransferSimulation(data);
				rpp.add(resp.getBody());
			}
			log.info(rpp.toString());
			return resp;
		}catch (Exception ex){
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<?> MoneyTransferSimulation(TransferSimulationDTO transfer) {
		
		return coreBankingService.transfer(
			new TransferTransactionDTO(
				transfer.getDebitAccountNumber(),  transfer.getBenefAccountNumber(), transfer.getAmount(), 
				transfer.getTranType(), transfer.getTranCrncy(),  transfer.getTranNarration(), 
				transfer.getPaymentReference(), CategoryType.FUNDING.getValue()),  "WAYASIMU");

	}

	@Override
	public ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request, DirectTransactionDTO transfer) {


		String wayaDisbursementAccount = coreBankingService.getEventAccountNumber(transfer.getEventId());

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
		if (account.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("NO DEFAULT WALLET FOR VIRTUAL ACCOUNT"),
					HttpStatus.BAD_REQUEST);
		}
		WalletAccount mAccount = account.get();
		String toAccountNumber = mAccount.getAccountNo();

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( wayaDisbursementAccount,  toAccountNumber, transfer.getAmount(),
				TransactionTypeEnum.BANK.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), CategoryType.TRANSFER.getValue()),  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;

	}

	@Override
	public ResponseEntity<?> PostExternalMoney(HttpServletRequest request, CardRequestPojo transfer, Long userId) {
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
		case ProviderType.MIFOS:
			return userDataService.getCardPayment(request, transfer, userId);
		case ProviderType.TEMPORAL:
			return userDataService.getCardPayment(request, transfer, userId);
		default:
			return userDataService.getCardPayment(request, transfer, userId);
		}
	}

	public ResponseEntity<?> OfficialMoneyTransferSw(HttpServletRequest request, OfficeTransferDTO transfer) {

		String token = request.getHeader(SecurityConstants.HEADER_STRING);
		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			throw new CustomException("INVALID TOKEN",HttpStatus.NOT_FOUND);
		}

		String fromAccountNumber = transfer.getOfficeDebitAccount();
		String toAccountNumber = transfer.getOfficeCreditAccount();
		if(fromAccountNumber.equals(toAccountNumber)) {
			throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT",HttpStatus.NOT_FOUND);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( fromAccountNumber,  toAccountNumber, transfer.getAmount(),
				tranType.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), tranCategory.name()),  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;
	}

	public ResponseEntity<?> doOfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer) {

		return coreBankingService.transfer(
											new TransferTransactionDTO(
												transfer.getOfficeDebitAccount(),  transfer.getCustomerCreditAccount(), transfer.getAmount(), 
												transfer.getTranType(), transfer.getTranCrncy(),  transfer.getTranNarration(), 
												transfer.getPaymentReference(), CategoryType.FUNDING.getValue()),  "WAYAOFFTOCUS");
	}

	public ApiResponse<?> OfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer,  boolean isMifos) {

		ResponseEntity<?> response = doOfficialUserTransfer(request, transfer);
		return new ApiResponse<>(response.getStatusCode().is2xxSuccessful(),
									 response.getStatusCode().value(), "PROCESSED", response.getBody());

	}

	@Override
	public ResponseEntity<?> OfficialUserTransferSystemSwitch(Map<String, String > mapp, String token, HttpServletRequest request, OfficeUserTransferDTO transfer) {

		MyData userToken = tokenService.getTokenUser(token);
		if (userToken == null) {
			throw new CustomException("INVALID TOKEN", HttpStatus.NOT_FOUND);
		}

		String fromAccountNumber = transfer.getOfficeDebitAccount();
		String toAccountNumber = transfer.getCustomerCreditAccount();
		if(fromAccountNumber.equals(toAccountNumber)) {
			throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.NOT_FOUND);
		}
		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("REVERSAL");

		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
				LocalDate.now(), transfer.getTranCrncy());
		if (!transRef.isEmpty()) {
			Optional<WalletTransaction> ret = transRef.stream()
					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
			if (ret.isPresent()) {
				throw new CustomException("Duplicate Payment Reference on the same Day", HttpStatus.NOT_FOUND);
			}
		}

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( fromAccountNumber,  toAccountNumber, transfer.getAmount(),
				tranType.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), tranCategory.name()),  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;
	}

	@Override
	public ResponseEntity<?> OfficialUserTransferMultiple(HttpServletRequest request, List<OfficeUserTransferDTO> transfer) {
		ApiResponse<?> response;
		ArrayList<Object> resObjects = new ArrayList<>();
		for(OfficeUserTransferDTO data: transfer){
			response = OfficialUserTransfer(request, data, false);

			resObjects.add(response.getData());
		}

		return new ResponseEntity<>(resObjects,HttpStatus.OK);
	}

	public ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer) {

		OfficeUserTransferDTO _transfer = new OfficeUserTransferDTO();
		BeanUtils.copyProperties(transfer, _transfer);
		_transfer.setOfficeDebitAccount(transfer.getDebitAccountNumber());
		_transfer.setCustomerCreditAccount(transfer.getBenefAccountNumber());

		return OfficialUserTransfer(request, _transfer, false);

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

	public ResponseEntity<?> AdminCommissionMoney(HttpServletRequest request, CommissionTransferDTO transfer) {

		ClientComTransferDTO dd = modelMapper.map(transfer, ClientComTransferDTO.class);
		adminCheck(dd);

		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( transfer.getDebitAccountNumber(),  transfer.getBenefAccountNumber(), transfer.getAmount(),
				tranType.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), tranCategory.name()),  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;
	}


	private void adminCheck(ClientComTransferDTO transfer){

		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = transfer.getBenefAccountNumber();


		WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getDebitAccountNumber());
		if (!acctComm.getProduct_code().equals("SB901")) {
			throw new CustomException("NOT COMMISSION WALLET", HttpStatus.NOT_FOUND);
		}


		WalletAccount acctDef = walletAccountRepository.findByAccountNo(transfer.getBenefAccountNumber());
		if (!acctDef.isWalletDefault()) {
			throw new CustomException("NOT DEFAULT WALLET", HttpStatus.NOT_FOUND);
		}

		if(fromAccountNumber.equals(toAccountNumber)) {
			throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<?> ClientCommissionMoney(HttpServletRequest request, ClientComTransferDTO transfer) {

		adminCheck(transfer);

		TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
		CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

		ResponseEntity<?> debitResponse = coreBankingService.transfer( new TransferTransactionDTO( transfer.getDebitAccountNumber(),  transfer.getBenefAccountNumber(), transfer.getAmount(),
				tranType.getValue(), "NGN",  transfer.getTranNarration(),
				transfer.getPaymentReference(), tranCategory.name()),  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;

	}

	@Override
	public ResponseEntity<?> sendMoneyCharge(HttpServletRequest request, WalletTransactionChargeDTO transfer) {
		TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
		transactionDTO.setTranType(transfer.getTranType());
		transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
		transactionDTO.setTranNarration(transfer.getTranNarration());
		transactionDTO.setTranCrncy(transfer.getTranCrncy());
		transactionDTO.setPaymentReference(transfer.getPaymentReference());
		transactionDTO.setDebitAccountNumber(transfer.getDebitAccountNumber());
		transactionDTO.setBenefAccountNumber(transfer.getBenefAccountNumber());
		transactionDTO.setAmount(transfer.getAmount());

		ResponseEntity<?> debitResponse = coreBankingService.transfer(transactionDTO, "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}

		return debitResponse;
	}

	@Override
	public ResponseEntity<?> sendMoneyCustomer(HttpServletRequest request, WalletTransactionDTO transfer) {

		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
				LocalDate.now(), transfer.getTranCrncy());
		if (!transRef.isEmpty()) {
			Optional<WalletTransaction> ret = transRef.stream()
					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
			if (ret.isPresent()) {
				return new ResponseEntity<>("Duplicate Payment Reference on the same Day", HttpStatus.BAD_REQUEST);
			}
		}

		Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumber());
		if (wallet.isEmpty()) {
			return new ResponseEntity<>("EMAIL OR PHONE NO DOES NOT EXIST", HttpStatus.BAD_REQUEST);
		}
		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if (defaultAcct.isEmpty()) {
			return new ResponseEntity<>("NO ACCOUNT NUMBER EXIST", HttpStatus.BAD_REQUEST);
		}
		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = defaultAcct.get().getAccountNo();
		if(fromAccountNumber.equals(toAccountNumber)) {
			return new ResponseEntity<>("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.BAD_REQUEST);
		}


		TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
		transactionDTO.setTranType(transfer.getTranType());
		transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
		transactionDTO.setTranNarration(transfer.getTranNarration());
		transactionDTO.setTranCrncy(transfer.getTranCrncy());
		transactionDTO.setPaymentReference(transfer.getPaymentReference());
		transactionDTO.setDebitAccountNumber(transfer.getDebitAccountNumber());
		transactionDTO.setBenefAccountNumber(toAccountNumber);
		transactionDTO.setAmount(transfer.getAmount());

		ResponseEntity<?> debitResponse = coreBankingService.transfer(transactionDTO, "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;

	}

	@Override
	public ApiResponse<?> AdminSendMoneyCustomer(HttpServletRequest request, AdminWalletTransactionDTO transfer) {
		Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumberOrId(transfer.getEmailOrPhoneNumberOrUserId());
		if (wallet.isEmpty()) {
				return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE OR ID DOES NOT EXIST",null);
		}
 
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(wallet.get());
		if (defaultAcct.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
		}

		OfficeUserTransferDTO _transfer = new OfficeUserTransferDTO();
		BeanUtils.copyProperties(transfer, _transfer);
		_transfer.setOfficeDebitAccount(transfer.getDebitAccountNumber());
		_transfer.setCustomerCreditAccount(defaultAcct.get().getAccountNo());

		return OfficialUserTransfer(request, _transfer, false);

	}

	private WalletAccount adminChecks(ClientWalletTransactionDTO transfer){
		List<WalletTransaction> transRef = walletTransactionRepository.findByReference(transfer.getPaymentReference(),
				LocalDate.now(), transfer.getTranCrncy());
		if (!transRef.isEmpty()) {
			Optional<WalletTransaction> ret = transRef.stream()
					.filter(code -> code.getPaymentReference().equals(transfer.getPaymentReference())).findAny();
			if (ret.isPresent()) {
				throw new CustomException("Duplicate Payment Reference on the same Day", HttpStatus.NOT_FOUND);
			}
		}

		Optional<WalletUser> wallet = walletUserRepository
				.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumberOrUserId());
		if (!wallet.isPresent()) {
			Long userId = Long.valueOf(transfer.getEmailOrPhoneNumberOrUserId());
			wallet = walletUserRepository.findUserId(userId);
			if (wallet.isEmpty()) {
				throw new CustomException("EMAIL OR PHONE OR ID DOES NOT EXIST", HttpStatus.NOT_FOUND);
			}
		}

		WalletUser user = wallet.get();
		Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
		if (defaultAcct.isEmpty()) {
			throw new CustomException("NO ACCOUNT NUMBER EXIST", HttpStatus.NOT_FOUND);
		}
		return defaultAcct.get();
	}


	@Override
	public ResponseEntity<?> ClientSendMoneyCustomer(HttpServletRequest request, ClientWalletTransactionDTO transfer) {

		// check for admin
		WalletAccount defaultAcct = adminChecks(transfer);


		String fromAccountNumber = transfer.getDebitAccountNumber();
		String toAccountNumber = defaultAcct.getAccountNo();
		if(fromAccountNumber.equals(toAccountNumber)) {
			throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.NOT_FOUND);
		}


		TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
		transactionDTO.setTranType(transfer.getTranType());
		transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
		transactionDTO.setTranNarration(transfer.getTranNarration());
		transactionDTO.setTranCrncy(transfer.getTranCrncy());
		transactionDTO.setPaymentReference(transfer.getPaymentReference());
		transactionDTO.setDebitAccountNumber(transfer.getDebitAccountNumber());
		transactionDTO.setBenefAccountNumber(toAccountNumber);
		transactionDTO.setAmount(transfer.getAmount());

		ResponseEntity<?> debitResponse = coreBankingService.transfer(transactionDTO, "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

		if(!debitResponse.getStatusCode().is2xxSuccessful()){
			return debitResponse;
		}
		return debitResponse;

	}


	private void removeLien(WalletAccount accountDebit, BigDecimal amount){

		log.info("############### AccountDebit ::: #################3  " + accountDebit);
		// get user current Lien
		AccountLienDTO accountLienDTO = new AccountLienDTO();
		accountLienDTO.setCustomerAccountNo(accountDebit.getAccountNo());
		accountLienDTO.setLien(false);
		accountLienDTO.setLienReason("no longer needed");
		//double lienAmount = accountDebit.getLien_amt() - amount.doubleValue();

		accountLienDTO.setLienAmount(amount);

		ResponseEntity<?> responseEntity;
		try{
			responseEntity = userAccountService.AccountAccessLien(accountLienDTO);
		}catch (CustomException ex){
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

		log.info("############### RESPONSE FROM REMOVING LIEN  :: ###############"  + responseEntity);

	}


	private void postToMifos(String token,WalletAccount accountCredit, WalletAccount accountDebit, BigDecimal amount, String tranNarration, String tranId,
							 TransactionTypeEnum tranType){

		if(accountCredit.getNubanAccountNo() !=null){
			log.info("this is not  null");
		}else{
			log.info("this is null");
		}

		if(accountDebit.getNubanAccountNo() !=null){
			log.info(" 22this is not  null");
		}else{
			log.info("this is null");
		}
		MifosTransfer mifosTransfer = new MifosTransfer();
		mifosTransfer.setAmount(amount);
 		mifosTransfer.setDestinationAccountNumber(accountCredit.getNubanAccountNo() !=null ? accountCredit.getNubanAccountNo(): accountCredit.getAccountNo());

		mifosTransfer.setDestinationAccountType(accountCredit.getAccountType() !=null ? accountCredit.getAccountType() : "SAVINGS");
		mifosTransfer.setDestinationCurrency(accountCredit.getAcct_crncy_code());
		mifosTransfer.setNarration(tranNarration);
		mifosTransfer.setRequestId(tranId+"345493");
		mifosTransfer.setSourceAccountNumber(accountDebit.getNubanAccountNo() !=null ? accountDebit.getNubanAccountNo() : accountDebit.getAccountNo());

		mifosTransfer.setSourceAccountType("SAVINGS");
		mifosTransfer.setSourceCurrency(accountDebit.getAcct_crncy_code());
		mifosTransfer.setTransactionType(TransactionTypeEnum.TRANSFER.getValue());
		ExternalCBAResponse response;
		System.out.println(" here" + mifosTransfer);
		try{
			log.info("## token  ####### :: " + token);
			log.info("## BEFOR MIFOS REQUEST ####### :: " + mifosTransfer);
			response = mifosWalletProxy.transferMoney(mifosTransfer);
			 log.info("### RESPONSE FROM MIFOS MifosWalletProxy  ###### :: " + response);
		}catch(CustomException ex){
			System.out.println("ERROR posting to MIFOS :::: " + ex.getMessage());
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
		System.out.println("RESPONSE" +  response.getResponseDescription()); 
		
	}
 
	private UserPricing getUserProduct(WalletAccount accountDebit, String eventId){
		WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
		Long xUserId = xUser.getUserId();
		System.out.println("user pricing prod is" + xUserId + eventId);
		// get user charge by eventId and userID
		return userPricingRepository.findDetailsByCode(xUserId,eventId).orElse(null);
	}


	public String createEventTransactionSettlement(String eventId, String merchantAcctNo, String wayaCommAcctNo, String tranCrncy,
												   BigDecimal amount, BigDecimal merchantAmount,BigDecimal wayaCommAmount,
										 TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
										 CategoryType tranCategory) {

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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()) {
				return "DJGO|NO EVENT ACCOUNT";
			}

			WalletAccount accountDebit = accountDebitTeller.get();

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
				BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(Objects.requireNonNull(tokenData).getTransactionLimit());
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
			String tranId;
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

//	private Map<String, WalletAccount> checkAccountForDebitOrCredit(WalletEventCharges charge, WalletAccount accountDebit, WalletAccount accountCredit, Optional<WalletAccount> accountDebitTeller, String creditAcctNo){
//	Map<String, WalletAccount> map = new HashMap<>();
//
//		if (charge.isChargeWaya()) {
//			accountDebit = accountDebitTeller.get();
//			accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
//		} else if(charge.isChargeCustomer()){
//			accountCredit = accountDebitTeller.get();
//			accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
//		}else{
//			accountDebit = accountDebitTeller.get();
//			accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
//		}
//		map.put("accountDebit", accountDebit);
//		map.put("accountCredit", accountCredit);
//		return map;
//	}


	private WalletAccount getAccountByEventId(String eventId, String creditAcctNo){
		System.out.println("eventId " + eventId);
		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
		if (eventInfo.isEmpty()) {
			throw new CustomException("DJGO|Event Code Does Not Exist", HttpStatus.NOT_FOUND);
		}
		WalletEventCharges charge = eventInfo.get();
		boolean validate2 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
		if (!validate2) {
			throw new CustomException("DJGO|Event Validation Failed", HttpStatus.NOT_FOUND);
		}
		WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);
		if (eventAcct == null) {
			throw new CustomException("DJGO|CUSTOMER ACCOUNT DOES NOT EXIST", HttpStatus.NOT_FOUND);
		}
		// Does account exist
		Optional<WalletAccount> accountDebitTeller = walletAccountRepository
				.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
		if (accountDebitTeller.isEmpty()) {
			throw new CustomException("DJGO|NO EVENT ACCOUNT", HttpStatus.NOT_FOUND);
		}
		return accountDebitTeller.get();
	}

	public String createEventTransaction(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
			CategoryType tranCategory, boolean isMifos) {

		String tranDate = getTransactionDate();

		if(isMifos){
			System.out.println("Send request to Mifos; if successful, post to wallet");
		}
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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()) {
				return "DJGO|NO EVENT ACCOUNT";
			}


			String outBoundFeeEvent = "OB_TRANS_FEE_INCOME_ACCT";

			WalletAccount accountDebit = null;
			WalletAccount accountCredit = null;
			BigDecimal tranAmCharges = null;
			//Map<String, WalletAccount> map = checkAccountForDebitOrCredit(charge, accountDebit, accountCredit, accountDebitTeller, creditAcctNo);
			if (charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
				log.info("accountCredit accountCredit accountCredit :: " + accountCredit);
				System.out.println("accountDebit accountDebit accountDebit :: " + accountDebit);
			} else if(charge.isChargeCustomer()){
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
				log.info("accountCredit accountCredit accountCredit :: " + accountCredit);
				log.info("accountDebit accountDebit accountDebit :: " + accountDebit);
				UserPricing userPricingOptional;
				// get user charge by eventId and userID
				if(eventId.equals("WEMA_INT_DISBURS_ACCT") || eventId.equals("ZE_INT_DISBURS_ACCOUNT") || eventId.equals("MIFOSNIPINTRAS")){
					eventId = "BANKPMT";
					userPricingOptional = getUserProduct(accountDebit, eventId);

				}else{
					userPricingOptional = getUserProduct(accountDebit, eventId);
				}

				if(eventId.equals("MIFOSNIPINTRAS")){
					outBoundFeeEvent = "OUTTRANSFEE";
				}
				if(eventId.equals("ZE_INT_DISBURS_ACCOUNT")){

				}
				if(eventId.equals("WEMA_INT_DISBURS_ACCT")){
					eventId = "OB_TRANS_FEE_INCOME_ACCT";
				}


				System.out.println("userPricingOptional " + userPricingOptional);

				tranAmCharges = getChargesAmount(userPricingOptional, amount);

				System.out.println("charge amount " + tranAmCharges);


			}else{
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}
			// Check for account security


			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************

			String check = securityCheck(eventId,creditAcctNo,tranCrncy,amount,tokenData, accountDebit,accountCredit);
			log.info("security check ::" + check);

			// **********************************************
			// Account Transaction Locks
			// *********************************************

			// **********************************************
			String tranId;
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

			// REMOVE LEIN
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

			String contentType;
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
			log.info("ABOUT TO DO DEDUCT TRANSACTION FEE");

			WalletAccount finalAccountCredit = accountCredit; //getAccountByEventId(outBoundFeeEvent, accountCredit.getAccountNo());

			WalletAccount finalAccountDebit = accountDebit;
			BigDecimal finalTranAmCharges = tranAmCharges;
			doDeductTransactionCharges(tokenData ,senderName, receiverName, paymentRef, "DEDUCT TRANSACTION FEE", tranCrncy, tranCategory, tranType, finalTranAmCharges, finalAccountDebit, finalAccountCredit);

			System.out.println("eventId eventId:: " + eventId);
			System.out.println("outBoundFeeEvent 3333:: " + outBoundFeeEvent);
			log.info("ABOUT TO DEBIT FROM NIP INTRASIT DISBURS ACCOUT TO CREDIT  OUTBOUND TRANSFER FEE INCOME ACCOUNT CREDITED WITH 10");
			WalletAccount finalAccountCredit2 = getAccountByEventId(outBoundFeeEvent, accountCredit.getAccountNo());

			WalletAccount finalAccountDebit2 = walletAccountRepository.findByAccountNo(finalAccountCredit.getAccountNo()); // Debit  NIP Intransit disbursement account debited with 10
			BigDecimal finalTranAmCharges2 = tranAmCharges;
			doDeductTransactionCharges(tokenData ,senderName, receiverName, paymentRef, "OUTBOUND TRANSFER FEE", tranCrncy, tranCategory, tranType, finalTranAmCharges2, finalAccountDebit2, finalAccountCredit2);

			// NIP VAT DEDUCTIONS CUSTOMES ACCOUNT TO ==> WAYA MFB (Mifos) NIP Intransit disbursement account credited with N10 VAT Fee
			doDeductTransactionCharges(tokenData ,senderName, receiverName, paymentRef, "NIP VAT FEES", tranCrncy, tranCategory, tranType, finalTranAmCharges2, finalAccountDebit, finalAccountCredit);



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


	public String securityCheck(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount, MyData tokenData, WalletAccount accountDebit, WalletAccount accountCredit) throws Exception {

		log.info("START TRANSACTION");

		boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
		if (!validate) {
			return "DJGO|Currency Code Validation Failed";
		}
		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
		if (eventInfo.isEmpty()) {
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
		if (accountDebitTeller.isEmpty()) {
			return "DJGO|NO EVENT ACCOUNT";
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
			BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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

		return "";
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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit;
			WalletAccount accountCredit;
			BigDecimal tranAmCharges;
			if (charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			} else if(charge.isChargeCustomer()){
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}else {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
			}
				UserPricing userPricingOptional = getUserProduct(accountDebit, eventId);

				tranAmCharges = getChargesAmount(userPricingOptional, amount);

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
				BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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

				BigDecimal userLim = new BigDecimal(Objects.requireNonNull(tokenData).getTransactionLimit());
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
			String tranId;
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

			String contentType;
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

			if(!eventId.equals("WAYAOFFTOCUS")){
				WalletAccount finalAccountCredit = accountCredit;
				WalletAccount finalAccountDebit = accountDebit;
				CompletableFuture.runAsync(() -> doDeductTransactionCharges(tokenData, senderName, receiverName, paymentRef, "Wallet Transfer", tranCrncy, tranCategory, TransactionTypeEnum.CHARGES, tranAmCharges, finalAccountDebit, finalAccountCredit));
			}

			return tranId;
		} catch (Exception e) {
			e.printStackTrace();
			return ("DJGO|" + e.getMessage());
		}

	}




	public String createEventTransactionDebitUserCreditWayaAccount(String eventId, String debitAcctNo, String tranCrncy, BigDecimal amount,
										 TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
										 CategoryType tranCategory, MyData userToken) {
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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit;
			WalletAccount accountCredit;

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
				BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(Objects.requireNonNull(tokenData).getTransactionLimit());
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
			String tranId;
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
											   CategoryType tranCategory) {
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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()) {
				return "DJGO|NO EVENT ACCOUNT";
			}

			//For credit Event only
			WalletEventCharges eventCredit = walletEventRepository.findByEventId(creditEventId).orElseThrow();
			if (eventInfo.isEmpty()) {
				return "DJGO|Event Code Does Not Exist";
			}

			WalletEventCharges chargeCredit = eventCredit;
			boolean validate3 = paramValidation.validateDefaultCode(charge.getPlaceholder(), "Batch Account");
			if (!validate3) {
				return "DJGO|Event Validation Failed";
			}

			// Does account exist
			Optional<WalletAccount> accountCreditTeller = walletAccountRepository
					.findByUserPlaceholder(chargeCredit.getPlaceholder(), chargeCredit.getCrncyCode(), "0000");
			if (accountCreditTeller.isEmpty()) {
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
				BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(Objects.requireNonNull(tokenData).getTransactionLimit());
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
			String tranId;
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

	public String createEventRedeem(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request, String senderName) {
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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()
			) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit;
			WalletAccount accountCredit;
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
				BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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
					if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
					if (new BigDecimal(oustbal).compareTo(amount) == -1) {
						return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
					}
				}

				BigDecimal userLim = new BigDecimal(Objects.requireNonNull(tokenData).getTransactionLimit());
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
			String tranId;
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
			CategoryType tranCategory) {
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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit;
			WalletAccount accountCredit;
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
				BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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
			String tranId;
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

		log.info("categoryType : " + categoryType);
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
								 HttpServletRequest request, CategoryType tranCategory, String senderName, String receiverName) {
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
		if (eventInfo.isEmpty()) {
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
		if (accountDebitTeller.isEmpty()) {
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
			BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
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
				if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
				if (new BigDecimal(oustbal).compareTo(amount) == -1) {
					return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
				}
			}

			BigDecimal userLim = new BigDecimal(Objects.requireNonNull(tokenData).getTransactionLimit());
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
		String tranId;
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
			HttpServletRequest request, CategoryType tranCategory, String senderName, String receiverName) {
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
			if (eventInfo.isEmpty()) {
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
			if (accountDebitTeller.isEmpty()) {
				return "DJGO|NO EVENT ACCOUNT";
			}
			WalletAccount accountDebit;
			WalletAccount accountCredit;
			BigDecimal tranAmCharges;
			if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
				accountDebit = accountDebitTeller.get();
				accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
				tranAmCharges = charge.getTranAmt();
			} else {
				log.info( creditAcctNo + " Charge here " + charge.getTranAmt());
				accountCredit = accountDebitTeller.get();
				accountDebit = walletAccountRepository.findByAccountNo(creditAcctNo);
				//tranAmCharges = charge.getTranAmt();

				// get user charge by eventId and userID
				if(eventId.equals("WEMA_INT_DISBURS_ACCT") || eventId.equals("ZE_INT_DISBURS_ACCOUNT") || eventId.equals("MIFOSNIPINTRAS")){
					eventId = "BANKPMT";
				}

				UserPricing userPricingOptional = getUserProduct(accountDebit, eventId);
				System.out.println("userPricingOptional " + userPricingOptional);

				tranAmCharges = getChargesAmount(userPricingOptional, amount);

				System.out.println("charge amount " + tranAmCharges);
			}
			if (accountDebit == null || accountCredit == null) {
				return "DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST";
			}


			// Check for account security

			// Check for Amount Limit

			// Token Fetch
			MyData tokenData = tokenService.getUserInformation();
			String email = tokenData != null ? tokenData.getEmail() : "";
			String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
			// **********************************************

			// AUth Security check
			// **********************************************
			String validateDebitAccount = validateDebitAccount(accountCredit, creditAcctNo, tranAmCharges, accountDebit, tokenData, amount);
			log.info("Security check for Debit Account :: " + validateDebitAccount);


			// **********************************************
			// Account Transaction Locks
			// *********************************************

			// **********************************************
			String tranId;
			if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
				tranId = tempwallet.SystemGenerateTranId();
			} else {
				tranId = tempwallet.GenerateTranId();
			}
			if (tranId.equals("")) {
				return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
			}

			// Remove Lien
			removeLien(accountDebit,amount);

			String tranNarrate = "WALLET-" + tranNarration + " TO:" + bk;
			WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			n = n + 1;
			WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
					tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
					n, tranCategory,senderName,receiverName);

			//  ################ TERSEER ########################


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

			removeLien(accountDebit,amount);
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

	private String validateDebitAccount(WalletAccount accountCredit, String creditAcctNo, BigDecimal tranAmCharges, WalletAccount accountDebit,MyData tokenData, BigDecimal amount) throws Exception {

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

		if (!accountDebit.getAcct_ownership().equals("O")) {

			Long userId = Long.parseLong(keyDebit[0]);
			WalletUser user = walletUserRepository.findByUserId(userId);
			BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
			if (AmtVal.compareTo(amount) == -1) {
				return "DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED";
			}

			double totalAmount = accountDebit.getClr_bal_amt() + tranAmCharges.doubleValue();

			if (new BigDecimal(totalAmount).compareTo(amount) == -1) {
				return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
			}

			if (new BigDecimal(totalAmount).compareTo(BigDecimal.ONE) != 1) {
				return "DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE";
			}
		}

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
		return "";
	}


	private BigDecimal computePercentage(BigDecimal amount, BigDecimal percentageValue){
		BigDecimal per = BigDecimal.valueOf(percentageValue.doubleValue() / 100);
		log.info("per :: " + per);
		return BigDecimal.valueOf(per.doubleValue() * amount.doubleValue());
	}

	public BigDecimal computeTransFee(String accountDebit, BigDecimal amount,  String eventId){
		WalletAccount account = walletAccountRepository.findByAccountNo(accountDebit);
		UserPricing userPricingOptional = getUserProduct(account, eventId);
		return getChargesAmount(userPricingOptional, amount);
	}

	public BigDecimal getChargesAmount(UserPricing userPricingOptional, BigDecimal amount){
		BigDecimal percentage = null;
		log.info("Inside getChargesAmount userPricingOptional" + userPricingOptional);
		log.info("Inside getChargesAmount amount" + amount);
		if(userPricingOptional.getStatus().equals(ProductPriceStatus.GENERAL)){
			log.info(" #### GENERAL  PRICING #####  ::");
		percentage = computePercentage(amount, userPricingOptional.getGeneralAmount());
				// apply discount if applicable
			log.info(" #### GENERAL  PRICe  #####  :: " + percentage);
		}else if (userPricingOptional.getStatus().equals(ProductPriceStatus.CUSTOM)){
			if(userPricingOptional.getPriceType().equals(PriceCategory.FIXED)){
				percentage = userPricingOptional.getGeneralAmount();
				log.info(" #### CUSTOM  PRICe  #####  :: " + percentage);
			}else {
				// apply discount if applicable
				percentage = computePercentage(amount, userPricingOptional.getCustomAmount());
			}
		}
		if(amount.doubleValue() <= 0){ return percentage; }

		if(percentage.doubleValue() > userPricingOptional.getCapPrice().doubleValue()){
			percentage = userPricingOptional.getCapPrice();
		}

		log.info("CAP PRICE ::" + userPricingOptional.getCapPrice());
		log.info("TRANSACTION FEE ::" + percentage);
		return percentage;

	}

	private BigDecimal computeCap(){

		return new BigDecimal(Long.min(0,9));
	}

	private void doDeductTransactionCharges(MyData tokenData, String senderName, String receiverName, String paymentRef, String bk, String tranCrncy, CategoryType tranCategory, TransactionTypeEnum tranType, BigDecimal chargesAmount, WalletAccount accountDebit, WalletAccount accountCredit){
		try{
			// POST TO MIFOS
				log.info("bk =" + bk);

		String email = tokenData != null ? tokenData.getEmail() : "";
		String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

		String tranId;
		if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
			tranId = tempwallet.SystemGenerateTranId();
		} else {
			tranId = tempwallet.GenerateTranId();
		}
		if (tranId.equals("")) {
			throw new CustomException("DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN", HttpStatus.NOT_FOUND);
		}

		int n = 1;
		String tranNarrate = "WALLET-" + "transaction charges" + bk;
		WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), chargesAmount, tranType,
				tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
				n, tranCategory,senderName,receiverName);
			System.out.println("tranDebit  transaction charges ==: : " + tranDebit);

		n = n + 1;
		WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), chargesAmount, tranType,
				tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
				n, tranCategory,senderName,receiverName);

			System.out.println("tranCredit transaction charges ==: : " + tranCredit);

		//  ################ TERSEER ########################

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

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ApiResponse<?> getStatement(String accountNumber) {
		WalletAccountStatement statement;
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
		if (account == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
		}
		List<WalletTransaction> transaction = walletTransactionRepository.findByAcctNumEquals(accountNumber);
		if (transaction == null) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
		}
		statement = new WalletAccountStatement(BigDecimal.valueOf(account.getClr_bal_amt()), transaction);
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


		WalletAccount walletAccount = getAcount(Long.valueOf(eventPay.getSenderId()));
		Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventPay.getEventId());
		if (eventInfo.isEmpty()){
			return new ResponseEntity<>(new ErrorResponse("EVENT DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
		}
		WalletEventCharges charge = eventInfo.get();
		WalletAccount accountDebitTeller = walletAccountRepository
				.findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), walletAccount.getSol_id()).orElse(null);

		OfficeUserTransferDTO officeTransferDTO = new OfficeUserTransferDTO();
		officeTransferDTO.setAmount(eventPay.getAmount());
		officeTransferDTO.setCustomerCreditAccount(walletAccount.getAccountNo());
		officeTransferDTO.setOfficeDebitAccount(Objects.requireNonNull(accountDebitTeller).getAccountNo());
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
		res = OfficialUserTransfer(request, officeTransferDTO, false);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.EXPECTATION_FAILED);
		}
		CompletableFuture.runAsync(() -> updatePaymentRequestStatus(eventPay.getPaymentRequestReference()));

		return new ResponseEntity<>(res, HttpStatus.OK);
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
		case ProviderType.MIFOS:
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
		ArrayList<String> account = new ArrayList<>();

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
			String tranId;
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

			if(tokenData !=null){
				CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, fullName,
						xUser.getEmailAddress(), message2, tokenData.getId(), trans.getTranAmount().toString(), trans.getTranId(),
						trans.getTranDate().toString(), trans.getTranNarrate()));
				CompletableFuture.runAsync(() -> customNotification.pushSMS(token, fullName, xUser.getMobileNo(),
						message2, tokenData.getId()));
				CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
						message2, tokenData.getId(), TRANSACTION_HAS_OCCURRED));
			}

		}

		log.info("END TRANSACTION");

	}

	public ResponseEntity<?> TranReversePaymentRevised(HttpServletRequest request, ReverseTransactionDTO reverseDto)
			{
		Provider provider = switchWalletService.getActiveProvider();
		if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
		}
		log.info("WALLET PROVIDER: " + provider.getName());
		switch (provider.getName()) {
			case ProviderType.MIFOS:
				return ReversePaymentRevised(request, reverseDto);
			case ProviderType.TEMPORAL:
				return ReversePaymentRevised(request, reverseDto);
			default:
				return ReversePaymentRevised(request, reverseDto);
		}
	}

	public ResponseEntity<?> ReversePaymentRevised(HttpServletRequest request, ReverseTransactionDTO reverseDto)
			{

		LocalDate reverseDate = reverseDto.getTranDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		BigDecimal debitAmount = new BigDecimal("0"), creditAmount = new BigDecimal("0");
		ArrayList<String> account = new ArrayList<>();

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
			String tranId;
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
	public ResponseEntity<?> createBulkTransaction(HttpServletRequest request, BulkTransactionCreationDTO bulkList) {
		ResponseEntity<?> responseEntity;
		try {
			if (bulkList == null || bulkList.getUsersList().isEmpty())
				throw new CustomException("Bulk List cannot be null or Empty", HttpStatus.EXPECTATION_FAILED);

			String fromAccountNumber = bulkList.getOfficeAccountNo();
			TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(bulkList.getTranType());

			 responseEntity = createBulkTransaction(fromAccountNumber, bulkList.getTranCrncy(), bulkList.getUsersList(),
					tranType, bulkList.getTranNarration(), bulkList.getPaymentReference());

		} catch (Exception e) {
			log.error("Error in Creating Bulk Account:: {}", e.getMessage());
			throw new CustomException(e.getMessage(), BAD_REQUEST);
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<?> createBulkExcelTrans(HttpServletRequest request, MultipartFile file) {
		String message;
		BulkTransactionExcelDTO bulkLimt;
		ResponseEntity<?> debitResponse;
		if (ExcelHelper.hasExcelFormat(file)) {
			try {
				bulkLimt = ExcelHelper.excelToBulkTransactionPojo(file.getInputStream(), file.getOriginalFilename());
				debitResponse = createExcelTransaction(bulkLimt.getUsersList());

				if(!debitResponse.getStatusCode().is2xxSuccessful()){
					return debitResponse;
				}

			} catch (Exception e) {
				throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
		message = "Please upload an excel file!";

		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
	}

	public ResponseEntity<?> createBulkTransaction(String debitAcctNo, String tranCrncy, Set<UserTransactionDTO> usersList,
			TransactionTypeEnum tranType, String tranNarration, String paymentRef){
		String reference;
		reference = tempwallet.TransactionGenerate();
		ResponseEntity<?> debitResponse = null;
		try {

			for (UserTransactionDTO mUser : usersList) {

				debitResponse = coreBankingService.transfer(new TransferTransactionDTO(debitAcctNo, mUser.getCustomerAccountNo(), mUser.getAmount(),
						TransactionTypeEnum.TRANSFER.getValue(), "NGN", "Builk Account Creation",
						reference, CategoryType.TRANSFER.getValue()), "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return debitResponse;
	}

	public ResponseEntity<?> createExcelTransaction(Set<ExcelTransactionCreationDTO> transList) {

		ResponseEntity<?> debitResponse = null;
		try {

			for (ExcelTransactionCreationDTO mUser : transList) {
				log.info("Process Transaction: {}", mUser.toString());

				 debitResponse = coreBankingService.transfer( new TransferTransactionDTO( mUser.getOfficeAccountNo(),  mUser.getCustomerAccountNo(), mUser.getAmount(),
						TransactionTypeEnum.TRANSFER.getValue(), "NGN",  mUser.getTranNarration(),
						mUser.getPaymentReference(), CategoryType.TRANSFER.getValue()),  "INTERNAL_TRANS_INTRANSIT_DISBURS_ACCOUNT");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return debitResponse;
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
		return tempwallet.GetTransactionType2(acctNo,fromdate,todate);
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
		ArrayList<String> account = new ArrayList<>();

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
			String tranId;
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

		WalletEventCharges event = walletEventRepository.findByEventId(eventId)
				.orElseThrow(() -> new NoSuchElementException("EVENT ID NOT AVAILABLE FOR EventId FIRST:" + eventId));

		boolean validate2 = paramValidation.validateDefaultCode(event.getPlaceholder(), "Batch Account");
		if (!validate2) {
			throw new CustomException("Event Placeholder Validation Failed", HttpStatus.BAD_REQUEST);
		}

		return walletAccountRepository
				.findByUserPlaceholder(event.getPlaceholder(), event.getCrncyCode(), "0000")
				.orElseThrow(() -> new NoSuchElementException("EVENT ID NOT AVAILABLE FOR EventId :" + eventId));
	}

	public String formatMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy, String narration,
			String tokenId) {
		log.info("amt" + amount + "|" + tranId + "|" + tranCrncy + "|" + narration + "|" + tranDate );
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

		log.info("sender" + sender);
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

		log.info("sender" + sender);
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



	@Override
	public ResponseEntity<?> WayaPaymentRequestUsertoUser(HttpServletRequest request, WayaPaymentRequest transfer) {
		try{

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
					log.info("Inside here == " + transfer.getPaymentRequest());
					PaymentRequest mPay = transfer.getPaymentRequest();
					WalletAccount debitAcct;
					WalletAccount creditAcct;
					// if this request comes from an official transfer.getPaymentRequest().isWayaOfficial() ||
					if (!StringUtils.isNumeric(mPayRequest.getSenderId())) {
						creditAcct = getOfficialAccount(mPayRequest.getSenderId());
						debitAcct = getAcount(Long.valueOf(mPay.getReceiverId()));
						OfficeUserTransferDTO transferDTO = new OfficeUserTransferDTO(creditAcct.getAccountNo(),
								debitAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN", mPayRequest.getReason(),
								mPayRequest.getReference());
						ApiResponse<?> res = OfficialUserTransfer( request,transferDTO, false);

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
