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
import com.wayapaychat.temporalwallet.pojo.*;
import com.wayapaychat.temporalwallet.proxy.MifosWalletProxy;
import com.wayapaychat.temporalwallet.proxy.SMUProxy;
import com.wayapaychat.temporalwallet.repository.*;
import com.wayapaychat.temporalwallet.service.*;
import com.wayapaychat.temporalwallet.util.*;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
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

import com.waya.security.auth.pojo.UserIdentityData;
import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.notification.CustomNotification;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;
import com.wayapaychat.temporalwallet.response.Analysis;
import com.wayapaychat.temporalwallet.response.CategoryAnalysis;
import com.wayapaychat.temporalwallet.response.OverallAnalysis;
import com.wayapaychat.temporalwallet.response.TransactionAnalysis;
import com.wayapaychat.temporalwallet.response.TransactionsResponse;

import feign.FeignException;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
public class TransAccountServiceImpl implements TransAccountService {

    @Value("${waya.wallet.NIPGL}")
    String nipgl;
    @Value("${waya.wallet.PAYSTACKGL}")
    String paystackgl;

    public static final String mobileDownloadLink = "https://wayabank.ng/";

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
    private final AuthProxy authProxy;
    private final UserAccountService userAccountService;
    private final UserPricingRepository userPricingRepository;
    private final MifosWalletProxy mifosWalletProxy;
    private final CoreBankingService coreBankingService;
    private final ModelMapper modelMapper;
    private final WalletTransAccountRepository walletTransAccountRepo;
    private final SMUProxy smuProxy;

    @Autowired
    WalletTransactionRepository walletTransRepo;

    @Autowired
    public TransAccountServiceImpl(WalletUserRepository walletUserRepository,
            WalletAccountRepository walletAccountRepository,
            WalletAcountVirtualRepository walletAcountVirtualRepository, ReqIPUtils reqIPUtils,
            TemporalWalletDAO tempwallet, WalletTransactionRepository walletTransactionRepository,
            ParamDefaultValidation paramValidation, WalletEventRepository walletEventRepository,
            SwitchWalletService switchWalletService, TokenImpl tokenService, ExternalServiceProxyImpl userDataService,
            WalletNonWayaPaymentRepository walletNonWayaPaymentRepo, CustomNotification customNotification,
            WalletQRCodePaymentRepository walletQRCodePaymentRepo,
            WalletPaymentRequestRepository walletPaymentRequestRepo,
            AuthProxy authProxy, UserAccountService userAccountService, UserPricingRepository userPricingRepository,
            MifosWalletProxy mifosWalletProxy, CoreBankingService coreBankingService, ModelMapper modelMapper,
            WalletTransAccountRepository walletTransAccountRepo, SMUProxy smuProxy) {
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
        this.authProxy = authProxy;
        this.userAccountService = userAccountService;
        this.userPricingRepository = userPricingRepository;
        this.coreBankingService = coreBankingService;
        this.mifosWalletProxy = mifosWalletProxy;
        this.modelMapper = modelMapper;
        this.walletTransAccountRepo = walletTransAccountRepo;
        this.smuProxy = smuProxy;
    }

    @Override
    public ResponseEntity<?> adminTransferForUser(HttpServletRequest request, String command, AdminUserTransferDTO transfer) {
        log.info("Received admin transfer request for user: {}", transfer);

        String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("ADMINTIL");
        TransferTransactionDTO transferTransactionDTO;
        try {
            if (command.toUpperCase().equals("CREDIT")) {
                transferTransactionDTO = new TransferTransactionDTO(nonWayaDisbursementAccount, transfer.getCustomerAccountNumber(), transfer.getAmount(),
                        TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(), transfer.getPaymentReference(),
                        CategoryType.TRANSFER.getValue(), transfer.getReceiverName(), transfer.getSenderName());
            } else {
                transferTransactionDTO = new TransferTransactionDTO(transfer.getCustomerAccountNumber(), nonWayaDisbursementAccount, transfer.getAmount(),
                        TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(), transfer.getPaymentReference(),
                        CategoryType.TRANSFER.getValue(), transfer.getReceiverName(), transfer.getSenderName());
            }
            return coreBankingService.processTransaction(transferTransactionDTO, "WAYATRAN", request);
        } catch (Exception ex) {
            log.error("Error processing admin transfer for user: {}", ex.getMessage(), ex);
            throw new CustomException("Error processing admin transfer for user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> cashTransferByAdmin(HttpServletRequest request, String command, WalletAdminTransferDTO transfer) {
        log.info("Received cash transfer by admin request: {}", transfer);

        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token, request);
        if (userToken == null) {
            log.error("Invalid token provided");
            throw new CustomException("INVALID", HttpStatus.NOT_FOUND);
        }

        Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumber());
        if (wallet.isEmpty()) {
            log.error("User with email or phone number {} does not exist", transfer.getEmailOrPhoneNumber());
            throw new CustomException("EMAIL OR PHONE NO DOES NOT EXIST", HttpStatus.NOT_FOUND);
        }
        WalletUser user = wallet.get();
        Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
        if (defaultAcct.isEmpty()) {
            log.error("No account number exists for user {}", user);
            throw new CustomException("NO ACCOUNT NUMBER EXIST", HttpStatus.NOT_FOUND);
        }
        String toAccountNumber = defaultAcct.get().getAccountNo();

        String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("ADMINTIL");
        TransferTransactionDTO transferTransactionDTO;
        try {
            if (command.toUpperCase().equals("CREDIT")) {
                transferTransactionDTO = new TransferTransactionDTO(nonWayaDisbursementAccount, toAccountNumber,
                        transfer.getAmount(), TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                        transfer.getSenderName());
            } else {
                transferTransactionDTO = new TransferTransactionDTO(toAccountNumber, nonWayaDisbursementAccount,
                        transfer.getAmount(), TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                        transfer.getSenderName());
            }
            return coreBankingService.processTransaction(transferTransactionDTO, "WAYATRAN", request);
        } catch (Exception ex) {
            log.error("Error processing cash transfer by admin: {}", ex.getMessage(), ex);
            throw new CustomException("Error processing cash transfer by admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public ResponseEntity<?> EventTransferPayment(HttpServletRequest request, EventPaymentDTO transfer,
                                                  boolean isMifos) {

        log.info("Transaction Request Creation: {}", transfer.toString());

        TransferTransactionDTO transferTransactionDTO = null;
        String managementAccount;

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(transfer.getEventId());
        if (eventInfo.isEmpty()) {
            log.error("Error processing transaction: Event information not found for Event ID {}", transfer.getEventId());
            return new ResponseEntity<>(new ErrorResponse("ERROR PROCESSING TRANSACTION"), HttpStatus.BAD_REQUEST);
        }

        if(isVirtualAccount(transfer.getCustomerAccountNumber()) && TransactionChannel.NIP_FUNDING.name().equals(transfer.getEventId())) {

            //If it is a virtual account get the default account for the business and credit it.
            WalletAccount walletAccount = walletAccountRepository.findByNubanAccountNo(transfer.getCustomerAccountNumber());

            if(walletAccount !=null){
                transfer.setCustomerAccountNumber(getDefaultAccount(walletAccount.getUser()));
            }
        }
        if (eventInfo.get().isChargeWaya()) {
            log.info("NIP_FUNDING TRANS {}", transfer.getCustomerAccountNumber());
            managementAccount = coreBankingService
                    .getEventAccountNumber(EventCharge.COLLECTION_.name().concat(transfer.getEventId()));
            transferTransactionDTO = new TransferTransactionDTO(managementAccount, transfer.getCustomerAccountNumber(),
                    transfer.getAmount(),
                    TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                    transfer.getPaymentReference(), transfer.getTransactionCategory(), transfer.getReceiverName(),
                    transfer.getSenderName());
        } else {
            managementAccount = coreBankingService
                    .getEventAccountNumber(EventCharge.DISBURS_.name().concat(transfer.getEventId()));
            transferTransactionDTO = new TransferTransactionDTO(transfer.getCustomerAccountNumber(), managementAccount,
                    transfer.getAmount(),
                    TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                    transfer.getPaymentReference(), transfer.getTransactionCategory(), transfer.getReceiverName(),
                    transfer.getSenderName());

        }

        ResponseEntity<?> response = coreBankingService.processTransaction(transferTransactionDTO, transfer.getEventId(), request);
        log.info("Transaction Response: {}", response);
        return response;
    }


    private boolean isVirtualAccount(String accountNo){
        WalletAccount account = walletAccountRepository.findByNubanAccountNo(accountNo);
        System.out.println("isVirtualAccount?" + account);
        if(account !=null){
            if(account.isVirtualAccount()){
                return true;
            }
        }
        return false;
    }

    private String getDefaultAccount(WalletUser walletUser){
        String defaultAccount = "";
        Optional<WalletAccount> walletAccount = walletAccountRepository.findByDefaultAccount(walletUser);
        if(walletAccount.isPresent()) {
            defaultAccount = walletAccount.get().getAccountNo();
        }
        return defaultAccount;
    }
    @Override
    public ResponseEntity<?> TemporalWalletToOfficialWalletMutiple(HttpServletRequest request,
                                                                   List<TemporalToOfficialWalletDTO> transfer) {
        log.info("method TemporalWalletToOfficialWalletMutiple request: {}", transfer);

        ArrayList<Object> list = new ArrayList<>();
        ResponseEntity<?> resp;
        for (TemporalToOfficialWalletDTO data : transfer) {
            resp = TemporalWalletToOfficialWallet(request, data);
            list.add(resp);
        }
        return new ResponseEntity<>(list, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> EventOfficePayment(HttpServletRequest request, EventOfficePaymentDTO transfer) {

        log.info("Transaction Request Creation: {}", transfer);

        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token,request);
        if (userToken == null) {
            log.error("Invalid Token: Token is null or invalid");
            return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
        }

        String toAccountNumber = transfer.getCreditEventId();
        String fromAccountNumber = transfer.getDebitEventId();
        if (fromAccountNumber.equals(toAccountNumber)) {
            log.error("Debit event ID is the same as Credit event ID: {}", fromAccountNumber);
            return new ResponseEntity<>(new ErrorResponse("DEBIT EVENT CAN'T BE THE SAME WITH CREDIT EVENT"),
                    HttpStatus.BAD_REQUEST);
        }
        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("TRANSFER");
        CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

        TransferTransactionDTO transferTransactionDTO = new TransferTransactionDTO(fromAccountNumber, toAccountNumber,
                transfer.getAmount(),
                tranType.getValue(), "NGN", transfer.getTranNarration(),
                transfer.getPaymentReference(), tranCategory.getValue(), transfer.getReceiverName(),
                transfer.getSenderName());

        ResponseEntity<?> response = coreBankingService.processTransaction(transferTransactionDTO, "WAYATRAN", request);
        log.info("Transaction Response: {}", response);
        return response;
    }

    private String getTransactionDate() {
        Date tDate = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String transactionDate = dateFormat.format(tDate);
        log.info("Transaction Date: {}", transactionDate);
        return transactionDate;
    }

    public ResponseEntity<?> TemporalWalletToOfficialWallet(HttpServletRequest request,
                                                            TemporalToOfficialWalletDTO transfer) {

        // Ability to transfer money from the temporal wallet back to waya official
        // account in single or in mass with excel upload
        log.info("Transaction Request Creation: {}", transfer);

        String toAccountNumber = transfer.getOfficialAccountNumber();
        String fromAccountNumber = transfer.getCustomerAccountNumber();
        if (fromAccountNumber.equals(toAccountNumber)) {
            log.error("Debit event ID is the same as Credit event ID: {}", fromAccountNumber);
            return new ResponseEntity<>(new ErrorResponse("DEBIT EVENT CAN'T BE THE SAME WITH CREDIT EVENT"),
                    HttpStatus.BAD_REQUEST);
        }

        CategoryType tranCategory = CategoryType.valueOf(transfer.getTransactionCategory());

        TransferTransactionDTO transferTransactionDTO = new TransferTransactionDTO(fromAccountNumber, toAccountNumber,
                transfer.getAmount(),
                TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                transfer.getPaymentReference(), tranCategory.getValue(), transfer.getReceiverName(),
                transfer.getSenderName());

        ResponseEntity<?> response = coreBankingService.processTransaction(transferTransactionDTO, "WAYATRAN", request);
        log.info("Transaction Response: {}", response);
        return response;
    }

    public ResponseEntity<?> EventNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer) {
        ArrayList<Object> list = new ArrayList<>();
        ResponseEntity<?> resp;
        for (NonWayaPaymentDTO data : transfer) {
            resp = EventNonPayment(request, data);
            list.add(resp.getBody());
        }
        resp = new ResponseEntity<>(list, HttpStatus.CREATED);
        return resp;
    }

    public ResponseEntity<?> EventNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
        log.info("Transaction Request Creation: {}", transfer);

        String toAccountNumber = transfer.getCustomerDebitAccountNo();
        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf("CARD");
        // debit customer || credit Official Account

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId("DISBURSE_NONWAYAPT");
        if (eventInfo.isEmpty()) {
            log.error("Error processing transaction: Event information not found for Event ID {}", "DISBURSE_NONWAYAPT");
            throw new CustomException("ERROR PROCESSING TRANSACTION", HttpStatus.NOT_FOUND);
        }

        String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("NONWAYAPT");
        TransferTransactionDTO transferTransactionDTO;
        if (eventInfo.get().isChargeWaya()) {
            transferTransactionDTO = new TransferTransactionDTO(nonWayaDisbursementAccount, toAccountNumber,
                    transfer.getAmount(),
                    tranType.name(), "NGN", transfer.getTranNarration(),
                    transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                    transfer.getSenderName());
        } else {
            transferTransactionDTO = new TransferTransactionDTO(toAccountNumber, nonWayaDisbursementAccount,
                    transfer.getAmount(),
                    TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                    transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                    transfer.getSenderName());
        }

        ResponseEntity<?> response = coreBankingService.processTransaction(transferTransactionDTO, "WAYATRAN", request);
        log.info("Transaction Response: {}", response);
        return response;
    }

    public ResponseEntity<?> EventNonRedeem(HttpServletRequest request, NonWayaPaymentDTO transfer) {
        log.info("Transaction Request Creation: {}", transfer);
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token,request);
        if (userToken == null) {
            log.error("Invalid Token: Token is null or invalid");
            throw new CustomException("INVAILED TOKEN", HttpStatus.NOT_FOUND);
        }
        String toAccountNumber = transfer.getCustomerDebitAccountNo();

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId("DISBURSE_NONWAYAPT");
        if (eventInfo.isEmpty()) {
            log.error("Error processing transaction: Event information not found for Event ID {}", "DISBURSE_NONWAYAPT");
            throw new CustomException("ERROR PROCESSING TRANSACTION", HttpStatus.NOT_FOUND);
        }

        String noneWayaAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");

        ResponseEntity<?> debitResponse = coreBankingService
                .processTransaction(new TransferTransactionDTO(noneWayaAccount, toAccountNumber, transfer.getAmount(),
                        TransactionTypeEnum.CARD.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                        transfer.getSenderName()), "NONWAYAPT", request);

        String tranId = transfer.getPaymentReference();
        String tranDate = getTransactionDate();

        String message = formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
                transfer.getTranNarration());
        CompletableFuture.runAsync(
                () -> customNotification.pushTranEMAIL(NON_WAYA_TRANSACTION_ALERT, token, transfer.getFullName(),
                        transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
                        tranId, tranDate, transfer.getTranNarration(), "", "", ""));
        CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
                transfer.getEmailOrPhoneNo(), message, userToken.getId()));
        log.info("Transaction Response: {}", debitResponse);
        return debitResponse;
    }

    @Override
    public ResponseEntity<?> EventNonRedeemMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer) {
        log.info("method EventNonRedeemMultiple request ---->> {}", transfer);
        ArrayList<Object> list = new ArrayList<>();
        ResponseEntity<?> res = null;
        for (NonWayaPaymentDTO data : transfer) {
            res = EventNonRedeem(request, data);
            list.add(res.getBody());
            res = new ResponseEntity<>(list, HttpStatus.OK);
            log.info("Transaction Response: {}", list.toString());
        }
        return res;
    }

    @Override
    public ResponseEntity<?> TransferNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer) {
        log.info("method TransferNonPaymentMultiple request ---->> {}", transfer);
        ResponseEntity<?> resp = null;
        ArrayList<Object> rpp = new ArrayList<>();
        for (NonWayaPaymentDTO data : transfer) {
            resp = NonPayment(request, data);
            rpp.add(resp.getBody());
        }
        log.info("Transaction Response: {}", rpp.toString());
        return resp;

    }

    @Override
    public ResponseEntity<?> TransferNonPaymentMultipleUpload(HttpServletRequest request, MultipartFile file) {
        log.info("TransferNonPaymentMultipleUpload request ");

        Map<String, ArrayList<ResponseHelper>> responseEntity = null;
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                responseEntity = MultipleUpload2(request,
                        ExcelHelper.excelToNoneWayaTransferAdmin(file.getInputStream(), file.getOriginalFilename()));

            } catch (Exception e) {
                log.error("Failed to parse Excel data: {}", e.getMessage());
                throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(responseEntity, HttpStatus.OK);
    }

    private Map<String, ArrayList<ResponseHelper>> MultipleUpload2(HttpServletRequest request,
                                                                   @Valid NonWayaTransferExcelDTO transferExcelDTO) {
        log.info("MultipleUpload2 request ---->> {}", transferExcelDTO);

        ArrayList<ResponseHelper> respList = new ArrayList<>();
        Map<String, ArrayList<ResponseHelper>> map = new HashMap<>();

        if (transferExcelDTO == null || transferExcelDTO.getTransfer().isEmpty()) {
            log.error("Transfer List is null or empty");
            throw new CustomException("Transfer List cannot be null or Empty", BAD_REQUEST);
        }

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
    public ResponseEntity<?> TransferNonPaymentSingleWayaOfficial(HttpServletRequest request,
                                                                  NonWayaPaymentMultipleOfficialDTO transfer) {
        log.info("TransferNonPaymentSingleWayaOfficial request ---->> {}", transfer);
        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            log.error("No Provider switched");
            return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
        }
        log.info("Wallet Provider: {}", provider.getName());

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
    public ResponseEntity<?> TransferNonPaymentMultipleWayaOfficial(HttpServletRequest request,
                                                                    List<NonWayaPaymentMultipleOfficialDTO> transfer) {
        log.info("Received request to transfer multiple payments");
        log.info("Transfer details: {}", transfer);
        ResponseEntity<?> resp;
        ArrayList<Object> rpp = new ArrayList<>();
        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            log.error("No active provider found.");
            return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
        }
        log.info("WALLET PROVIDER: {}", provider.getName());

        switch (provider.getName()) {
            case ProviderType.MIFOS:
                for (NonWayaPaymentMultipleOfficialDTO data : transfer) {
                    log.info("Processing transfer: {}", data.toString());
                    resp = NonPaymentFromOfficialAccount(request, data);
                    rpp.add(resp.getBody());
                    log.info("Transfer response: {}", resp);
                }
                return new ResponseEntity<>(rpp, HttpStatus.OK);
            case ProviderType.TEMPORAL:
                for (NonWayaPaymentMultipleOfficialDTO data : transfer) {
                    log.info("Processing transfer: {}", data.toString());
                    resp = NonPaymentFromOfficialAccount(request, data);
                    rpp.add(resp.getBody());
                    log.info("Transfer response: {}", resp);
                }
                return new ResponseEntity<>(rpp, HttpStatus.OK);
            default:
                for (NonWayaPaymentMultipleOfficialDTO data : transfer) {
                    log.info("Processing transfer: {}", data.toString());
                    resp = NonPaymentFromOfficialAccount(request, data);
                    rpp.add(resp.getBody());
                    log.info("Transfer response: {}", resp);
                }
                return new ResponseEntity<>(rpp, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<?> TransferNonPaymentWayaOfficialExcel(HttpServletRequest request, MultipartFile file) {
        log.info("Received request to transfer payments from Excel file");
        Map<String, ArrayList<ResponseHelper>> responseEntity = null;
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                log.info("Parsing Excel file: {}", file.getOriginalFilename());
                responseEntity = MultipleUpload(request,
                        ExcelHelper.excelToNoneWayaTransferPojo(file.getInputStream(), file.getOriginalFilename()));
                log.info("Excel parsing completed successfully");
            } catch (Exception e) {
                log.error("Failed to parse Excel data: {}", e.getMessage());
                throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(responseEntity, HttpStatus.OK);
    }


    public ByteArrayInputStream createExcelSheet(String isOnBhalfNoneWaya) {
        log.info("Creating Excel sheet for: {}", isOnBhalfNoneWaya);
        switch (isOnBhalfNoneWaya) {
            case "PRIVATE_USER_HEADERS":
                return ExcelHelper.createExcelSheet(ExcelHelper.PRIVATE_USER_HEADERS);
            case "PRIVATE_TRANSFER_HEADERS":
                return ExcelHelper.createExcelSheet(ExcelHelper.PRIVATE_TRANSFER_HEADERS);
            default:
                return ExcelHelper.createExcelSheet(ExcelHelper.TRANSFER_HEADERS);
        }
    }

    private Map<String, ArrayList<ResponseHelper>> MultipleUpload(HttpServletRequest request,
                                                                  @Valid BulkNonWayaTransferExcelDTO transferExcelDTO) {
        log.info("Processing multiple uploads");
        ResponseEntity<?> resp;
        ArrayList<ResponseHelper> respList = new ArrayList<>();
        Map<String, ArrayList<ResponseHelper>> map = new HashMap<>();

        if (transferExcelDTO == null || transferExcelDTO.getTransfer().isEmpty()) {
            log.error("Transfer List is empty or null");
            throw new CustomException("Transfer List cannot be null or Empty", BAD_REQUEST);
        }

        for (NonWayaPaymentMultipleOfficialDTO mTransfer : transferExcelDTO.getTransfer()) {
            log.info("Processing transfer: {}", mTransfer.toString());
            resp = NonPaymentFromOfficialAccount(request, mTransfer);
            respList.add((ResponseHelper) resp.getBody());
            log.info("Transfer response: {}", resp);
        }
        map.put("Response", respList);
        return map;
    }


    @Override
    public ResponseEntity<?> transferToNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
        log.info("Method transferToNonPayment request ---->> {}", transfer);
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);

        Optional<WalletAccount> walletAccount = walletAccountRepository.findByAccount(transfer.getCustomerDebitAccountNo());
        if(!walletAccount.isPresent()) {
            log.error("CUSTOMER DEBIT ACCOUNT NOT FOUND, CAN NOT PROCESS TRANSFER");
            return new ResponseEntity<>(new ErrorResponse("CUSTOMER DEBIT ACCOUNT NOT FOUND, CAN NOT PROCESS TRANSFER"), HttpStatus.NOT_FOUND);
        }
        transfer.setReceiverName(transfer.getFullName());
        String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");
        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(new TransferTransactionDTO(
                transfer.getCustomerDebitAccountNo(), nonWayaDisbursementAccount, transfer.getAmount(),
                TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                transfer.getSenderName()), "NONWAYAPT", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            return debitResponse;
        }

        String transactionToken = tempwallet.generateToken();
        WalletNonWayaPayment nonpay = new WalletNonWayaPayment(transactionToken, transfer.getEmailOrPhoneNo(),
                transfer.getPaymentReference(), transfer.getCustomerDebitAccountNo(), transfer.getAmount(),
                transfer.getTranNarration(),
                transfer.getTranCrncy(), transfer.getPaymentReference(), userToken.getId().toString(),
                userToken.getEmail(), PaymentStatus.PENDING, transfer.getFullName());
        nonpay.setMerchantId(userToken.getId());
        nonpay.setSenderName(transfer.getSenderName());
        nonpay.setSenderProfileId(transfer.getSenderProfileId());
        walletNonWayaPaymentRepo.save(nonpay);

        String tranDate = getCurrentDate();
        String tranId = transfer.getPaymentReference();
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        String nonWayaSms = formatMessage(transfer.getAmount(), transactionToken, tranDate, transfer.getTranNarration(),transfer.getReceiverName(),transfer.getSenderName());
        String nonWayaEmail = formatEmailMessage(transfer.getAmount(), transactionToken,transfer.getReceiverName(),transfer.getSenderName());

        if (!StringUtils.isNumeric(transfer.getEmailOrPhoneNo())) {
            log.info("SEND EMAIL NOTIFICATION TO WAYA AND NOW WAYA: " + transfer.getEmailOrPhoneNo());
            String wayaUserSms = formatMoneWayaMessage(transfer.getAmount(), transfer.getPaymentReference(), tranDate,
                    transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken,transfer.getReceiverName(),transfer.getEmailOrPhoneNo(),false);

            CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaEmail, userToken.getId(), transfer.getAmount().toString(),
                    tranId, tranDate, transfer.getTranNarration()));

            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
                    userToken.getPhoneNumber(), wayaUserSms, userToken.getId()));

            CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaEmail, userToken.getId(), NON_WAYA_PAYMENT_REQUEST));
        } else {
            log.info("SEND PHONE(SMS) NOTIFICATION TO NON-WAYA AND WAYA USER: " + transfer.getEmailOrPhoneNo());
            String wayaUserSms = formatMoneWayaMessage(transfer.getAmount(), transfer.getPaymentReference(), tranDate,
                    transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken,transfer.getReceiverName(),transfer.getEmailOrPhoneNo(),true);


            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaSms, userToken.getId()));

            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
                    userToken.getPhoneNumber(), wayaUserSms, userToken.getId()));

            CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaEmail, userToken.getId(), NON_WAYA_PAYMENT_REQUEST));
        }

        return debitResponse;

    }

public ResponseEntity<?> NonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer) {
    log.info("Processing non-payment: {}", transfer);
    // Debit Customer and credit Waya
    return transferToNonPayment(request, transfer);
}


    private String getCurrentDate() {
        Date tDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(tDate);
    }

    public ResponseEntity<?> NonPaymentFromOfficialAccount(HttpServletRequest request,
            NonWayaPaymentMultipleOfficialDTO transfer) {

        log.info("Transaction Request Creation: {}", transfer);
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token,request);
        if (userToken == null) {
            log.error("Unauthorized access detected");
            return new ResponseEntity<>(new ErrorResponse("UNAUTHORIZED, PLEASE LOGIN"), HttpStatus.UNAUTHORIZED);
        }

        String transactionToken = tempwallet.generateToken();
        String debitAccountNumber = transfer.getOfficialAccountNumber();

        transfer.setReceiverName(transfer.getFullName());
        String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");
        ResponseEntity<?> debitResponse = coreBankingService
                .processTransaction(
                        new TransferTransactionDTO(debitAccountNumber, nonWayaDisbursementAccount, transfer.getAmount(),
                                TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                                transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(),
                                transfer.getReceiverName(), transfer.getSenderName()),
                        "NONWAYAPT", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            return debitResponse;
        }

        WalletNonWayaPayment nonpay = new WalletNonWayaPayment(transactionToken, transfer.getEmailOrPhoneNo(),
                transfer.getPaymentReference(), transfer.getOfficialAccountNumber(), transfer.getAmount(),
                transfer.getTranNarration(),
                transfer.getTranCrncy(), transfer.getPaymentReference(), userToken.getId().toString(),
                userToken.getEmail(), PaymentStatus.PENDING, transfer.getFullName());
        nonpay.setMerchantId(userToken.getId());
        nonpay.setSenderName(transfer.getSenderName());
        nonpay.setSenderProfileId(transfer.getSenderProfileId());
        walletNonWayaPaymentRepo.save(nonpay);

        String tranDate = getCurrentDate();
        String tranId = transfer.getPaymentReference();
        String nonWayaSms = formatMessage(transfer.getAmount(), transactionToken, tranDate, transfer.getTranNarration(),transfer.getReceiverName(),transfer.getSenderName());
        String nonWayaEmail = formatEmailMessage(transfer.getAmount(), transactionToken,transfer.getReceiverName(),transfer.getSenderName());

        if (!StringUtils.isNumeric(transfer.getEmailOrPhoneNo())) {
            log.info("SEND EMAIL NOTIFICATION TO WAYA AND NOW WAYA: " + transfer.getEmailOrPhoneNo());
            String wayaUserSms = formatMoneWayaMessage(transfer.getAmount(), transfer.getPaymentReference(), tranDate,
                    transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken,transfer.getReceiverName(),transfer.getEmailOrPhoneNo(),false);

            CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaEmail, userToken.getId(), transfer.getAmount().toString(),
                    tranId, tranDate, transfer.getTranNarration()));

            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
                    userToken.getPhoneNumber(), wayaUserSms, userToken.getId()));

            CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaEmail, userToken.getId(), NON_WAYA_PAYMENT_REQUEST));
        } else {
            log.info("SEND PHONE(SMS) NOTIFICATION TO NON-WAYA AND WAYA USER: " + transfer.getEmailOrPhoneNo());
            String wayaUserSms = formatMoneWayaMessage(transfer.getAmount(), transfer.getPaymentReference(), tranDate,
                    transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken,transfer.getReceiverName(),transfer.getEmailOrPhoneNo(),true);

            CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
                    userToken.getEmail(), nonWayaEmail, userToken.getId(), transfer.getAmount().toString(), tranId,
                    tranDate, transfer.getTranNarration()));

            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaSms, userToken.getId()));

            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
                    userToken.getPhoneNumber(), wayaUserSms, userToken.getId()));

            CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
                    transfer.getEmailOrPhoneNo(), nonWayaEmail, userToken.getId(), NON_WAYA_PAYMENT_REQUEST));
        }

//        String message = formatMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
//                transfer.getTranNarration(), transactionToken);
//        String noneWaya = formatMoneWayaMessage(transfer.getAmount(), transfer.getPaymentReference(), tranDate,
//                transfer.getTranCrncy(), transfer.getTranNarration(), transactionToken,transfer.getReceiverName(),);
//
//        if (!StringUtils.isNumeric(transfer.getEmailOrPhoneNo())) {
//            log.info("EMAIL: " + transfer.getEmailOrPhoneNo());
//            CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
//                    transfer.getEmailOrPhoneNo(), message, userToken.getId(), transfer.getAmount().toString(),
//                    tranId, tranDate, transfer.getTranNarration()));
//
//            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
//                    userToken.getPhoneNumber(), noneWaya, userToken.getId()));
//
//            CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
//                    transfer.getEmailOrPhoneNo(), message, userToken.getId(), NON_WAYA_PAYMENT_REQUEST));
//        } else {
//            log.info("PHONE: " + transfer.getEmailOrPhoneNo());
//            CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, transfer.getFullName(),
//                    userToken.getEmail(), message, userToken.getId(), transfer.getAmount().toString(), tranId,
//                    tranDate, transfer.getTranNarration()));
//
//            CompletableFuture.runAsync(() -> customNotification.pushSMS(token, transfer.getFullName(),
//                    transfer.getEmailOrPhoneNo(), noneWaya, userToken.getId()));
//
//            CompletableFuture.runAsync(() -> customNotification.pushInApp(token, transfer.getFullName(),
//                    transfer.getEmailOrPhoneNo(), message, userToken.getId(), NON_WAYA_PAYMENT_REQUEST));
//        }

        return debitResponse;
    }

        public ResponseEntity<?> getListOfNonWayaTransfers(HttpServletRequest request, String userId, int page, int size) {
            log.info("Retrieving list of non-Waya transfers for user: {}", userId);

            try {
            String token = request.getHeader(SecurityConstants.HEADER_STRING);
            MyData userToken = tokenService.getTokenUser(token,request);
            if (userToken == null) {
                return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
            }

            Pageable paging = PageRequest.of(page, size);
            Page<WalletNonWayaPayment> walletNonWayaPaymentPage = walletNonWayaPaymentRepo.findAllByCreatedBy(userId,
                    paging);
            List<WalletNonWayaPayment> walletNonWayaPaymentList = walletNonWayaPaymentPage.getContent();
            Map<String, Object> response = new HashMap<>();

            response.put("nonWayaList", walletNonWayaPaymentList);
            response.put("currentPage", walletNonWayaPaymentPage.getNumber());
            response.put("totalItems", walletNonWayaPaymentPage.getTotalElements());
            response.put("totalPages", walletNonWayaPaymentPage.getTotalPages());

            log.info("Data Retrieved");
            return new ResponseEntity<>(new SuccessResponse("Data Retrieved", response),
                    HttpStatus.CREATED);

        } catch (Exception ex) {
            log.error("Error occurred - GET WALLET TRANSACTION :" + ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> listOfNonWayaTransfers(HttpServletRequest request, int page, int size) {
        log.info("list Of Non Waya Transfers ");
        try {
            String token = request.getHeader(SecurityConstants.HEADER_STRING);
            MyData userToken = tokenService.getTokenUser(token,request);
            if (userToken == null) {
                log.info("INVALID TOKEN");
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

            log.info("Data Retrieved");
            return new ResponseEntity<>(new SuccessResponse("Data Retrieved", response),
                    HttpStatus.CREATED);

        } catch (Exception ex) {
            log.error("GET WALLET TRANSACTION {} " + ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> TransferNonRedeem(HttpServletRequest request, NonWayaBenefDTO transfer) {
        log.info("Transaction Request Creation: {}", transfer);

        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token,request);
        if (userToken == null) {
            log.error("UNAUTHORIZED, PLEASE LOGIN");
            throw new CustomException("UNAUTHORIZED, PLEASE LOGIN", HttpStatus.BAD_REQUEST);
        }
        WalletUser user = walletUserRepository.findByUserIdAndProfileId(Long.valueOf(transfer.getBeneficiaryUserId()),transfer.getBeneficiaryProfileId());
        if (user == null) {
            log.error("BENEFICIARY INFO NOT FOUND");
            throw new CustomException("BENEFICIARY INFO NOT FOUND", HttpStatus.BAD_REQUEST);
        }
        String fullName = user.getCust_name();
        String emailAddress = user.getEmailAddress();
        String phoneNo = user.getMobileNo();

        Optional<WalletAccount> walletAccount = walletAccountRepository.findByDefaultAccount(user);
        if(!walletAccount.isPresent()) {
            log.error("BENEFICIARY ACCOUNT NOT FOUND");
            throw new CustomException("BENEFICIARY ACCOUNT NOT FOUND", HttpStatus.BAD_REQUEST);
        }
        String beneAccount = walletAccount.get().getAccountNo();

        String transactionToken = tempwallet.generateToken();
        // Debit NONWAYAPT can credit Customer account
        String noneWayaAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");
        if(noneWayaAccount == null)
            throw new CustomException("DISBURSEMENT ACCOUNT NOT FOUND", HttpStatus.BAD_REQUEST);

        ResponseEntity<?> debitResponse = coreBankingService
                .processTransaction(new TransferTransactionDTO(noneWayaAccount, beneAccount, transfer.getAmount(),
                        TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                        transfer.getSenderName()), "NONWAYAPT", request);

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
                () -> customNotification.pushInApp(token, fullName, userToken.getId().toString(), message,
                        userToken.getId(), TRANSACTION_HAS_OCCURRED));
        log.info("Debit response ----> {}", debitResponse);

        return debitResponse;
    }

    public ResponseEntity<?> TransferNonReject(HttpServletRequest request, String beneAccount, BigDecimal amount,
                                               String tranCrncy, String tranNarration, String paymentReference, String receiverName, String senderName) {

        log.info("Processing non-reject transfer: amount={}, beneAccount={}, tranCrncy={}, tranNarration={}, paymentReference={}, receiverName={}, senderName={}",
                amount, beneAccount, tranCrncy, tranNarration, paymentReference, receiverName, senderName);

        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token, request);
        if (userToken == null) {
            log.error("Invalid token detected");
            return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
        }

        WalletAccount account = walletAccountRepository.findByAccountNo(beneAccount);
        if (account == null) {
            log.error("Invalid account detected");
            return new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT"), HttpStatus.BAD_REQUEST);
        }
        String fullName = account.getUser().getCust_name();
        String emailAddress = account.getUser().getEmailAddress();
        String phoneNo = account.getUser().getMobileNo();

        String transactionToken = tempwallet.generateToken();

        String noneWayaAccount = coreBankingService.getEventAccountNumber("DISBURSE_NONWAYAPT");

        ResponseEntity<?> debitResponse = coreBankingService
                .processTransaction(new TransferTransactionDTO(noneWayaAccount, beneAccount, amount,
                                TransactionTypeEnum.TRANSFER.getValue(), "NGN", "TransferNonReject",
                                paymentReference, CategoryType.TRANSFER.getValue(), receiverName, senderName), "NONWAYAPT",
                        request);

        String tranDate = getTransactionDate();
        String tranId = paymentReference;

        String message = formatMessage(amount, tranId, tranDate, tranCrncy, tranNarration, transactionToken);
        CompletableFuture.runAsync(() -> customNotification.pushNonWayaEMAIL(token, fullName, emailAddress,
                message, userToken.getId(), amount.toString(), tranId, tranDate, tranNarration));
        CompletableFuture.runAsync(
                () -> customNotification.pushSMS(token, fullName, phoneNo, message, userToken.getId()));
        CompletableFuture.runAsync(
                () -> customNotification.pushInApp(token, fullName, userToken.getId().toString(), message,
                        userToken.getId(), TRANSACTION_HAS_OCCURRED));

        log.info("Non-reject transfer processed successfully");
        return debitResponse;
    }


    @Override
    public ResponseEntity<?> NonWayaPaymentRedeem(HttpServletRequest request, NonWayaRedeemDTO transfer) {
        try {
            log.info("Processing NonWayaPaymentRedeem request: transfer={}", transfer);

            // To fetch the token used
            String token = request.getHeader(SecurityConstants.HEADER_STRING);
            MyData userToken = tokenService.getTokenUser(token, request);
            if (userToken == null) {
                log.error("Unauthorized access detected");
                return new ResponseEntity<>(new ErrorResponse("UNAUTHORIZED, PLEASE LOGIN"), HttpStatus.BAD_REQUEST);
            }

            // check if Transaction is still valid
            Optional<WalletNonWayaPayment> nonWayaPayment = walletNonWayaPaymentRepo.findByToken(transfer.getToken());
            if (!nonWayaPayment.isPresent()) {
                log.error("Invalid token detected");
                return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN, CAN NOT REDEEMED THIS CREDIT"), HttpStatus.BAD_REQUEST);
            }

            if (nonWayaPayment.get().getStatus() != null && nonWayaPayment.get().getStatus().equals(PaymentStatus.REJECT)) {
                log.error("Token is no longer valid");
                return new ResponseEntity<>(new ErrorResponse("TOKEN IS NO LONGER VALID, CAN NOT REDEEMED THIS CREDIT"), HttpStatus.BAD_REQUEST);
            } else if (nonWayaPayment.get().getStatus() != null && nonWayaPayment.get().getStatus().equals(PaymentStatus.PAYOUT)) {
                log.error("Transaction has been paid out");
                return new ResponseEntity<>(new ErrorResponse("TRANSACTION HAS BEEN PAID OUT"), HttpStatus.BAD_REQUEST);
            } else if (nonWayaPayment.get().getStatus() != null && nonWayaPayment.get().getStatus().equals(PaymentStatus.EXPIRED)) {
                log.error("Token for this transaction has expired");
                return new ResponseEntity<>(new ErrorResponse("TOKEN FOR THIS TRANSACTION HAS EXPIRED"), HttpStatus.BAD_REQUEST);
            }

            String userId = String.valueOf(nonWayaPayment.get().getMerchantId());
            if (transfer.getBeneficiaryUserId().equals(userId)) {
                log.error("Same merchant can't process and redeem non-waya transfer request");
                return new ResponseEntity<>(new ErrorResponse("SAME MERCHANT CAN'T PROCESS AND REDEEM NON-WAYA TRANSFER REQUEST, PLEASE CONTACT SUPPORT!"), HttpStatus.FORBIDDEN);
            }

            if (transfer.getStatusAction() == null) {
                log.error("Kindly specify your redeem status action");
                return new ResponseEntity<>(new ErrorResponse("KINDLY SPECIFY YOUR REDEEM STATUS ACTION"), HttpStatus.BAD_REQUEST);
            }

            if (transfer.getStatusAction().equals(NonWayaPaymentStatusAction.REJECTED)) {
                String messageStatus = "TRANSACTION REJECT.";
                nonWayaPayment.get().setStatus(PaymentStatus.REJECT);
                nonWayaPayment.get().setUpdatedAt(LocalDateTime.now());
                nonWayaPayment.get().setRedeemedEmail(userToken.getEmail());
                nonWayaPayment.get().setRedeemedBy(userToken.getId().toString());
                nonWayaPayment.get().setRedeemedAt(LocalDateTime.now().plusHours(1));
                String tranNarrate = "REJECT " + nonWayaPayment.get().getTranNarrate();
                String payRef = "REJECT" + nonWayaPayment.get().getPaymentReference();

                ResponseEntity<?> rejectResponse = TransferNonReject(request, nonWayaPayment.get().getDebitAccountNo(), nonWayaPayment.get().getTranAmount(),
                        nonWayaPayment.get().getCrncyCode(), tranNarrate, payRef, transfer.getReceiverName(), nonWayaPayment.get().getSenderName());
                log.info("TransferNonReject response: {}", rejectResponse);

                String message = formatMessengerRejection(nonWayaPayment.get().getTranAmount(), payRef, nonWayaPayment.get().getFullName(), nonWayaPayment.get().getEmailOrPhone());
                CompletableFuture.runAsync(() -> customNotification.pushInApp(token, nonWayaPayment.get().getFullName(),
                        nonWayaPayment.get().getEmailOrPhone(), message, userToken.getId(), TRANSACTION_REJECTED));

                if (!rejectResponse.getStatusCode().is2xxSuccessful()) {
                    log.error("Non-reject transfer failed");
                    return rejectResponse;
                }

                walletNonWayaPaymentRepo.save(nonWayaPayment.get());

                log.info("Non-reject transfer successful");
                return new ResponseEntity<>(new SuccessResponse(messageStatus, null), HttpStatus.CREATED);
            }

            String messageStatus;
            if (nonWayaPayment.get().getStatus() != null && nonWayaPayment.get().getStatus().equals(PaymentStatus.PENDING)) {
                messageStatus = "TRANSACTION RESERVED: Kindly note that a confirm PIN has been sent to " + nonWayaPayment.get().getEmailOrPhone();
                nonWayaPayment.get().setStatus(PaymentStatus.RESERVED);
                String pinToken = tempwallet.generateOtpPin();
                nonWayaPayment.get().setConfirmPIN(pinToken);
                nonWayaPayment.get().setUpdatedAt(LocalDateTime.now());
                nonWayaPayment.get().setBeneficiaryUserId(transfer.getBeneficiaryUserId());
                nonWayaPayment.get().setBeneficiaryProfileId(transfer.getBeneficiaryProfileId());
                nonWayaPayment.get().setReceiverName(transfer.getReceiverName());
                walletNonWayaPaymentRepo.save(nonWayaPayment.get());
                String message = formatMessagePIN(pinToken);

                if (nonWayaPayment.get().getEmailOrPhone().contains("@")) {
                    CompletableFuture.runAsync(() -> customNotification.pushEMAIL(REDEEM_NON_WAYA_TRANSACTION_ALERT,
                            token, nonWayaPayment.get().getFullName(), nonWayaPayment.get().getEmailOrPhone(), message, userToken.getId()));
                } else {
                    CompletableFuture.runAsync(() -> customNotification.pushSMS(token, nonWayaPayment.get().getFullName(),
                            nonWayaPayment.get().getEmailOrPhone(), message, userToken.getId()));
                }
                CompletableFuture.runAsync(() -> customNotification.pushInApp(token, nonWayaPayment.get().getFullName(),
                        nonWayaPayment.get().getEmailOrPhone(), message, userToken.getId(), TRANSACTION_HAS_OCCURRED));

                log.info("Transaction reserved successfully");
                return new ResponseEntity<>(new SuccessResponse(messageStatus, null), HttpStatus.CREATED);
            } else if (nonWayaPayment.get().getStatus() != null && nonWayaPayment.get().getStatus().equals(PaymentStatus.RESERVED)) {
                messageStatus = "TRANSACTION PAYOUT.";

                String tranNarrate = "REDEEM " + nonWayaPayment.get().getTranNarrate();
                String payRef = "REDEEM" + nonWayaPayment.get().getPaymentReference();
                NonWayaBenefDTO merchant = nonWayaBenefDTO(nonWayaPayment.get(), payRef, tranNarrate);
                ResponseEntity<?> redeemedResponse = TransferNonRedeem(request, merchant);
                log.info("TransferNonRedeem response: {}", redeemedResponse);
                if (!redeemedResponse.getStatusCode().is2xxSuccessful()) {
                    log.error("Can't redeem this transaction: {}", nonWayaPayment.get().getPaymentReference());
                    nonWayaPayment.get().setStatus(PaymentStatus.PENDING);
                    nonWayaPayment.get().setUpdatedAt(LocalDateTime.now().plusHours(1));
                    walletNonWayaPaymentRepo.save(nonWayaPayment.get());
                    return redeemedResponse;
                }
                nonWayaPayment.get().setStatus(PaymentStatus.PAYOUT);
                nonWayaPayment.get().setUpdatedAt(LocalDateTime.now().plusHours(1));
                if (transfer.getRedeemerEmail() != null) {
                    nonWayaPayment.get().setRedeemedEmail(transfer.getRedeemerEmail());
                } else {
                    nonWayaPayment.get().setRedeemedEmail(userToken.getEmail());
                }
                if (transfer.getRedeemerId() != null) {
                    nonWayaPayment.get().setRedeemedBy(transfer.getRedeemerId());
                } else {
                    nonWayaPayment.get().setRedeemedBy(userToken.getId().toString());
                }
                nonWayaPayment.get().setRedeemedAt(LocalDateTime.now().plusHours(1));
                walletNonWayaPaymentRepo.save(nonWayaPayment.get());
                String message = formatMessageRedeem(nonWayaPayment.get().getTranAmount(), payRef);
                CompletableFuture.runAsync(() -> customNotification.pushInApp(token, nonWayaPayment.get().getFullName(),
                        nonWayaPayment.get().getEmailOrPhone(), message, userToken.getId(), TRANSACTION_PAYOUT));

                log.info("Transaction payout successful");
                return new ResponseEntity<>(new SuccessResponse(messageStatus, null), HttpStatus.CREATED);
            } else {
                log.error("Unable to payout. Please try again");
                return new ResponseEntity<>(new ErrorResponse("UNABLE TO PAYOUT.PLEASE TRY AGAIN"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            log.error("Error occurred during NonWayaPaymentRedeem: {}", ex.getMessage());
            throw new CustomException(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    private NonWayaBenefDTO nonWayaBenefDTO(WalletNonWayaPayment nonWayaPayment, String ref, String narration){
        NonWayaBenefDTO wayaBenefDTO = new NonWayaBenefDTO();
        wayaBenefDTO.setAmount(nonWayaPayment.getTranAmount());
        wayaBenefDTO.setPaymentReference(ref);
        wayaBenefDTO.setTranCrncy(nonWayaPayment.getCrncyCode());
        wayaBenefDTO.setTranNarration(narration);
        wayaBenefDTO.setReceiverName(nonWayaPayment.getReceiverName());
        wayaBenefDTO.setSenderName(nonWayaPayment.getSenderName());
        wayaBenefDTO.setBeneficiaryUserId(nonWayaPayment.getBeneficiaryUserId());
        wayaBenefDTO.setBeneficiaryProfileId(nonWayaPayment.getBeneficiaryProfileId());
        return wayaBenefDTO;
    }

    @Override
    public ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request, NonWayaPayPIN transfer) {
        try {
            log.info("NonWayaRedeemPIN request received: {}", transfer);

            String token = request.getHeader(SecurityConstants.HEADER_STRING);
            MyData userToken = tokenService.getTokenUser(token,request);
            if (userToken == null)
                return new ResponseEntity<>(new ErrorResponse("UNAUTHORIZED, PLEASE LOGIN"), HttpStatus.BAD_REQUEST);

            WalletNonWayaPayment check = walletNonWayaPaymentRepo.findByConfirmPINAndBeneficiaryUserId(transfer.getTokenPIN(),transfer.getBeneficiaryUserId()).orElse(null);
            if (check == null) {
                log.info("Invalid PIN detected for redemption: {}", transfer.getTokenPIN());
                return new ResponseEntity<>(new ErrorResponse("INVALID PIN, PLEASE TRY AGAIN"), HttpStatus.BAD_REQUEST);
            } else if (check.getStatus().equals(PaymentStatus.REJECT)) {
                log.info("Transfer request already rejected for PIN: {}", transfer.getTokenPIN());
                return new ResponseEntity<>(new ErrorResponse("TRANSFER REQUEST ALREADY REJECTED!"), HttpStatus.BAD_REQUEST);
            } else if (check.getStatus().equals(PaymentStatus.EXPIRED)) {
                log.info("Expired PIN detected: {}", transfer.getTokenPIN());
                return new ResponseEntity<>(new ErrorResponse("TOKEN HAS EXPIRED AFTER 30 DAYS"), HttpStatus.BAD_REQUEST);
            } else if (check.getStatus().equals(PaymentStatus.PAYOUT)) {
                log.info("Transfer already redeemed by: {}", check.getRedeemedEmail());
                return new ResponseEntity<>(new ErrorResponse("TRANSFER ALREADY REDEEMED BY "+check.getRedeemedEmail()), HttpStatus.BAD_REQUEST);
            } else if (check.getStatus().equals(PaymentStatus.PENDING)) {
                log.info("OTP needed to perform this action for PIN: {}", transfer.getTokenPIN());
                return new ResponseEntity<>(new ErrorResponse("OTP IS NEEDED TO PERFORM THIS ACTION, KINDLY REQUEST REDEEMER OTP"), HttpStatus.BAD_REQUEST);
            }

            WalletNonWayaPayment redeem = walletNonWayaPaymentRepo.findByTokenPIN(check.getTokenId(), transfer.getTokenPIN()).orElse(null);
            if (redeem == null) {
                log.info("Invalid PIN detected, PIN does not match generated token: {}", transfer.getTokenPIN());
                return new ResponseEntity<>(new ErrorResponse("INVALID PIN, PIN DOES NOT MATCH GENERATED TOKEN"), HttpStatus.BAD_REQUEST);
            }

            String senderUserId = String.valueOf(redeem.getMerchantId());
            String redeemerUserId = transfer.getBeneficiaryUserId();
            if (senderUserId.equals(redeemerUserId)) {
                log.info("Attempted redemption by same merchant, PIN: {}", transfer.getTokenPIN());
                return new ResponseEntity<>(new ErrorResponse("SAME MERCHANT CAN'T PROCESS AND REDEEM NON-WAYA TRANSFER REQUEST, PLEASE CONTACT SUPPORT!"), HttpStatus.BAD_REQUEST);
            }

            NonWayaRedeemDTO waya = new NonWayaRedeemDTO();
            waya.setReceiverName(redeem.getReceiverName());
            waya.setToken(redeem.getTokenId());
            waya.setStatusAction(NonWayaPaymentStatusAction.ACCEPTED);
            waya.setBeneficiaryProfileId(redeem.getBeneficiaryProfileId());
            waya.setBeneficiaryUserId(transfer.getBeneficiaryUserId());
            if (transfer.getRedeemerId() !=null)
                waya.setRedeemerId(transfer.getRedeemerId());
            if (transfer.getRedeemerEmail() != null)
                waya.setRedeemerEmail(transfer.getRedeemerEmail());

            ResponseEntity<?> nonWayaPaymentRedeemResponse = NonWayaPaymentRedeem(request, waya);
            log.info("Redeem response: {}", nonWayaPaymentRedeemResponse);

            if (!nonWayaPaymentRedeemResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to redeem transaction with PIN: {}", redeem.getPaymentReference());
                redeem.setStatus(PaymentStatus.PENDING);
                redeem.setUpdatedAt(LocalDateTime.now().plusHours(1));
                walletNonWayaPaymentRepo.save(redeem);
            }

            log.info("non Waya Payment Redeem Response ---->> {}", nonWayaPaymentRedeemResponse);
            return nonWayaPaymentRedeemResponse;
        } catch (Exception ex) {
            log.error("Error in NonWayaRedeemPIN: {}", ex.getLocalizedMessage());
            ex.printStackTrace();
            throw new CustomException(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public WalletAccount findUserAccount(Long userId, String profileId){
        try {
            log.info("Finding user account for user ID {} and profile ID {}", userId, profileId);

            WalletUser walletUser = walletUserRepository.findByUserIdAndProfileId(userId, profileId);
            if (walletUser == null) {
                List<WalletUser> walletUserList = walletUserRepository.findAllByUserId(userId);
                if (walletUserList.size() > 0 && walletUserList.size() == 1) {
                    walletUser = walletUserList.get(0);
                } else {
                    log.info("No wallet user found for user ID {} and profile ID {}", userId, profileId);
                    return null;
                }
            }
            Optional<WalletAccount> walletAccount = walletAccountRepository.findByDefaultAccount(walletUser);
            if (walletAccount.isPresent()) {
                log.info("User account found for user ID {} and profile ID {}", userId, profileId);
                return walletAccount.get();
            }
            log.info("No wallet account found for user ID {} and profile ID {}", userId, profileId);
            return null;
        } catch (Exception ex) {
            log.error("Error finding user account: {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public WalletAccount findByEmailOrPhoneNumberOrId(Boolean isAccountNumber, String value, String userId, String accountNo, String profileId) {
        userAccountService.securityCheck(Long.valueOf(userId), profileId);
        try {
            securityWtihAccountNo2(accountNo, Long.valueOf(userId), profileId);
        } catch (CustomException ex) {
            throw new CustomException("Your Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
        }

        try {
            log.info("Finding wallet account for user with value {}", value);

            if (!isAccountNumber) {
                return walletAccountRepository.findByAccount(value).orElseThrow(() ->
                        new CustomException("Wallet account not found for value " + value, HttpStatus.NOT_FOUND));
            }
            Optional<WalletUser> user;
            if (value.startsWith("234") || value.contains("@")) {
                user = walletUserRepository.findByEmailOrPhoneNumber(value);
            } else {
                user = walletUserRepository.findUserIdAndProfileId(Long.parseLong(value), profileId);
            }

            if (!user.isPresent()) {
                throw new CustomException("User doesn't exist", HttpStatus.NOT_FOUND);
            }

            return walletAccountRepository.findByDefaultAccount(user.get()).orElseThrow(() ->
                    new CustomException("Wallet account not found for value " + value, HttpStatus.NOT_FOUND));
        } catch (CustomException ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void securityWtihAccountNo2(String accountNo, long userId, String profileId) {
        try {
            log.info("Performing security check with account number {}", accountNo);

            boolean check = false;
            WalletUser xUser = walletUserRepository.findByUserIdAndProfileId(userId, profileId);
            List<WalletAccount> walletAccount = walletAccountRepository.findByUser(xUser);
            List<String> accountNoList = new ArrayList<>();
            for (WalletAccount data : walletAccount) {
                accountNoList.add(data.getAccountNo());
            }

            if (accountNoList.contains(accountNo)) {
                check = true;
            } else {
                throw new CustomException("Your Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
            }
            log.info("Security check result: {}", check);
            log.info("Account number: {}", accountNo);

        } catch (CustomException ex) {
            throw new CustomException("Your Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> EventCommissionPayment(HttpServletRequest request, EventPaymentDTO transfer) {
        try {
            log.info("Processing commission payment for customer account number: {}", transfer.getCustomerAccountNumber());

            WalletAccount acctComm = walletAccountRepository.findByAccountNo(transfer.getCustomerAccountNumber());
            if (!acctComm.getProduct_code().equals("SB901")) {
                return new ResponseEntity<>(new ErrorResponse("NOT COMMISSION WALLET"), HttpStatus.BAD_REQUEST);
            }

            String toAccountNumber = transfer.getCustomerAccountNumber();

            // take money from commission wallet to user commission wallet
            String officialCommissionAccount = coreBankingService.getEventAccountNumber(transfer.getEventId());

            ResponseEntity<?> response = coreBankingService.processTransaction(
                    new TransferTransactionDTO(officialCommissionAccount, toAccountNumber, transfer.getAmount(),
                            TransactionTypeEnum.CARD.getValue(), "NGN", transfer.getTranNarration(),
                            transfer.getPaymentReference(), CategoryType.COMMISSION.getValue(),
                            transfer.getReceiverName(), transfer.getSenderName()),
                    "COMMPMT", request);

            log.info("Commission payment response: {}", response);
            return response;
        } catch (Exception ex) {
            log.error("Error processing commission payment: {}", ex.getMessage());
            ex.printStackTrace();
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public ResponseEntity<?> BankTransferPaymentOfficial(HttpServletRequest request, BankPaymentOfficialDTO transfer) {
        BankPaymentDTO transferDTO = new BankPaymentDTO(
                transfer.getBankName(),
                transfer.getCustomerAccountNumber(),
                transfer.getAmount(),
                transfer.getTranCrncy(),
                transfer.getTranNarration(),
                transfer.getPaymentReference(),
                transfer.getTransactionCategory(),
                transfer.getSenderName(),
                transfer.getReceiverName(),
                transfer.getEventId());
        return BankTransferPayment(request, transferDTO);

    }

    @Override
    public ResponseEntity<?> BankTransferPaymentOfficialMultiple(HttpServletRequest request,
                                                                 List<BankPaymentOfficialDTO> transfer) {
        log.info("Initiating bank transfer payment for multiple requests");
        ResponseEntity<?> response;
        ArrayList<Object> resObjects = new ArrayList<>();
        for (BankPaymentOfficialDTO data : transfer) {
            response = BankTransferPaymentOfficial(request, data);
            resObjects.add(response.getBody());
        }
        log.info("Bank transfer payment for multiple requests completed successfully");
        return new ResponseEntity<>(new SuccessResponse("TRANSACTION SUCCESSFUL", resObjects), HttpStatus.OK);
    }


    public ResponseEntity<?> BankTransferPayment(HttpServletRequest request, BankPaymentDTO transfer) {

        log.info("BankTransferPayment :: " + transfer);

        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);

        String wayaDisbursementAccount = coreBankingService
                .getEventAccountNumber(EventCharge.DISBURS_.name().concat(transfer.getEventId()));

        log.info("BankTransferPayment :: wayaDisbursementAccount " + wayaDisbursementAccount);

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(
                new TransferTransactionDTO(transfer.getCustomerAccountNumber(), wayaDisbursementAccount,
                        transfer.getAmount(),
                        TransactionTypeEnum.TRANSFER.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), transfer.getTransactionCategory(), transfer.getReceiverName(),
                        transfer.getSenderName()),
                transfer.getEventId(), request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
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

        String message = formatNewMessage(transfer.getAmount(), tranId, new Date().toString(), transfer.getTranCrncy(),
                transfer.getTranNarration(), transfer.getSenderName(), transfer.getReceiverName(),
                xAccount.getClr_bal_amt(), description, transfer.getBankName());

        CompletableFuture.runAsync(() -> customNotification.pushInApp(token, fullName, xUser.getUserId().toString(),
                message, userToken.getId(), CategoryType.WITHDRAW.name()));
        log.info("Debit response ---->> {}", debitResponse);
        return debitResponse;
    }

    public ApiResponse<Page<WalletTransaction>> findAllTransaction(int page, int size) {
        log.info("Finding all transactions");
        Page<WalletTransaction> transaction = walletTransactionRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        log.info("Found {} transactions", transaction.getTotalElements());
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
    }

    public TransactionsResponse findClientTransaction(String tranId) {
        log.info("Finding client transaction with ID: {}", tranId);
        Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByTranIdIgnoreCase(tranId);
        if (transaction.isEmpty()) {
            log.error("Unable to find client transaction with ID: {}", tranId);
            return new TransactionsResponse(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
        }
        log.info("Found client transaction with ID: {}", tranId);
        return new TransactionsResponse(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction.get());
    }


    public TransactionsResponse findAllTransactionEntries(String tranId) {
        log.info("Finding all transaction entries for transaction ID: {}", tranId);
        Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByRelatedTrans(tranId);
        if (transaction.isEmpty()) {
            log.error("Unable to find transactions with transaction ID: {}", tranId);
            return new TransactionsResponse(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
        }
        log.info("Found {} transactions with transaction ID: {}", transaction.get().size(), tranId);
        return new TransactionsResponse(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction.get());
    }

    public ApiResponse<List<AccountStatementDTO>> ReportTransaction(String accountNo) {
        log.info("Generating transaction report for account number: {}", accountNo);
        List<AccountStatementDTO> transaction = tempwallet.TransactionReport(accountNo);
        if (transaction == null) {
            log.error("Unable to generate statement for account number: {}", accountNo);
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
        }
        log.info("Generated statement for account number: {}", accountNo);
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
    }

    @Override
    public ApiResponse<Page<WalletTransaction>> getTransactionByWalletId(int page, int size, Long walletId) {
        log.info("Retrieving transactions for wallet ID: {}", walletId);
        Optional<WalletAccount> account = walletAccountRepository.findById(walletId);
        if (account.isEmpty()) {
            log.error("Invalid account number: {}", walletId);
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
        }
        WalletAccount acct = account.get();
        Pageable sortedByName = PageRequest.of(page, size, Sort.by("tranDate"));
        Page<WalletTransaction> transaction = walletTransactionRepository.findAllByAcctNum(acct.getAccountNo(), sortedByName);
        if (transaction == null) {
            log.error("Unable to retrieve transactions for account number: {}", acct.getAccountNo());
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
        }
        log.info("Retrieved transactions for account number: {}", acct.getAccountNo());
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
    }

    @Override
    public ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber) {
        log.info("Finding transactions for account number: {}", accountNumber);
        WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
        if (account == null) {
            log.error("Invalid account number: {}", accountNumber);
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
        }
        Pageable sortedByName = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WalletTransaction> transaction = walletTransactionRepository.findAllByAcctNum(accountNumber, sortedByName);
        if (transaction == null) {
            log.error("Unable to generate statement for account number: {}", accountNumber);
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
        }
        log.info("Found {} transactions for account number: {}", transaction.getTotalElements(), accountNumber);
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transaction);
    }


    @Override
    public ResponseEntity<?> makeWalletTransaction(HttpServletRequest request, String command,
                                                   TransferTransactionDTO transfer) {

        log.info("Command: {}", command);
        log.info("Request: {}", transfer);

        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token,request);
        if (userToken == null) {
            return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
        }

        // check if user is a merchant
        String fromAccountNumber = transfer.getDebitAccountNumber();
        String toAccountNumber = transfer.getBenefAccountNumber();
        if (fromAccountNumber.equals(toAccountNumber)) {
            return new ResponseEntity<>(new ErrorResponse("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT"),
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

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(transactionDTO, "WAYATRAN", request);

        log.info("Response: {}", debitResponse);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            return debitResponse;
        }
        return debitResponse;

    }

    @Override
    public ResponseEntity<?> sendMoney(HttpServletRequest request, TransferTransactionDTO transfer) {

        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        MyData userToken = tokenService.getTokenUser(token,request);

        if (userToken == null) {
            return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
        }

        String fromAccountNumber = transfer.getDebitAccountNumber();
        String toAccountNumber = transfer.getBenefAccountNumber();
        if (fromAccountNumber.equals(toAccountNumber)) {
            return new ResponseEntity<>(new ErrorResponse("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT"),
                    HttpStatus.BAD_REQUEST);
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
        transactionDTO.setSenderName(transfer.getSenderName());
        transactionDTO.setBeneficiaryName(transfer.getBeneficiaryName());

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(transactionDTO, "WAYATRAN", request);

        log.info("Request: {}", transactionDTO);
        log.info("Response: {}", debitResponse);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            return debitResponse;
        }
        return debitResponse;

    }


    @Override
    public ResponseEntity<?> sendMoneyToSimulatedUser(HttpServletRequest request, List<TransferSimulationDTO> transfer) {
        // Check that only admin can perform this action
        log.info("send Money To Simulated User: {}", transfer);

        ResponseEntity<?> resp = null;
        ArrayList<Object> responseList = new ArrayList<>();
        try {
            for (TransferSimulationDTO data : transfer) {
                resp = MoneyTransferSimulation(request, data);
                responseList.add(resp.getBody());
            }
            log.info("Money Transfer Simulation Responses: {}", responseList);
            return resp;
        } catch (Exception ex) {
            log.error("Error occurred during money transfer simulation: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    public ResponseEntity<?> MoneyTransferSimulation(HttpServletRequest request, TransferSimulationDTO transfer) {
        TransferTransactionDTO transactionDTO = new TransferTransactionDTO(
                transfer.getDebitAccountNumber(), transfer.getBenefAccountNumber(), transfer.getAmount(),
                transfer.getTranType(), transfer.getTranCrncy(), transfer.getTranNarration(),
                transfer.getPaymentReference(), CategoryType.FUNDING.getValue(), transfer.getReceiverName(),
                transfer.getSenderName());

        log.info("Money Transfer Simulation Request: {}", transactionDTO);

        return coreBankingService.processTransaction(transactionDTO, "WAYASIMU", request);
    }

    @Override
    public ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request, DirectTransactionDTO transfer) {
        String wayaDisbursementAccount = coreBankingService.getEventAccountNumber(transfer.getEventId());

        if (!transfer.getSecureKey()
                .equals("yYSowX0uQVUZpNnkY28fREx0ayq+WsbEfm2s7ukn4+RHw1yxGODamMcLPH3R7lBD+Tmyw/FvCPG6yLPfuvbJVA==")) {
            log.error("Invalid Secure Key: {}", transfer.getSecureKey());
            return new ResponseEntity<>(new ErrorResponse("Invalid Key"), HttpStatus.BAD_REQUEST);
        }

        WalletAcountVirtual virtualAccount = walletAcountVirtualRepository.findByIdAccount(transfer.getVId(),
                transfer.getVAccountNo());
        if (virtualAccount == null) {
            log.error("Invalid Virtual Account: VId={}, VAccountNo={}", transfer.getVId(), transfer.getVAccountNo());
            return new ResponseEntity<>(new ErrorResponse("Invalid Virtual Account"), HttpStatus.BAD_REQUEST);
        }
        Long userId = Long.parseLong(virtualAccount.getUserId());
        log.info("USER ID: {}", userId);
        WalletUser user = walletUserRepository.findByUserIdAndProfileId(userId, transfer.getProfileId());
        if (user == null) {
            log.error("User not found for ID: {}, Profile ID: {}", userId, transfer.getProfileId());
            return new ResponseEntity<>(new ErrorResponse("Invalid Virtual Account"), HttpStatus.BAD_REQUEST);
        }
        Optional<WalletAccount> optionalAccount = walletAccountRepository.findByDefaultAccount(user);
        if (optionalAccount.isEmpty()) {
            log.error("No default wallet for user: {}", userId);
            return new ResponseEntity<>(new ErrorResponse("No default wallet for virtual account"), HttpStatus.BAD_REQUEST);
        }
        WalletAccount defaultAccount = optionalAccount.get();
        String toAccountNumber = defaultAccount.getAccountNo();

        TransferTransactionDTO transactionDTO = new TransferTransactionDTO(wayaDisbursementAccount, toAccountNumber,
                transfer.getAmount(), TransactionTypeEnum.BANK.getValue(), "NGN", transfer.getTranNarration(),
                transfer.getPaymentReference(), CategoryType.TRANSFER.getValue(), transfer.getReceiverName(),
                transfer.getSenderName());

        log.info("Virtual Payment Money Request: {}", transactionDTO);

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(transactionDTO, "WAYATRAN", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to process transaction. Status Code: {}", debitResponse.getStatusCode());
        }
        return debitResponse;
    }


    @Override
    public ResponseEntity<?> PostExternalMoney(HttpServletRequest request, CardRequestPojo transfer, Long userId) {
        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            log.error("No provider switched");
            return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
        }
        log.info("WALLET PROVIDER: {}", provider.getName());
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
        MyData userToken = tokenService.getTokenUser(token,request);
        if (userToken == null) {
            throw new CustomException("INVALID TOKEN", HttpStatus.NOT_FOUND);
        }

        String fromAccountNumber = transfer.getOfficeDebitAccount();
        String toAccountNumber = transfer.getOfficeCreditAccount();
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.NOT_FOUND);
        }
        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
        CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

        ResponseEntity<?> debitResponse = coreBankingService
                .processTransaction(new TransferTransactionDTO(fromAccountNumber, toAccountNumber, transfer.getAmount(),
                        tranType.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), tranCategory.name(), transfer.getReceiverName(),
                        transfer.getSenderName()), "WAYATRAN", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            return debitResponse;
        }
        return debitResponse;
    }

    public ResponseEntity<?> doOfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer) {
        if (transfer.getTransactionChannel() == null) {
            transfer.setTransactionChannel(TransactionChannel.WAYABANK.name());
        }

        TransferTransactionDTO transferTransactionDTO = new TransferTransactionDTO(
                transfer.getOfficeDebitAccount(), transfer.getCustomerCreditAccount(), transfer.getAmount(),
                transfer.getTranType(), transfer.getTranCrncy(), transfer.getTranNarration(),
                transfer.getPaymentReference(), CategoryType.FUNDING.getValue(), transfer.getReceiverName(),
                transfer.getSenderName());
        transferTransactionDTO.setTransactionChannel(transfer.getTransactionChannel());

        log.info("Performing official user transfer: {}", transferTransactionDTO);
        ResponseEntity<?> response = coreBankingService.processTransaction(transferTransactionDTO, "WAYATRAN", request);

        log.info("Official user transfer response: {}", response);
        return response;
    }


    public ApiResponse<?> OfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer,
                                               boolean isMifos) {
        log.info("Official user transfer initiated: {}", transfer);

        ResponseEntity<?> response = doOfficialUserTransfer(request, transfer);

        log.info("Official user transfer response: {}", response);
        return new ApiResponse<>(response.getStatusCode().is2xxSuccessful(),
                response.getStatusCode().value(), "PROCESSED", response.getBody());
    }

    @Override
    public ResponseEntity<?> OfficialUserTransferSystemSwitch(Map<String, String> mapp, String token,
                                                              HttpServletRequest request, OfficeUserTransferDTO transfer) {
        log.info("Official user transfer with system switch initiated: {}", transfer);

        MyData userToken = tokenService.getTokenUser(token, request);
        if (userToken == null) {
            throw new CustomException("INVALID TOKEN", HttpStatus.NOT_FOUND);
        }

        String fromAccountNumber = transfer.getOfficeDebitAccount();
        String toAccountNumber = transfer.getCustomerCreditAccount();
        if (fromAccountNumber.equals(toAccountNumber)) {
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

        ResponseEntity<?> debitResponse = coreBankingService
                .processTransaction(new TransferTransactionDTO(fromAccountNumber, toAccountNumber, transfer.getAmount(),
                        tranType.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), tranCategory.name(), transfer.getReceiverName(),
                        transfer.getSenderName()), "WAYATRAN", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error occurred during official user transfer with system switch: {}", debitResponse);
            return debitResponse;
        }
        log.info("Official user transfer with system switch successful");
        return debitResponse;
    }

    @Override
    public ResponseEntity<?> OfficialUserTransferMultiple(HttpServletRequest request,
                                                          List<OfficeUserTransferDTO> transfer) {
        log.info("Official user multiple transfers initiated");

        ApiResponse<?> response;
        ArrayList<Object> resObjects = new ArrayList<>();
        for (OfficeUserTransferDTO data : transfer) {
            response = OfficialUserTransfer(request, data, false);
            resObjects.add(response.getData());
        }

        return new ResponseEntity<>(resObjects, HttpStatus.OK);
    }

    public ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer) {
        log.info("Admin send money initiated: {}", transfer);

        OfficeUserTransferDTO _transfer = new OfficeUserTransferDTO();
        BeanUtils.copyProperties(transfer, _transfer);
        _transfer.setOfficeDebitAccount(transfer.getDebitAccountNumber());
        _transfer.setCustomerCreditAccount(transfer.getBenefAccountNumber());

        ApiResponse<?> response = OfficialUserTransfer(request, _transfer, false);

        log.info("Admin send money response: {}", response);
        return response;
    }

    @Override
    public ApiResponse<?> AdminSendMoneyMultiple(HttpServletRequest request, List<AdminLocalTransferDTO> transfer) {
        log.info("Admin send money multiple initiated");

        ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
        try {
            for (AdminLocalTransferDTO data : transfer) {
                resp = AdminsendMoney(request, data);
            }
        } catch (Exception e) {
            log.error("Error occurred during admin send money multiple: {}", e.getMessage());
            e.printStackTrace();
        }

        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION DONE SUCCESSFULLY", resp.getData());
    }

    public ResponseEntity<?> AdminCommissionMoney(HttpServletRequest request, CommissionTransferDTO transfer) {
        log.info("Admin commission money initiated: {}", transfer);

        ClientComTransferDTO dd = modelMapper.map(transfer, ClientComTransferDTO.class);
        adminCheck(dd);

        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
        CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(new TransferTransactionDTO(
                        transfer.getDebitAccountNumber(), transfer.getBenefAccountNumber(), transfer.getAmount(),
                        tranType.getValue(), "NGN", transfer.getTranNarration(),
                        transfer.getPaymentReference(), tranCategory.name(), dd.getReceiverName(), dd.getSenderName()),
                "WAYATRAN", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error occurred during admin commission money: {}", debitResponse);
            return debitResponse;
        }
        log.info("Admin commission money successful");
        return debitResponse;
    }


    private void adminCheck(ClientComTransferDTO transfer) {
        log.info("Admin check initiated for commission transfer: {}", transfer);

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

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> ClientCommissionMoney(HttpServletRequest request, ClientComTransferDTO transfer) {
        log.info("Client commission money request received: {}", transfer);

        adminCheck(transfer);

        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
        CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(new TransferTransactionDTO(
                transfer.getDebitAccountNumber(), transfer.getBenefAccountNumber(), transfer.getAmount(),
                tranType.getValue(), "NGN", transfer.getTranNarration(),
                transfer.getPaymentReference(), tranCategory.name(), transfer.getReceiverName(),
                transfer.getSenderName()), "WAYATRAN", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error occurred during commission money transfer: {}", debitResponse);
            return debitResponse;
        }
        log.info("Commission money transfer successful");
        return debitResponse;
    }

    @Override
    public ResponseEntity<?> sendMoneyCharge(HttpServletRequest request, WalletTransactionChargeDTO transfer) {
        log.info("Send money charge request received: {}", transfer);

        TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
        transactionDTO.setTranType(transfer.getTranType());
        transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
        transactionDTO.setTranNarration(transfer.getTranNarration());
        transactionDTO.setTranCrncy(transfer.getTranCrncy());
        transactionDTO.setPaymentReference(transfer.getPaymentReference());
        transactionDTO.setDebitAccountNumber(transfer.getDebitAccountNumber());
        transactionDTO.setBenefAccountNumber(transfer.getBenefAccountNumber());
        transactionDTO.setAmount(transfer.getAmount());
        transactionDTO.setSenderName(transfer.getSenderName());
        transactionDTO.setBeneficiaryName(transfer.getReceiverName());

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(transactionDTO, "WAYATRAN", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error occurred during sending money charge: {}", debitResponse);
            return debitResponse;
        }

        log.info("Sending money charge successful");
        return debitResponse;
    }

    @Override
    public ResponseEntity<?> sendMoneyCustomer(HttpServletRequest request, WalletTransactionDTO transfer) {
        log.info("SendMoneyCustomer request received: {}", transfer);

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
            log.error("Email or phone number does not exist: {}", transfer.getEmailOrPhoneNumber());
            return new ResponseEntity<>("EMAIL OR PHONE NO DOES NOT EXIST", HttpStatus.BAD_REQUEST);
        }
        WalletUser user = wallet.get();
        Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
        if (defaultAcct.isEmpty()) {
            log.error("No account number exist for user: {}", user.getId());
            return new ResponseEntity<>("NO ACCOUNT NUMBER EXIST", HttpStatus.BAD_REQUEST);
        }
        String fromAccountNumber = transfer.getDebitAccountNumber();
        String toAccountNumber = defaultAcct.get().getAccountNo();
        if (fromAccountNumber.equals(toAccountNumber)) {
            log.error("Debit account cannot be the same as credit account: {}", fromAccountNumber);
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
        transactionDTO.setSenderName(transfer.getSenderName());
        transactionDTO.setBeneficiaryName(transfer.getReceiverName());

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(transactionDTO, "WAYATRAN", request);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Transaction processing failed: {}", debitResponse.getBody());
            return debitResponse;
        }
        log.info("Transaction processed successfully: {}", debitResponse.getBody());
        return debitResponse;
    }

    @Override
    public ApiResponse<?> AdminSendMoneyCustomer(HttpServletRequest request, AdminWalletTransactionDTO transfer) {
        log.info("AdminSendMoneyCustomer request received: {}", transfer);

        Optional<WalletUser> wallet;
        if (transfer.getEmailOrPhoneNumberOrUserId().startsWith("234") || transfer.getEmailOrPhoneNumberOrUserId().contains("@")) {
            wallet = walletUserRepository.findByEmailOrPhoneNumber(transfer.getEmailOrPhoneNumberOrUserId());
        } else {
            wallet = walletUserRepository.findUserIdAndProfileId(Long.valueOf(transfer.getEmailOrPhoneNumberOrUserId()),transfer.getProfileId());
        }

        if (wallet.isEmpty()) {
            log.error("Email, phone, or ID does not exist: {}", transfer.getEmailOrPhoneNumberOrUserId());
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "EMAIL OR PHONE OR ID DOES NOT EXIST", null);
        }

        Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(wallet.get());
        if (defaultAcct.isEmpty()) {
            log.error("No account number exist for user: {}", wallet.get().getId());
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO ACCOUNT NUMBER EXIST", null);
        }

        OfficeUserTransferDTO _transfer = new OfficeUserTransferDTO();
        BeanUtils.copyProperties(transfer, _transfer);
        _transfer.setOfficeDebitAccount(transfer.getDebitAccountNumber());
        _transfer.setCustomerCreditAccount(defaultAcct.get().getAccountNo());

        ApiResponse<?> response = OfficialUserTransfer(request, _transfer, false);
        log.info("AdminSendMoneyCustomer response: {}", response);

        return response;
    }

    private WalletAccount adminChecks(ClientWalletTransactionDTO transfer) {
        log.info("Admin checks for ClientSendMoneyCustomer request: {}", transfer);

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
                .findByEmailAddressOrMobileNoAndProfileId(transfer.getEmailOrPhoneNumberOrUserId(),transfer.getEmailOrPhoneNumberOrUserId(),transfer.getProfileId());
        if (!wallet.isPresent()) {
            Long userId = Long.valueOf(transfer.getEmailOrPhoneNumberOrUserId());
            wallet = walletUserRepository.findUserIdAndProfileId(userId,transfer.getProfileId());
            if (wallet.isEmpty()) {
                throw new CustomException("EMAIL OR PHONE OR ID DOES NOT EXIST", HttpStatus.NOT_FOUND);
            }
        }

        WalletUser user = wallet.get();
        Optional<WalletAccount> defaultAcct = walletAccountRepository.findByDefaultAccount(user);
        if (defaultAcct.isEmpty()) {
            throw new CustomException("NO ACCOUNT NUMBER EXIST", HttpStatus.NOT_FOUND);
        }
        log.info("Default account ---->> {}", defaultAcct.get());
        return defaultAcct.get();
    }


    @Override
    public ResponseEntity<?> ClientSendMoneyCustomer(HttpServletRequest request, ClientWalletTransactionDTO transfer) {
        log.info("ClientSendMoneyCustomer request received: {}", transfer);

        // check for admin
        WalletAccount defaultAcct = adminChecks(transfer);

        String fromAccountNumber = transfer.getDebitAccountNumber();
        String toAccountNumber = defaultAcct.getAccountNo();
        if (fromAccountNumber.equals(toAccountNumber)) {
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

        ResponseEntity<?> debitResponse = coreBankingService.processTransaction(transactionDTO, "WAYATRAN", request);

        log.info("ClientSendMoneyCustomer response: {}", debitResponse);

        if (!debitResponse.getStatusCode().is2xxSuccessful()) {
            return debitResponse;
        }
        return debitResponse;
    }


    private void removeLien(WalletAccount accountDebit, BigDecimal amount) {

        log.info("############### AccountDebit ::: #################3  " + accountDebit);
        // get user current Lien
        AccountLienDTO accountLienDTO = new AccountLienDTO();
        accountLienDTO.setCustomerAccountNo(accountDebit.getAccountNo());
        accountLienDTO.setLien(false);
        accountLienDTO.setLienReason("no longer needed");
        // double lienAmount = accountDebit.getLien_amt() - amount.doubleValue();

        accountLienDTO.setLienAmount(amount);

        ResponseEntity<?> responseEntity;
        try {
            responseEntity = userAccountService.AccountAccessLien(accountLienDTO);
        } catch (CustomException ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

        log.info("############### RESPONSE FROM REMOVING LIEN  :: ###############" + responseEntity);

    }

    private void postToMifos(String token, WalletAccount accountCredit, WalletAccount accountDebit, BigDecimal amount,
            String tranNarration, String tranId,
            TransactionTypeEnum tranType) {

        if (accountCredit.getNubanAccountNo() != null) {
            log.info("this is not  null");
        } else {
            log.info("this is null");
        }

        if (accountDebit.getNubanAccountNo() != null) {
            log.info(" 22this is not  null");
        } else {
            log.info("this is null");
        }
        MifosTransfer mifosTransfer = new MifosTransfer();
        mifosTransfer.setAmount(amount);
        mifosTransfer.setDestinationAccountNumber(
                accountCredit.getNubanAccountNo() != null ? accountCredit.getNubanAccountNo()
                : accountCredit.getAccountNo());

        mifosTransfer.setDestinationAccountType(
                accountCredit.getAccountType() != null ? accountCredit.getAccountType() : "SAVINGS");
        mifosTransfer.setDestinationCurrency(accountCredit.getAcct_crncy_code());
        mifosTransfer.setNarration(tranNarration);
        mifosTransfer.setRequestId(tranId + "345493");
        mifosTransfer.setSourceAccountNumber(accountDebit.getNubanAccountNo() != null ? accountDebit.getNubanAccountNo()
                : accountDebit.getAccountNo());

        mifosTransfer.setSourceAccountType("SAVINGS");
        mifosTransfer.setSourceCurrency(accountDebit.getAcct_crncy_code());
        mifosTransfer.setTransactionType(TransactionTypeEnum.TRANSFER.getValue());
        ExternalCBAResponse response;
        System.out.println(" here" + mifosTransfer);
        try {
            log.info("## token  ####### :: " + token);
            log.info("## BEFOR MIFOS REQUEST ####### :: " + mifosTransfer);
            response = mifosWalletProxy.transferMoney(mifosTransfer);
            log.info("### RESPONSE FROM MIFOS MifosWalletProxy  ###### :: " + response);
        } catch (CustomException ex) {
            System.out.println("ERROR posting to MIFOS :::: " + ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
        System.out.println("RESPONSE" + response.getResponseDescription());

    }

    private UserPricing getUserProduct(WalletAccount accountDebit, String eventId) {
        WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
        Long xUserId = xUser.getUserId();
        System.out.println("user pricing prod is" + xUserId + eventId);
        // get user charge by eventId and userID
        return userPricingRepository.findDetailsByCode(xUserId, eventId).orElse(null);
    }

    private WalletAccount getAccountByEventId(String eventId, String creditAcctNo) {
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

    public String securityCheck(String eventId, String creditAcctNo, String tranCrncy, BigDecimal amount,
            MyData tokenData, WalletAccount accountDebit, WalletAccount accountCredit) throws Exception {

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
//            WalletUser user = walletUserRepository.findByUserId(userId)
            WalletUser user = walletUserRepository.findByUserIdAndAccount(userId,accountDebit);
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
            if (accountDebit.isAcct_cls_flg()) {
                return "DJGO|DEBIT ACCOUNT IS CLOSED";
            }
            log.info("Debit Account is: {}", accountDebit.getAccountNo());
            log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
            if (accountDebit.getFrez_code() != null) {
                if (accountDebit.getFrez_code().equals("D")) {
                    return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
                }
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
            if (accountCredit.isAcct_cls_flg()) {
                return "DJGO|CREDIT ACCOUNT IS CLOSED";
            }

            log.info("Credit Account is: {}", accountCredit.getAccountNo());
            log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
            if (accountCredit.getFrez_code() != null) {
                if (accountCredit.getFrez_code().equals("C")) {
                    return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
                }
            }
        }

        return "";
    }

    private String validateDebitAccount(WalletAccount accountCredit, String creditAcctNo, BigDecimal tranAmCharges,
            WalletAccount accountDebit, MyData tokenData, BigDecimal amount) throws Exception {

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
//            WalletUser user = walletUserRepository.findByUserId(userId);
            WalletUser user = walletUserRepository.findByUserIdAndAccount(userId,accountDebit);
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
            if (accountDebit.isAcct_cls_flg()) {
                return "DJGO|DEBIT ACCOUNT IS CLOSED";
            }
            log.info("Debit Account is: {}", accountDebit.getAccountNo());
            log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
            if (accountDebit.getFrez_code() != null) {
                if (accountDebit.getFrez_code().equals("D")) {
                    return "DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE";
                }
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
            if (accountCredit.isAcct_cls_flg()) {
                return "DJGO|CREDIT ACCOUNT IS CLOSED";
            }

            log.info("Credit Account is: {}", accountCredit.getAccountNo());
            log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
            if (accountCredit.getFrez_code() != null) {
                if (accountCredit.getFrez_code().equals("C")) {
                    return "DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE";
                }
            }
        }
        return "";
    }

    private BigDecimal computePercentage(BigDecimal amount, BigDecimal percentageValue) {
        BigDecimal per = BigDecimal.valueOf(percentageValue.doubleValue() / 100);
        log.info("Computing percentage :: {}", per);
        return BigDecimal.valueOf(per.doubleValue() * amount.doubleValue());
    }

    public BigDecimal computeTransFee(String accountDebit, BigDecimal amount, String eventId) {
        log.info("Computing transaction fee for account: {}, amount: {}, event ID: {}", accountDebit, amount, eventId);
        return coreBankingService.computeTotalTransactionFee(accountDebit, amount, eventId);
    }

    public BigDecimal getChargesAmount(UserPricing userPricingOptional, BigDecimal amount) {
        BigDecimal percentage = null;
        log.info("Inside getChargesAmount userPricingOptional" + userPricingOptional);
        log.info("Inside getChargesAmount amount" + amount);
        if (userPricingOptional.getStatus().equals(ProductPriceStatus.GENERAL)) {
            log.info(" #### GENERAL  PRICING #####  ::");
            percentage = computePercentage(amount, userPricingOptional.getGeneralAmount());
            // apply discount if applicable
            log.info(" #### GENERAL  PRICe  #####  :: " + percentage);
        } else if (userPricingOptional.getStatus().equals(ProductPriceStatus.CUSTOM)) {
            if (userPricingOptional.getPriceType().equals(PriceCategory.FIXED)) {
                percentage = userPricingOptional.getGeneralAmount();
                log.info(" #### CUSTOM  PRICe  #####  :: " + percentage);
            } else {
                // apply discount if applicable
                percentage = computePercentage(amount, userPricingOptional.getCustomAmount());
            }
        }
        if (amount.doubleValue() <= 0) {
            return percentage;
        }

        if (percentage.doubleValue() > userPricingOptional.getCapPrice().doubleValue()) {
            percentage = userPricingOptional.getCapPrice();
        }

        log.info("CAP PRICE ::" + userPricingOptional.getCapPrice());
        log.info("TRANSACTION FEE ::" + percentage);
        return percentage;

    }

    private BigDecimal computeCap() {

        return new BigDecimal(Long.min(0, 9));
    }

    @Override
    public ApiResponse<?> getStatement(String accountNumber) {
        log.info("Received request to get statement for account: {}", accountNumber);
        WalletAccountStatement statement;
        WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
        if (account == null) {
            log.error("Invalid account number.");
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID ACCOUNT NO", null);
        }
        List<WalletTransaction> transaction = walletTransactionRepository.findByAcctNumEquals(accountNumber);
        if (transaction == null) {
            log.error("Unable to generate statement.");
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "UNABLE TO GENERATE STATEMENT", null);
        }
        BigDecimal blockedAmount = account.getBlockAmount();
        statement = new WalletAccountStatement(BigDecimal.valueOf(account.getClr_bal_amt()), transaction, blockedAmount);
        log.info("Statement generated successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", statement);
    }

    private void updatePaymentRequestStatus(String reference) {
        log.info("Updating payment request status for reference: {}", reference);
        Optional<WalletNonWayaPayment> walletNonWayaPayment = walletNonWayaPaymentRepo.findByToken(reference);
        if (walletNonWayaPayment.isPresent()) {
            WalletNonWayaPayment walletNonWayaPayment1 = walletNonWayaPayment.get();
            walletNonWayaPayment1.setStatus(PaymentStatus.RESERVED);
            walletNonWayaPaymentRepo.save(walletNonWayaPayment1);
        }
        Optional<WalletPaymentRequest> walletPaymentRequest = walletPaymentRequestRepo.findByReference(reference);
        if (walletPaymentRequest.isPresent()) {
            WalletPaymentRequest walletPaymentRequest1 = walletPaymentRequest.get();
            walletPaymentRequest1.setStatus(PaymentRequestStatus.RESERVED);
            walletPaymentRequestRepo.save(walletPaymentRequest1);
        }
        log.info("Payment request status updated successfully.");
    }


    @Override
    public ResponseEntity<?> EventReversePaymentRequest(HttpServletRequest request,
                                                        EventPaymentRequestReversal eventPay) {
        log.info("Received request to reverse event payment. ---->> {}", eventPay);
        WalletAccount walletAccount = getAcount(Long.valueOf(eventPay.getSenderId()), eventPay.getProfileId());
        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventPay.getEventId());
        if (eventInfo.isEmpty()) {
            log.error("Event does not exist.");
            return new ResponseEntity<>(new ErrorResponse("EVENT DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
        }
        WalletEventCharges charge = eventInfo.get();
        WalletAccount accountDebitTeller = walletAccountRepository
                .findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), walletAccount.getSol_id())
                .orElse(null);

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
            log.error("No provider switched.");
            return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);
        }

        log.info("WALLET PROVIDER: " + provider.getName());
        ApiResponse<?> res;
        res = OfficialUserTransfer(request, officeTransferDTO, false);
        if (!res.getStatus()) {
            log.error("Transaction reversal failed.");
            return new ResponseEntity<>(res, HttpStatus.EXPECTATION_FAILED);
        }
        CompletableFuture.runAsync(() -> updatePaymentRequestStatus(eventPay.getPaymentRequestReference()));

        log.info("Transaction reversal request processed successfully.");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Override
    public ApiResponse<?> TranRevALLReport(Date fromdate, Date todate) {
        log.info("Received request to retrieve reversal report.");
        LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<WalletTransaction> transaction = walletTransactionRepository.findByReverse(fromDate, toDate);
        if (transaction.isEmpty()) {
            log.info("No reversal report found for the specified date range.");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
        }
        log.info("Reversal report retrieved successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSAL REPORT SUCCESSFULLY", transaction);
    }

    @Override
    public ApiResponse<?> PaymentTransAccountReport(Date fromdate, Date todate, String accountNo) {
        log.info("Received request to retrieve payment transaction report by account number.");
        LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<WalletTransaction> transaction = walletTransactionRepository.findByAccountReverse(fromDate, toDate,
                accountNo);
        if (transaction.isEmpty()) {
            log.info("No payment transaction report found for the specified account number and date range.");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
        }
        log.info("Payment transaction report by account number retrieved successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSAL REPORT SUCCESSFULLY", transaction);
    }


    @Override
    public ApiResponse<?> PaymentAccountTrans(Date fromdate, Date todate, String wayaNo) {
        log.info("Request received to retrieve payment account transactions.");
        LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<WalletTransaction> transaction = walletTransactionRepository.findByOfficialAccount(fromDate, toDate,
                wayaNo);
        if (transaction.isEmpty()) {
            log.info("No payment account transactions found for the specified date range.");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
        }
        log.info("Payment account transactions retrieved successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "OFFICIAL ACCOUNT SUCCESSFULLY", transaction);
    }

    @Override
    public ApiResponse<?> viewTransActivities(String userId,String profileId) {
        WalletUser walletUser = walletUserRepository.findByUserIdAndProfileId(Long.valueOf(userId),profileId);
        // walletUser.getId()
        WalletAccount walletTransaction = walletAccountRepository.findByAccountUser(walletUser).orElse(null);

        return null;
    }

    @Override
    public ApiResponse<?> PaymentOffTrans(int page, int size, String fillter) {
        System.out.println("wallet fillter " + fillter);

        Pageable pagable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();
        Page<WalletTransaction> walletTransactionPage = null;

        if (fillter != null) {
            walletTransactionPage = walletTransactionRepository.findByAccountOfficial3(pagable, fillter);

        } else {
            walletTransactionPage = walletTransactionRepository.findByAccountOfficial(pagable);
        }

        List<WalletTransaction> transaction = walletTransactionPage.getContent();
        response.put("transaction", transaction);
        response.put("currentPage", walletTransactionPage.getNumber());
        response.put("totalItems", walletTransactionPage.getTotalElements());
        response.put("totalPages", walletTransactionPage.getTotalPages());

        if (transaction.isEmpty()) {
            log.error("NO REPORT SPECIFIED DATE");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
        }
        log.info("OFFICIAL ACCOUNT SUCCESSFULLY");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "OFFICIAL ACCOUNT SUCCESSFULLY", response);
    }

    @Override
    public ApiResponse<?> OfficialAccountReports(int page, int size, LocalDate fromdate, LocalDate todate,
                                                 String fillter) {
        log.info("Request received to generate official account report.");
        Pageable pagable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();
        Page<WalletTransaction> walletTransactionPage = null;

        if (fillter != null) {
            walletTransactionPage = walletTransactionRepository.findByAccountOfficialWithFilter(pagable, fromdate,
                    todate, fillter);
        } else {
            walletTransactionPage = walletTransactionRepository.findByAccountOfficialWithFilter(pagable, fromdate,
                    todate);
        }

        List<WalletTransaction> transaction = walletTransactionPage.getContent();
        response.put("transaction", transaction);
        response.put("currentPage", walletTransactionPage.getNumber());
        response.put("totalItems", walletTransactionPage.getTotalElements());
        response.put("totalPages", walletTransactionPage.getTotalPages());

        if (transaction.isEmpty()) {
            log.info("No official account report found for the specified date range.");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
        }
        log.info("Official account report generated successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "OFFICIAL ACCOUNT SUCCESSFULLY", response);
    }

    public ApiResponse<?> getAllTransactions(int page, int size, String fillter, LocalDate fromdate, LocalDate todate) {
        log.info("Request received to retrieve all transactions.");
        Pageable pagable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();
        Page<WalletTransaction> walletTransactionPage = null;

        if (fillter != null) {
            walletTransactionPage = walletTransactionRepository.findByAllTransactionsWithDateRangeAndTranTypeOR(pagable,
                    fillter, fromdate, todate);
        } else {
            walletTransactionPage = walletTransactionRepository.findByAllTransactionsWithDateRange(pagable, fromdate,
                    todate);
        }

        List<WalletTransaction> transaction = walletTransactionPage.getContent();
        response.put("transaction", transaction);
        response.put("currentPage", walletTransactionPage.getNumber());
        response.put("totalItems", walletTransactionPage.getTotalElements());
        response.put("totalPages", walletTransactionPage.getTotalPages());

        if (transaction.isEmpty()) {
            log.info("No transaction found for the specified date range.");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO RECORD FOUND", null);
        }
        log.info("Transactions retrieved successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION LIST SUCCESSFULLY", response);
    }


    public ApiResponse<?> getAllTransactionsByAccountNo(int page, int size, String fillter, LocalDate fromdate,
            LocalDate todate, String accountNo) {
        log.info("Method to get All Transactions By Account No ---->> {}", accountNo);
        Pageable pagable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();
        Page<WalletTransaction> walletTransactionPage = null;

        if (fillter != null) {
            // LocalDate fromtranDate, LocalDate totranDate
            walletTransactionPage = walletTransactionRepository
                    .findByAllTransactionsWithDateRangeAndTranTypeAndAccountNo(pagable, fillter, fromdate, todate,
                            accountNo);

            log.info("walletTransactionPage2 " + walletTransactionPage.getContent());
        } else {
            walletTransactionPage = walletTransactionRepository.findByAllTransactionsWithDateRangeaAndAccount(pagable,
                    fromdate, todate, accountNo);
        }

        List<WalletTransaction> transaction = walletTransactionPage.getContent();
        response.put("transaction", transaction);
        response.put("currentPage", walletTransactionPage.getNumber());
        response.put("totalItems", walletTransactionPage.getTotalElements());
        response.put("totalPages", walletTransactionPage.getTotalPages());

        if (transaction.isEmpty()) {
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO RECORD FOUND", null);
        }
        log.info("TRANSACTION LIST SUCCESSFULLY");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION LIST SUCCESSFULLY", response);
    }
    //

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
            log.info("No transaction found for reversal report.");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
        }
        log.info("Reversal report generated successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "REVERSAL REPORT SUCCESSFULLY", transaction);
    }

    public ApiResponse<?> TranChargeReport() {
        List<AccountTransChargeDTO> transaction = tempwallet.TransChargeReport();
        if (transaction.isEmpty()) {
            log.info("No charge report found.");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO CHARGE REPORT", null);
        }
        log.info("Charge report generated successfully.");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "CHARGE REPORT SUCCESSFULLY", transaction);
    }


    @Override
    public ResponseEntity<?> createBulkTransaction(HttpServletRequest request, BulkTransactionCreationDTO bulkList) {
        ResponseEntity<?> responseEntity;
        try {
            if (bulkList == null || bulkList.getUsersList().isEmpty()) {
                throw new CustomException("Bulk List cannot be null or Empty", HttpStatus.EXPECTATION_FAILED);
            }

            String fromAccountNumber = bulkList.getOfficeAccountNo();
            TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(bulkList.getTranType());

            log.info("Bulk Transaction Creation Request: {}", bulkList);
            responseEntity = createBulkTransaction(request, fromAccountNumber, bulkList.getTranCrncy(),
                    bulkList.getUsersList(),
                    tranType, bulkList.getTranNarration(), bulkList.getPaymentReference());
            log.info("Bulk Transaction Creation Response: {}", responseEntity);

        } catch (Exception e) {
            log.error("Error in Creating Bulk Account: {}", e.getMessage());
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
                log.info("Bulk Excel Transaction Request: {}", bulkLimt);
                debitResponse = createExcelTransaction(request, bulkLimt.getUsersList());
                log.info("Bulk Excel Transaction Response: {}", debitResponse);

                if (!debitResponse.getStatusCode().is2xxSuccessful()) {
                    return debitResponse;
                }

            } catch (Exception e) {
                log.error("Error in creating bulk excel transaction: {}", e.getMessage());
                throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        message = "Please upload an excel file!";
        log.warn("Invalid Excel File Uploaded: {}", file.getOriginalFilename());

        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }


    public ResponseEntity<?> createBulkTransaction(HttpServletRequest request, String debitAcctNo, String tranCrncy,
                                                   Set<UserTransactionDTO> usersList,
                                                   TransactionTypeEnum tranType, String tranNarration, String paymentRef) {
        String reference = tempwallet.TransactionGenerate();
        ResponseEntity<?> debitResponse = null;
        try {
            for (UserTransactionDTO mUser : usersList) {
                TransferTransactionDTO transactionDTO = new TransferTransactionDTO(debitAcctNo, mUser.getCustomerAccountNo(),
                        mUser.getAmount(), TransactionTypeEnum.TRANSFER.getValue(), "NGN", "Builk Account Creation",
                        reference, CategoryType.TRANSFER.getValue(), mUser.getReceiverName(), mUser.getSenderName());
                log.info("Bulk Transaction Request: {}", transactionDTO);
                debitResponse = coreBankingService.processTransaction(transactionDTO, "WAYATRAN", request);
                log.info("Bulk Transaction Response: {}", debitResponse);
            }
        } catch (Exception e) {
            log.error("Error in creating bulk transaction: {}", e.getMessage());
            e.printStackTrace();
        }
        return debitResponse;
    }

    public ResponseEntity<?> createExcelTransaction(HttpServletRequest request,
                                                    Set<ExcelTransactionCreationDTO> transList) {
        try {
            log.info("Creating Excel transaction...");
            ResponseEntity<?> debitResponse = null;
            for (ExcelTransactionCreationDTO mUser : transList) {
                log.info("Process Transaction: {}", mUser.toString());
                debitResponse = coreBankingService.processTransaction(new TransferTransactionDTO(
                        mUser.getOfficeAccountNo(), mUser.getCustomerAccountNo(), mUser.getAmount(),
                        TransactionTypeEnum.TRANSFER.getValue(), "NGN", mUser.getTranNarration(),
                        mUser.getPaymentReference(), CategoryType.TRANSFER.getValue(), mUser.getReceiverName(),
                        mUser.getSenderName()), "WAYATRAN", request);
            }
            log.info("Excel transaction created successfully.");
            return debitResponse;
        } catch (Exception e) {
            log.error("Error creating Excel transaction: {}", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> statementReport(Date fromdate, Date todate, String acctNo) {
        try {
            log.info("Retrieving statement report...");
            LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            List<WalletTransaction> transaction = walletTransactionRepository.findByStatement(fromDate, toDate, acctNo);
            if (transaction.isEmpty()) {
                log.warn("No statement report found for the specified date.");
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO REPORT SPECIFIED DATE", null);
            }
            log.info("Statement report retrieved successfully.");
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION REPORT", transaction);
        } catch (Exception e) {
            log.error("Error retrieving statement report: {}", e.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "Error retrieving statement report", null);
        }
    }


    public List<TransWallet> statementReport2(Date fromdate, Date todate, String acctNo) {
        return tempwallet.GetTransactionType2(acctNo, fromdate, todate);
    }

    @Override
    public ApiResponse<List<AccountStatementDTO>> ReportTransaction2(String accountNo) {
        return null;
    }

    @Override
    public ApiResponse<?> VirtuPaymentReverse(HttpServletRequest request, ReversePaymentDTO reverseDto)
            throws ParseException {
        return null;
    }

    @Override
    public ApiResponse<?> CommissionPaymentHistory() {
        try {
            log.info("Retrieving commission payment history...");
            List<CommissionHistoryDTO> listCom = tempwallet.GetCommissionHistory();
            if (listCom.isEmpty() || listCom == null) {
                log.error("No commission payment history found.");
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO COMMISSION", null);
            }
            log.info("Commission payment history retrieved successfully.");
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "COMMISSION LIST", listCom);
        } catch (Exception e) {
            log.error("Error retrieving commission payment history: {}", e.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "Error retrieving commission payment history", null);
        }
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

    public String formatMessage(BigDecimal amount, String tokenId, String tranDate, String narration,String receiverName, String senderName) {
        log.info("Formatting message with amount: {}, tokenId: {}, tranDate: {}, narration: {}, receiverName: {}, senderName: {}", amount, tokenId, tranDate, narration, receiverName, senderName);
        String message = "" + "Dear "+receiverName+",\n";
        message = message +"" + "You have received a credit alert from "+senderName+" see details below." + "\n";
        message = message + "" + "Amount : " + amount + "\n";
        message = message + "" + "TokenId : " + tokenId + "\n";
        message = message + "" + "TranDate : " + tranDate + "\n";
        message = message + "" + "Narration : " + narration + "\n";
        message = message + "" + "To claim this credit, use "+tokenId+" and proceed to download Wayabank App "+mobileDownloadLink+ " or visit any Wayabank outlets."+"\n";
        return message;
    }

    public String formatEmailMessage(BigDecimal amount, String tokenId,String receiverName, String senderName) {
        log.info("Formatting email message with amount: {}, tokenId: {}, receiverName: {}, senderName: {}", amount, tokenId, receiverName, senderName);
        String message = "" + "Dear "+receiverName+",\n";
        message = message +"" + "You have received a credit alert of NGN"+amount+" from "+senderName+", use this token id: "+tokenId+" to claim it by proceeding to download Wayabank App "+mobileDownloadLink+" or visit any Wayabank outlets" + "\n";
        return message;
    }

    public String formatMoneWayaMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
                                        String narration,
                                        String tokenId,String receiver,String receiverMeans, boolean phone) {
        log.info("Formatting MoneyWaya message with amount: {}, tranId: {}, tranDate: {}, tranCrncy: {}, narration: {}, tokenId: {}, receiver: {}, receiverMeans: {}, phone: {}", amount, tranId, tranDate, tranCrncy, narration, tokenId, receiver, receiverMeans, phone);
        String message = "" + "A transaction has occurred with token id: " + tokenId
                + "  on your account see details below." + "\n";
        message = message + "" + "Amount : " + amount + "\n";
        message = message + "" + "tranId : " + tranId + "\n";
        message = message + "" + "tranDate : " + tranDate + "\n";
        message = message + "" + "Currency : " + tranCrncy + "\n";
        message = message + "" + "Narration : " + narration + "\n";
        message = message + "" + "Receiver : " + receiver + "\n";
        if(phone){
            message = message + "" + "ReceiverPhone : " + receiverMeans + "\n";
        }else {
            message = message + "" + "ReceiverEmail : " + receiverMeans + "\n";
        }
        return message;
    }

    public String formatNewMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
                                   String narration) {
        log.info("Formatting new message with amount: {}, tranId: {}, tranDate: {}, tranCrncy: {}, narration: {}", amount, tranId, tranDate, tranCrncy, narration);
        String message = "" + "Message :" + "A credit transaction has occurred"
                + "  on your account see details below" + "\n";
        message = message + "" + "Amount :" + amount + "\n";
        message = message + "" + "tranId :" + tranId + "\n";
        message = message + "" + "tranDate :" + tranDate + "\n";
        message = message + "" + "Currency :" + tranCrncy + "\n";
        message = message + "" + "Narration :" + narration + "\n";
        return message;
    }

    public String formatNewMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
                                   String narration, String sender, String receiver, double availableBalance, String description,
                                   String bank) {
        log.info("Formatting new message with amount: {}, tranId: {}, tranDate: {}, tranCrncy: {}, narration: {}, sender: {}, receiver: {}, availableBalance: {}, description: {}, bank: {}", amount, tranId, tranDate, tranCrncy, narration, sender, receiver, availableBalance, description, bank);
        String message = "" + "Message :" + "A bank withdrawal has occurred"
                + " see details below" + "\n";
        message = message + "" + "Amount :" + amount + "\n";
        message = message + "" + "tranId :" + tranId + "\n";
        message = message + "" + "tranDate :" + tranDate + "\n";
        message = message + "" + "Currency :" + tranCrncy + "\n";
        message = message + "" + "Narration :" + narration + "\n";
        message = message + "" + "Desc :" + description + "\n";
        message = message + "" + "Avail Bal :" + availableBalance + "\n";
        message = message + "" + "Receiver :" + receiver + "\n";
        message = message + "" + "Bank :" + bank + "\n";
        return message;
    }

    public String formatSMSRecipient(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
                                     String narration, String sender, double availableBalance, String description) {
        log.info("Formatting SMS recipient with amount: {}, tranId: {}, tranDate: {}, tranCrncy: {}, narration: {}, sender: {}, availableBalance: {}, description: {}", amount, tranId, tranDate, tranCrncy, narration, sender, availableBalance, description);
        String message = "" + "Message :" + "A credit transaction has occurred"
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
        log.info("Formatting credit message with amount: {}, tranId: {}, tranDate: {}, tranCrncy: {}, narration: {}", amount, tranId, tranDate, tranCrncy, narration);
        String message = "" + "Message :" + "A credit transaction has occurred"
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
        log.info("Formatting debit message with amount: {}, tranId: {}, tranDate: {}, tranCrncy: {}, narration: {}", amount, tranId, tranDate, tranCrncy, narration);
        String message = "" + "Message :" + "A debit transaction has occurred"
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
        log.info("Formatting debit message with amount: {}, tranId: {}, tranDate: {}, tranCrncy: {}, narration: {}, sender: {}, availableBalance: {}, description: {}", amount, tranId, tranDate, tranCrncy, narration, sender, availableBalance, description);
        String message = "" + "Message :" + "A debit transaction has occurred"
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
        log.info("Formatting PIN message");
        String message = "Message : " + "Kindly confirm the reserved transaction with received pin: " + pin;
        return message;
    }

    public String formatMessageRedeem(BigDecimal amount, String tranId) {
        log.info("Formatting redemption message");
        String message = "" + "Message :" + "Transaction payout has occurred"
                + " on your account, Amount =" + amount + " Transaction Id = " + tranId;
        return message;
    }

    public String formatMessengerRejection(BigDecimal amount, String tranId,String name, String phone) {
        log.info("Formatting rejection message");
        String message = "" + "Message : " + "Transaction Amount: " + amount +", Transaction Id: " +tranId+" request sent to " +name+ ", with "+phone+" on your account has been rejected.";
        return message;
    }


    @Override
    public ResponseEntity<?> WayaQRCodePayment(HttpServletRequest request, WayaPaymentQRCode transfer) {
        try {
            log.info("WayaQRCodePayment Request: {}", transfer);
            String refNo = tempwallet.generateRefNo();
            if (refNo.length() < 12) {
                refNo = StringUtils.leftPad(refNo, 12, "0");
            }
            refNo = "QR-" + transfer.getPayeeId() + "-" + refNo;
            WalletQRCodePayment qrcode = new WalletQRCodePayment(transfer.getName(), transfer.getAmount(),
                    transfer.getReason(), refNo, LocalDate.now(), PaymentStatus.PENDING, transfer.getPayeeId(),
                    transfer.getCrncyCode());
            WalletQRCodePayment mPay = walletQRCodePaymentRepo.save(qrcode);
            log.info("WayaQRCodePayment Response: {}", mPay);
            return new ResponseEntity<>(new SuccessResponse("SUCCESS", mPay), HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Error in WayaQRCodePayment: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> WayaQRCodePaymentRedeem(HttpServletRequest request, WayaRedeemQRCode transfer) {
        try {
            log.info("WayaQRCodePaymentRedeem Request: {}", transfer);
            WalletQRCodePayment mPay = walletQRCodePaymentRepo.findByReferenceNo(transfer.getReferenceNo(), LocalDate.now())
                    .orElse(null);
            if (mPay != null && mPay.getAmount().compareTo(transfer.getAmount()) == 0) {
                if (mPay.getStatus().name().equals("PENDING")) {
                    String debitAcct = getAcount(transfer.getPayerId(),transfer.getPayerProfileId()).getAccountNo();
                    String creditAcct = getAcount(mPay.getPayeeId(),transfer.getPayeeProfileId()).getAccountNo();
                    TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct, creditAcct, transfer.getAmount(),
                            "TRANSFER", mPay.getCrncyCode(), "QR-CODE PAYMENT", mPay.getReferenceNo(),
                            transfer.getTransactionCategory(), transfer.getReceiverName(), transfer.getSenderName());
                    ResponseEntity<?> response = sendMoney(request, txt);
                    log.info("WayaQRCodePaymentRedeem Response: {}", response.getBody());
                    return response;
                }
            } else {
                return new ResponseEntity<>(new ErrorResponse("MISMATCH AMOUNT"), HttpStatus.BAD_REQUEST);
            }
            return null;
        } catch (Exception ex) {
            log.error("Error in WayaQRCodePaymentRedeem: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    
    public WalletAccount getAcount(Long userId,String profileId) {
        log.info(" getAcount ::  " + userId);
        WalletUser user = walletUserRepository.findByUserIdAndProfileId(userId,profileId);
        if (user == null) {
            log.error("INVALID USER ID");
            throw new CustomException("INVALID USER ID", HttpStatus.BAD_REQUEST);
        }
        WalletAccount account = walletAccountRepository.findByDefaultAccount(user).orElse(null);
        if (account == null) {
            log.error("INVALID USER ID");
            throw new CustomException("INVALID USER ID", HttpStatus.BAD_REQUEST);
        }
        log.info("Account ---->> {}", account);
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
        try {

            log.info("method request Waya Payment Request User to User {}",transfer);
            if (transfer.getPaymentRequest().getStatus().equals(PaymentRequestStatus.PENDING)) {
                WalletPaymentRequest mPayRequest = walletPaymentRequestRepo.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
                if (mPayRequest != null)
                    throw new CustomException("Request code already exist", HttpStatus.BAD_REQUEST);

                WalletPaymentRequest spay = new WalletPaymentRequest(transfer.getPaymentRequest());
                spay.setSenderProfileId(transfer.getPaymentRequest().getSenderProfileId());
                spay.setSenderName(transfer.getPaymentRequest().getSenderName());
                spay.setSenderEmail(transfer.getPaymentRequest().getSenderEmail());
                spay.setReceiverName(transfer.getPaymentRequest().getReceiverName());

                if(transfer.getPaymentRequest().getReceiverPhoneNumber() != null && !transfer.getPaymentRequest().getReceiverPhoneNumber().isEmpty())
                    spay.setReceiverPhoneNumber(transfer.getPaymentRequest().getReceiverPhoneNumber());

                if(transfer.getPaymentRequest().getReceiverEmail() != null && !transfer.getPaymentRequest().getReceiverEmail().isEmpty())
                    spay.setReceiverEmail(transfer.getPaymentRequest().getReceiverEmail());

                if(transfer.getPaymentRequest().getReceiverProfileId() != null)
                    spay.setReceiverProfileId(transfer.getPaymentRequest().getReceiverProfileId());

                if(transfer.getPaymentRequest().getReceiverId() != null)
                    spay.setReceiverId(transfer.getPaymentRequest().getReceiverId());

                WalletPaymentRequest mPay = walletPaymentRequestRepo.save(spay);
                return new ResponseEntity<>(new SuccessResponse("SUCCESS", mPay), HttpStatus.CREATED);

            } else if (transfer.getPaymentRequest().getStatus().equals(PaymentRequestStatus.PAID) ||
                    transfer.getPaymentRequest().getStatus().equals(PaymentRequestStatus.FAILED)) {

                WalletPaymentRequest mPayRequest = walletPaymentRequestRepo.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
                if (mPayRequest == null)
                    throw new CustomException("Request code does not exist", HttpStatus.BAD_REQUEST);

                log.info("::WalletPaymentRequest {}", mPayRequest);
                if (checkRequestStatus(mPayRequest.getStatus()) && mPayRequest.isWayauser()) {
                    log.info("::ABOUT TO PROCESS WAYA TO WAYA PAYMENT REQUEST {}",transfer.getPaymentRequest().getReference());
                    WalletAccount creditAcct = getAcount(Long.valueOf(mPayRequest.getSenderId()),transfer.getPaymentRequest().getSenderProfileId());
                    log.info("::Sender Account To Credit {}", creditAcct.getAccountNo());
                    WalletAccount debitAcct = getAcount(Long.valueOf(mPayRequest.getReceiverId()),transfer.getPaymentRequest().getReceiverProfileId());
                    log.info("::Receiver Account To Debit : {}", debitAcct.getAccountNo());

                    TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct.getAccountNo(),
                            creditAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN",
                            mPayRequest.getReason(),
                            mPayRequest.getReference(), mPayRequest.getCategory().getValue(),
                            mPayRequest.getReceiverName(),
                            mPayRequest.getSenderName());

                    mPayRequest.setReceiverId(transfer.getPaymentRequest().getReceiverId());
                    mPayRequest.setReceiverProfileId(transfer.getPaymentRequest().getReceiverProfileId());
                    walletPaymentRequestRepo.save(mPayRequest);
                    try {
                        log.info("::TransferTransactionDTO {}",txt);
                        ResponseEntity<?> res = sendMoney(request, txt);
                        log.info("::SEND-MONEY-RESPONSE {}", res);
                        if(res.getStatusCodeValue() == 200 || res.getStatusCodeValue() == 201) {
                            mPayRequest.setStatus(PaymentRequestStatus.PAID);
                        }else {
                            mPayRequest.setStatus(PaymentRequestStatus.FAILED);
                        }
                        walletPaymentRequestRepo.save(mPayRequest);
                        return res;
                    } catch (Exception e) {
                        log.error("::Error WayaPaymentRequestUsertoUser {}", e.getLocalizedMessage());
                        e.printStackTrace();
                        mPayRequest.setStatus(PaymentRequestStatus.FAILED);
                        walletPaymentRequestRepo.save(mPayRequest);
                        throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
                    }

                } else if (checkRequestStatus(mPayRequest.getStatus()) && !mPayRequest.isWayauser()) {
                    log.info("::Non-Waya-Payment-Dto", transfer.getPaymentRequest());
                    PaymentRequest mPay = transfer.getPaymentRequest();
                    mPay.setReceiverProfileId(transfer.getPaymentRequest().getReceiverProfileId());
                    mPay.setReceiverId(transfer.getPaymentRequest().getReceiverId());

                    WalletAccount debitAcct;
                    WalletAccount creditAcct;

                    if (!StringUtils.isNumeric(mPayRequest.getSenderId())) {
                        creditAcct = getOfficialAccount(mPayRequest.getSenderId());
                        debitAcct = getAcount(Long.valueOf(mPay.getReceiverId()),mPay.getReceiverProfileId());
                        OfficeUserTransferDTO transferDTO = new OfficeUserTransferDTO(creditAcct.getAccountNo(),
                                debitAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN",
                                mPayRequest.getReason(),
                                mPayRequest.getReference(), mPay.getReceiverName(),
                                mPay.getSenderName());
                        ApiResponse<?> res = OfficialUserTransfer(request, transferDTO, false);
                        if (res.getStatus()) {
//                            mPayRequest.setReceiverId(mPay.getReceiverId());
                            mPayRequest.setStatus(PaymentRequestStatus.PAID);
                            walletPaymentRequestRepo.save(mPayRequest);

                            return new ResponseEntity<>(res.getData(), HttpStatus.CREATED);
                        } else {
                            mPayRequest.setStatus(PaymentRequestStatus.FAILED);
                            walletPaymentRequestRepo.save(mPayRequest);
                            throw new CustomException(res.getMessage(), HttpStatus.EXPECTATION_FAILED);
                        }
                    } else {
                        creditAcct = getAcount(Long.valueOf(mPayRequest.getSenderId()),mPay.getSenderProfileId());
                        debitAcct = getAcount(Long.valueOf(mPay.getReceiverId()),mPay.getReceiverProfileId());
                        TransferTransactionDTO txt = new TransferTransactionDTO(debitAcct.getAccountNo(),
                                creditAcct.getAccountNo(), mPayRequest.getAmount(), "TRANSFER", "NGN",
                                mPayRequest.getReason(),
                                mPayRequest.getReference(), mPay.getTransactionCategory().getValue(),
                                mPay.getReceiverName(),
                                mPay.getSenderName());

                        try {
                            ResponseEntity<?> res = sendMoney(request, txt);
                            if (res.getStatusCodeValue() == 200 || res.getStatusCodeValue() == 201) {
                                mPayRequest.setStatus(PaymentRequestStatus.PAID);
                            }else {
                                mPayRequest.setStatus(PaymentRequestStatus.FAILED);
                            }
                            walletPaymentRequestRepo.save(mPayRequest);
                            return res;
                        } catch (Exception e) {
                            log.error("::Error Non-User {}",e.getLocalizedMessage());
                            e.printStackTrace();
                            mPayRequest.setStatus(PaymentRequestStatus.FAILED);
                            walletPaymentRequestRepo.save(mPayRequest);
                            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
                        }
                    }
                } else {
                    return new ResponseEntity<>(new ErrorResponse("Oops!\n Unable to process payment request, try again later"), HttpStatus.NOT_FOUND);
                }
            } else if (transfer.getPaymentRequest().getStatus().equals(PaymentRequestStatus.REJECTED)) {
                WalletPaymentRequest mPayRequest = walletPaymentRequestRepo.findByReference(transfer.getPaymentRequest().getReference()).orElse(null);
                if(mPayRequest == null)
                    throw new CustomException("Request code does not exist", HttpStatus.BAD_REQUEST);

                mPayRequest.setStatus(PaymentRequestStatus.REJECTED);
                mPayRequest.setRejected(true);
                WalletPaymentRequest mPay = walletPaymentRequestRepo.save(mPayRequest);
                log.info("Created ");
                return new ResponseEntity<>(new SuccessResponse("SUCCESS\n Payment request rejected", mPay), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(new ErrorResponse("Oops!\n Unable to process payment request, try again later"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    private boolean checkRequestStatus(PaymentRequestStatus status){
        if(status.name().equals(PaymentRequestStatus.PENDING.name()) || status.name().equals(PaymentRequestStatus.FAILED.name()))
            return true;
        return false;
    }

    @Override
    public ResponseEntity<?> PostOTPGenerate(HttpServletRequest request, String emailPhone,String businessId) {
        log.info("Generating OTP for email/phone: {} for business ID: {}", emailPhone, businessId);
        try {
            OTPResponse tokenResponse = authProxy.postOTPGenerate(emailPhone, businessId, request.getHeader(SecurityConstants.CLIENT_ID), request.getHeader(SecurityConstants.CLIENT_TYPE));
            if (tokenResponse == null) {
                throw new CustomException("Unable to deliver OTP", HttpStatus.BAD_REQUEST);
            }

            if (!tokenResponse.isStatus()) {
                log.error("Failed to generate OTP: {}", tokenResponse.getMessage());
                return new ResponseEntity<>(new ErrorResponse(tokenResponse.getMessage()), HttpStatus.BAD_REQUEST);
            }

            if (tokenResponse.isStatus()) {
                log.info("Generated OTP successfully: {}", tokenResponse);
            }

            return new ResponseEntity<>(new SuccessResponse("SUCCESS", tokenResponse), HttpStatus.CREATED);

        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Error occurred during OTP generation: {}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> PostOTPVerify(HttpServletRequest request, WalletRequestOTP transfer) {
        log.info("Verifying OTP for transfer: {}", transfer);
        try {
            OTPResponse tokenResponse = authProxy.postOTPVerify(transfer, request.getHeader(SecurityConstants.CLIENT_ID), request.getHeader(SecurityConstants.CLIENT_TYPE));
            if (tokenResponse == null) {
                throw new CustomException("Unable to deliver OTP", HttpStatus.BAD_REQUEST);
            }

            if (!tokenResponse.isStatus()) {
                log.error("Failed to verify OTP: {}", tokenResponse.getMessage());
                return new ResponseEntity<>(new ErrorResponse(tokenResponse.getMessage()), HttpStatus.BAD_REQUEST);
            }

            if (tokenResponse.isStatus()) {
                log.info("Verified OTP successfully: {}", tokenResponse);
            }

            return new ResponseEntity<>(new SuccessResponse("SUCCESS", tokenResponse), HttpStatus.CREATED);

        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Error occurred during OTP verification: {}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    public ResponseEntity<?> getTotalNoneWayaPaymentRequest(String userId) {
        log.info("Fetching total none-waya payment requests for user: {}", userId);
        long count = walletNonWayaPaymentRepo.findAllByTotal(userId);
        log.info("Retrieved total none-waya payment requests for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getExpierdNoneWayaPaymentRequest(String userId) {
        log.info("Fetching expired none-waya payment request amount for user: {}", userId);
        BigDecimal count = walletNonWayaPaymentRepo.findAllByExpiredAmount(userId);
        log.info("Retrieved expired none-waya payment request amount for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);      }


    public ResponseEntity<?> getReservedNoneWayaPaymentRequest(String userId) {
        log.info("Fetching reserved none-waya payment requests for user: {}", userId);
        long count = walletNonWayaPaymentRepo.findAllByReserved(userId);
        log.info("Retrieved reserved none-waya payment requests for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    public ResponseEntity<?> getPayoutNoneWayaPaymentRequest(String userId) {
        log.info("Fetching payout none-waya payment requests for user: {}", userId);
        long count = walletNonWayaPaymentRepo.findAllByPayout(userId);
        log.info("Retrieved payout none-waya payment requests for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    public ResponseEntity<?> getPendingNoneWayaPaymentRequest(String userId) {
        log.info("Fetching pending none-waya payment requests for user: {}", userId);
        long count = walletNonWayaPaymentRepo.findAllByPending(userId);
        log.info("Retrieved pending none-waya payment requests for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    public ResponseEntity<?> getExpiredNoneWayaPaymentRequest(String userId) {
        log.info("Fetching expired none-waya payment requests for user: {}", userId);
        long count = walletNonWayaPaymentRepo.findAllByExpired(userId);
        log.info("Retrieved expired none-waya payment requests for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    public ResponseEntity<?> getTotalNoneWayaPaymentRequestAmount(String userId) {
        log.info("Fetching total none-waya payment request amount for user: {}", userId);
        BigDecimal count = walletNonWayaPaymentRepo.findAllByTotalAmount(userId);
        log.info("Retrieved total none-waya payment request amount for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    public ResponseEntity<?> getReservedNoneWayaPaymentRequestAmount(String userId) {
        log.info("Fetching reserved none-waya payment request amount for user: {}", userId);
        BigDecimal count = walletNonWayaPaymentRepo.findAllByReservedAmount(userId);
        log.info("Retrieved reserved none-waya payment request amount for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }


    public ResponseEntity<?> getPayoutNoneWayaPaymentRequestAmount(String userId) {
        log.info("Fetching payout none-waya payment request amount for user: {}", userId);
        BigDecimal count = walletNonWayaPaymentRepo.findAllByPayoutAmount(userId);
        Map<String, BigDecimal> amount = new HashMap<>();
        amount.put("amount", count);
        log.info("Retrieved payout none-waya payment request amount for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
    }

    public ResponseEntity<?> getPendingNoneWayaPaymentRequestAmount(String userId) {
        log.info("Fetching pending none-waya payment request amount for user: {}", userId);
        BigDecimal count = walletNonWayaPaymentRepo.findAllByPendingAmount(userId);
        log.info("Retrieved pending none-waya payment request amount for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getExpierdNoneWayaPaymentRequestAmount(String userId) {
        log.info("Fetching expired none-waya payment request amount for user: {}", userId);
        BigDecimal count = walletNonWayaPaymentRepo.findAllByExpiredAmount(userId);
        log.info("Retrieved expired none-waya payment request amount for user {}: {}", userId, count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);    }


    public ResponseEntity<?> debitTransactionAmount() {
        log.info("Fetching debit transaction amount...");
        BigDecimal count = walletTransactionRepository.findByAllDTransaction();
        Map<String, BigDecimal> amount = new HashMap<>();
        amount.put("amount", count);
        log.info("Retrieved debit transaction amount: {}", count);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
    }

    public ResponseEntity<?> creditTransactionAmount() {
        log.info("Fetching credit transaction amount...");
        BigDecimal amount = walletTransactionRepository.findByAllCTransaction();
        log.info("Retrieved credit transaction amount: {}", amount);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
    }

    public ResponseEntity<?> debitAndCreditTransactionAmount() {
        log.info("Fetching debit and credit transaction amounts...");
        BigDecimal count = walletTransactionRepository.findByAllDTransaction();
        BigDecimal amount = walletTransactionRepository.findByAllCTransaction();
        if (ObjectUtils.isEmpty(count)) {
            count = BigDecimal.valueOf(0.0);
        }
        if (ObjectUtils.isEmpty(amount)) {
            amount = BigDecimal.valueOf(0.0);
        }
        BigDecimal total = BigDecimal.valueOf(count.doubleValue() + amount.doubleValue());
        log.info("Retrieved debit and credit transaction amounts: Debit={}, Credit={}, Total={}", count, amount, total);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", total), HttpStatus.OK);
    }

    public ResponseEntity<?> creditTransactionAmountOffical() {
        log.info("Fetching credit transaction amount (official)...");
        BigDecimal amount = walletTransactionRepository.findByAllCTransactionOfficial();
        log.info("Retrieved credit transaction amount (official): {}", amount);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
    }

    public ResponseEntity<?> debitTransactionAmountOffical() {
        log.info("Fetching debit transaction amount (official)...");
        BigDecimal amount = walletTransactionRepository.findByAllDTransactionOfficial();
        log.info("Retrieved debit transaction amount (official): {}", amount);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", amount), HttpStatus.OK);
    }


    public ResponseEntity<?> debitAndCreditTransactionAmountOfficial() {
        log.info("Fetching debit and credit transaction amount (official)...");
        BigDecimal count = walletTransactionRepository.findByAllCTransactionOfficial();
        BigDecimal amount = walletTransactionRepository.findByAllDTransactionOfficial();
        if (ObjectUtils.isEmpty(count)) {
            count = BigDecimal.ZERO;
        }
        if (ObjectUtils.isEmpty(amount)) {
            amount = BigDecimal.ZERO;
        }
        BigDecimal total = count.add(amount);
        log.info("Debit and Credit Transaction Official - Count: {}, Amount: {}", count, amount);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", total), HttpStatus.OK);
    }


    public ResponseEntity<?> getSingleAccountByEventID(String eventId) {
        log.info("Fetching single account by event ID: {}", eventId);
        String nonWayaDisbursementAccount = coreBankingService.getEventAccountNumber(eventId);
        log.info("Retrieved account number for event ID {}: {}", eventId, nonWayaDisbursementAccount);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", nonWayaDisbursementAccount), HttpStatus.OK);
    }

    @Override
    public ApiResponse<?> EventBuySellPayment(HttpServletRequest request, WayaTradeDTO eventPay) {
        log.info("Received request for event buy/sell payment processing.");
        throw new UnsupportedOperationException("Unimplemented method 'EventBuySellPayment'");
    }


    @Override
    public ResponseEntity<?> createBulkDebitTransaction(HttpServletRequest request, BulkTransactionCreationDTO bulk) {
        ResponseEntity<?> responseEntity;
        try {
            if (bulk == null || bulk.getUsersList().isEmpty()) {
                throw new CustomException("Bulk List cannot be null or Empty", HttpStatus.EXPECTATION_FAILED);
            }

            String toAccountNumber = bulk.getOfficeAccountNo();
            TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(bulk.getTranType());

            responseEntity = createBulkDebitTransactionHelper(request, toAccountNumber, bulk.getTranCrncy(),
                    bulk.getUsersList(), tranType, bulk.getTranNarration(), bulk.getPaymentReference());

            log.info("Bulk Debit Transaction Request: {}", bulk);
            log.info("Bulk Debit Transaction Response: {}", responseEntity.toString());

        } catch (Exception e) {
            log.error("Error in Creating Bulk Account:: {}", e.getMessage());
            throw new CustomException(e.getMessage(), BAD_REQUEST);
        }
        return responseEntity;
    }

    @Override
    public ResponseEntity<?> createBulkDebitExcelTrans(HttpServletRequest request, MultipartFile file) {
        String message;
        BulkTransactionExcelDTO bulkLimit;
        ResponseEntity<?> debitResponse;
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                bulkLimit = ExcelHelper.excelToBulkTransactionPojo(file.getInputStream(), file.getOriginalFilename());
                debitResponse = createDebitExcelTransaction(request, bulkLimit.getUsersList());

                if (!debitResponse.getStatusCode().is2xxSuccessful()) {
                    return debitResponse;
                }

                log.info("Bulk Debit Excel Transaction Request: File Name - {}, Content Type - {}", file.getOriginalFilename(), file.getContentType());
                log.info("Bulk Debit Excel Transaction Response: {}", debitResponse.toString());

            } catch (Exception e) {
                log.error("Failed to Parse excel data: {}", e.getMessage());
                throw new CustomException("Failed to parse Excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            message = "Please upload an excel file!";
            log.error("{}", message);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(debitResponse.getBody(), HttpStatus.OK);
    }

    public ResponseEntity<?> createBulkDebitTransactionHelper(HttpServletRequest request, String creditAcctNo, String tranCrncy,
                                                              Set<UserTransactionDTO> usersList, TransactionTypeEnum tranType, String tranNarration, String paymentRef) {
        String reference = tempwallet.TransactionGenerate();
        ResponseEntity<?> debitResponse = null;
        try {
            for (UserTransactionDTO mUser : usersList) {
                log.info("Process Bulk Transaction: {}", mUser.toString());
                debitResponse = coreBankingService.processTransaction(
                        new TransferTransactionDTO(mUser.getCustomerAccountNo(), creditAcctNo, mUser.getAmount(),
                                tranType.getValue(), tranCrncy, tranNarration, reference, CategoryType.TRANSFER.getValue(),
                                mUser.getReceiverName(), mUser.getSenderName()), "WAYATRAN", request);
                log.info("Bulk Debit Transaction Response: {}", debitResponse.toString());
            }

        } catch (Exception e) {
            log.error("Error in Bulk Debit Transaction: {}", e.getMessage());
            e.printStackTrace();
        }
        return debitResponse;
    }


    public ResponseEntity<?> createDebitExcelTransaction(HttpServletRequest request,
                                                         Set<ExcelTransactionCreationDTO> transList) {
        try {
            log.info("Creating debit transactions for ---->>> {}", transList);

            ResponseEntity<?> debitResponse = null;

            for (ExcelTransactionCreationDTO mUser : transList) {
                log.info("Processing Transaction: {}", mUser.toString());

                debitResponse = coreBankingService.processTransaction(new TransferTransactionDTO(
                        mUser.getCustomerAccountNo(), mUser.getOfficeAccountNo(), mUser.getAmount(),
                        TransactionTypeEnum.TRANSFER.getValue(), "NGN", mUser.getTranNarration(),
                        mUser.getPaymentReference(), CategoryType.TRANSFER.getValue(), mUser.getReceiverName(),
                        mUser.getSenderName()), "WAYATRAN", request);
            }

            log.info("Debit transactions processed successfully");
            return debitResponse;
        } catch (Exception e) {
            log.error("Error creating debit transactions: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating debit transactions");
        }
    }

    @Override
    public ResponseEntity<?> transactionAnalysis() {
        try {
            log.info("Performing transaction analysis...");

            TransactionAnalysis analysis = new TransactionAnalysis();

            // Overall analysis
            OverallAnalysis overall = new OverallAnalysis();
            BigDecimal totalRevenue = walletTransAccountRepo.totalRevenueAmount();
            CustomerTransactionSumary customerTransactionSumary = tempwallet.getCustomerTransactionSumary();

            // Count
            long totalRevenues = walletTransAccountRepo.totalRevenue();
            long countWithdrawal = walletTransRepo.countCustomersWithdrawal();
            long countDeposit = walletTransRepo.countCustomersDeposit();

            // Category trans analysis
            CategoryAnalysis category = new CategoryAnalysis();

            BigDecimal baxiPayment = walletTransAccountRepo.findByAllBaxiTransaction();
            BigDecimal quicketllerPayment = walletTransAccountRepo.findByAllQUICKTELLERTransaction();
            BigDecimal totalOutboundExternal = walletTransRepo.totalNipOutbound(nipgl);
            BigDecimal totalOutboundInternal = walletTransAccountRepo.findByAllOutboundInternalTransaction();
            BigDecimal totalPaystack = walletTransRepo.totalPayStack(paystackgl);
            BigDecimal totalNipInbound = walletTransRepo.totalNipInbound(nipgl);

            long baxiCount = walletTransAccountRepo.countBaxiTransaction();
            long quicktellerCount = walletTransAccountRepo.countQuickTellerTransaction();
            long outboundExternalCount = walletTransRepo.countNipOutbound(nipgl);
            long outboundInternalCount = walletTransAccountRepo.nipOutboundInternalCount();
            long paystackCount = walletTransRepo.countPayStack(paystackgl);
            long nipCount = walletTransRepo.countNipInbound(nipgl);

            try {
                TransactionAnalysis smuanalysis = smuProxy.GetTransactionAnalytics();
                if (!ObjectUtils.isEmpty(analysis)) {
                    // Overall analysis
                    BigDecimal _totalDeposit = customerTransactionSumary.getTotalDeposit().add(
                            smuanalysis.getOverallAnalysis().getSumResponse().get("totalDeposit")
                    );
                    customerTransactionSumary.setTotalDeposit(_totalDeposit);

                    BigDecimal _totalWithdrawal = customerTransactionSumary.getTotalDeposit().add(
                            smuanalysis.getOverallAnalysis().getSumResponse().get("totalWithdrawal")
                    );
                    customerTransactionSumary.setTotalWithdrawal(_totalWithdrawal);

                    totalRevenue.add(
                            smuanalysis.getOverallAnalysis().getSumResponse().get("totalRevenue")
                    );

                    totalRevenues += Long.parseLong(smuanalysis.getOverallAnalysis().getCountResponse().get("totalRevenue"));
                    countDeposit += Long.parseLong(smuanalysis.getOverallAnalysis().getCountResponse().get("countDeposit"));
                    countWithdrawal += Long.parseLong(smuanalysis.getOverallAnalysis().getCountResponse().get("countWithdrawal"));
                }
            } catch (Exception e) {
                log.error("Unable to fetch SMU users: {}", e.getMessage());
                e.printStackTrace();
            }

            Map<String, BigDecimal> overallSum = new HashMap<>();
            overallSum.put("totalBalance", customerTransactionSumary.getTotalBalance());
            overallSum.put("totalRevenue", totalRevenue);
            overallSum.put("totalDeposit", customerTransactionSumary.getTotalDeposit());
            overallSum.put("totalWithdrawal", customerTransactionSumary.getTotalWithdrawal());

            // Count response
            Map<String, String> overallCount = new HashMap<>();
            overallCount.put("totalRevenue", String.valueOf(totalRevenues));
            overallCount.put("countDeposit", String.valueOf(countDeposit));
            overallCount.put("countWithdrawal", String.valueOf(countWithdrawal));

            overall.setSumResponse(overallSum);
            overall.setCountResponse(overallCount);

            Map<String, BigDecimal> categorysum = new HashMap<>();
            categorysum.put("billsPaymentTrans", baxiPayment);
            categorysum.put("quicketllerPayment", quicketllerPayment);
            categorysum.put("outboundExternalTrans", totalOutboundExternal);
            categorysum.put("outboundInternalTrans", totalOutboundInternal);
            categorysum.put("totalPaystackTrans", totalPaystack);
            categorysum.put("nipInbountTrans", totalNipInbound);

            Map<String, String> categorycount = new HashMap<>();
            categorycount.put("billsCount", String.valueOf(baxiCount));
            categorycount.put("quicktellerCount", String.valueOf(quicktellerCount));
            categorycount.put("outboundExternalCount", String.valueOf(outboundExternalCount));
            categorycount.put("outboundInternalCount", String.valueOf(outboundInternalCount));
            categorycount.put("paystackCount", String.valueOf(paystackCount));
            categorycount.put("nipCount", String.valueOf(nipCount));

            category.setSumResponse(categorysum);
            category.setCountResponse(categorycount);

            // Category and overall response
            analysis.setCategoryAnalysis(category);
            analysis.setOverallAnalysis(overall);

            log.info("Transaction analysis completed successfully");
            return new ResponseEntity<>(new SuccessResponse("SUCCESS", analysis), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error performing transaction analysis: {}", ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error performing transaction analysis");
        }
    }


    @Override
    public ResponseEntity<?> transactionAnalysisFilterDate(Date fromdate, Date todate) {
        try {
            log.info("Performing transaction analysis with date filter from {} to {}", fromdate, todate);

            LocalDate fDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate tDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            TransactionAnalysis analysis = new TransactionAnalysis();

            // Overall trans analysis
            OverallAnalysis overall = new OverallAnalysis();

            BigDecimal totalRevenue = walletTransAccountRepo.totalRevenueAmountFilter(fromdate, todate);
            BigDecimal totalWithdrawal = walletTransRepo.totalCustomersWithdrawalFilter(fDate, tDate);
            BigDecimal totalDeposit = walletTransRepo.totalCustomersDepositFilter(fDate, tDate);
            BigDecimal totalBalance = totalDeposit.subtract(totalWithdrawal);

            // Count
            long totalRevenues = walletTransAccountRepo.totalRevenue();
            long countWithdrawal = walletTransRepo.countCustomersWithdrawal();
            long countDeposit = walletTransRepo.countCustomersDeposit();

            Map<String, BigDecimal> overallResp = new HashMap<>();
            overallResp.put("totalBalance", totalBalance);
            overallResp.put("totalRevenue", totalRevenue);
            overallResp.put("totalWithdrawal", totalWithdrawal);
            overallResp.put("totalDeposit", totalDeposit);

            // Count response
            Map<String, String> countresponse = new HashMap<>();
            countresponse.put("totalRevenue", String.valueOf(totalRevenues));
            countresponse.put("countWithdrawal", String.valueOf(countWithdrawal));
            countresponse.put("countDeposit", String.valueOf(countDeposit));

            overall.setSumResponse(overallResp);
            overall.setCountResponse(countresponse);

            // Category trans analysis
            CategoryAnalysis category = new CategoryAnalysis();

            BigDecimal baxisPayment = walletTransAccountRepo.findByAllBaxiTransactionByDate(fromdate, todate);
            BigDecimal totalOutboundInternal = walletTransAccountRepo.findByAllOutboundInternalTransactionByDate(fromdate, todate);
            BigDecimal totalCategoryOutboundExternal = walletTransRepo.totalNipOutboundFilter(fDate, tDate, nipgl);
            BigDecimal totalPaystack = walletTransRepo.totalPayStackFilter(fDate, tDate, paystackgl);
            BigDecimal totalNipInbound = walletTransRepo.totalNipInboundFilter(fDate, tDate, nipgl);

            // Count
            long baxiCount = walletTransAccountRepo.countBaxiTransaction();
            long quickTellerCount = walletTransAccountRepo.countBaxiTransaction();
            long outboundExternalCount = walletTransAccountRepo.nipOutboundExternalCount();
            long outboundInternalCount = walletTransAccountRepo.nipOutboundInternalCount();
            long paystackCount = walletTransAccountRepo.payStackCount();
            long nipCount = walletTransAccountRepo.nipInboundCount();

            Map<String, BigDecimal> categoryResponse = new HashMap<>();
            categoryResponse.put("billsPaymentTrans", baxisPayment);
            categoryResponse.put("outboundExternalTrans", totalCategoryOutboundExternal);
            categoryResponse.put("outboundInternalTrans", totalOutboundInternal);
            categoryResponse.put("totalPaystackTrans", totalPaystack);
            categoryResponse.put("nipInbountTrans", totalNipInbound);

            Map<String, String> categorycountResponse = new HashMap<>();
            categorycountResponse.put("baxiCount", String.valueOf(baxiCount));
            categorycountResponse.put("quickTellerCount", String.valueOf(quickTellerCount));
            categorycountResponse.put("outboundExternalCount", String.valueOf(outboundExternalCount));
            categorycountResponse.put("outboundInternalCount", String.valueOf(outboundInternalCount));
            categorycountResponse.put("paystackCount", String.valueOf(paystackCount));
            categorycountResponse.put("nipCount", String.valueOf(nipCount));

            category.setSumResponse(categoryResponse);
            category.setCountResponse(categorycountResponse);

            analysis.setCategoryAnalysis(category);
            analysis.setOverallAnalysis(overall);

            log.info("Transaction analysis completed successfully with date filter from {} to {}", fromdate, todate);
            return new ResponseEntity<>(new SuccessResponse("SUCCESS", analysis), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error performing transaction analysis with date filter from {} to {}: {}", fromdate, todate, ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse("ERROR, Oops!, unable to perform transaction analysis"), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Override
    public ApiResponse<?> getAllTransactionsNoPagination(LocalDate fromdate, LocalDate todate) {
        try {
            log.info("Fetching all transactions with no pagination for the date range from {} to {}", fromdate, todate);

            Map<String, Object> response = new HashMap<>();

            List<WalletTransaction> transaction = walletTransactionRepository.findByAllTransactions(fromdate, todate);
            response.put("transaction", transaction);

            if (transaction.isEmpty()) {
                log.warn("No transactions found for the date range from {} to {}", fromdate, todate);
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO RECORD FOUND", null);
            }

            log.info("Transactions fetched successfully with no pagination for the date range from {} to {}", fromdate, todate);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION LIST SUCCESSFULLY", response);
        } catch (Exception ex) {
            log.error("Error fetching all transactions with no pagination for the date range from {} to {}: {}", fromdate, todate, ex.getMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "Oops!, unable to fetch transactions", null);

        }
    }


    @Override
    public ApiResponse<?> getAllTransactionsByAccountNo(LocalDate fromdate, LocalDate todate, String accountNo) {
        try {
            log.info("Fetching all transactions by account number {} within the date range from {} to {}", accountNo, fromdate, todate);

            Map<String, Object> response = new HashMap<>();
            List<TransactionDTO> transactions = new ArrayList<>();

            List<WalletTransaction> transaction = walletTransactionRepository.findByAllTransactionsWithDateRangeaAndAccount(fromdate, todate, accountNo);

            for (WalletTransaction transList : transaction) {
                String transDate = transList.getTranDate().toString();
                TransactionDTO trans = new TransactionDTO(transList, transList.getAcctNum(), transDate);
                transactions.add(trans);
            }

            response.put("transactions", transactions);

            if (transaction.isEmpty()) {
                log.warn("No transactions found for account number {} within the date range from {} to {}", accountNo, fromdate, todate);
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO RECORD FOUND", null);
            }

            log.info("Transactions fetched successfully for account number {} within the date range from {} to {}", accountNo, fromdate, todate);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION LIST SUCCESSFULLY", response);

        } catch (Exception ex) {
            log.error("Error fetching all transactions by account number {}: {}", accountNo, ex.getMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "Oops!, unable to fetch transactions", null);
        }
    }

    @Override
    public ApiResponse<?> fetchUserTransactionsByReferenceNumber(String referenceNumber) {
        try {
            log.info("Fetching transactions by reference number: {}", referenceNumber);

            Optional<List<WalletTransaction>> transactionList = walletTransactionRepository.findByReference(referenceNumber);

            if (!transactionList.isPresent()) {
                log.warn("Transactions not found for reference number: {}", referenceNumber);
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Transaction not found", null);
            }

            log.info("Transactions fetched successfully for reference number: {}", referenceNumber);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "Success", transactionList.get());
        } catch (Exception ex) {
            log.error("Error fetching transactions by reference number {}: {}", referenceNumber, ex.getMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "Oops!, unable to fetch transactions", null);
        }
    }

    @Override
    public ApiResponse<?> fetchMerchantTransactionTqs(HttpServletRequest request, String token, String accountNumber, String referenceNumber) {
        try {
            log.info("Fetching merchant transactions with account number: {}, and reference number: {}", accountNumber, referenceNumber);

            TokenCheckResponse tokenCheckResponse = authProxy.getUserDataToken(token, request.getHeader(SecurityConstants.CLIENT_ID), request.getHeader(SecurityConstants.CLIENT_TYPE));

            if (!tokenCheckResponse.isStatus())
                return new ApiResponse<>(false, ApiResponse.Code.UNAUTHORIZED, "UNAUTHORIZED", null);

            WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);

            if (account == null)
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVALID ACCOUNT NO", null);

            WalletUser walletUser = walletUserRepository.findByAccount(account);

            if (walletUser == null)
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "ACCOUNT PROFILE NOT FOUND", null);

            String acctUserId = String.valueOf(tokenCheckResponse.getData().getId());
            String walletUserId = String.valueOf(walletUser.getUserId());

            if (!acctUserId.equals(walletUserId))
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "YOU LACK CREDENTIALS TO PERFORM THIS ACTION", null);

            Optional<List<WalletTransaction>> transactionList = walletTransactionRepository.findAllByPaymentReferenceAndAcctNum(referenceNumber, accountNumber);

            if (!transactionList.isPresent())
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION NOT FOUND", null);

            log.info("Merchant transactions fetched successfully with token: {}, account number: {}, and reference number: {}", token, accountNumber, referenceNumber);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", transactionList.get());
        } catch (Exception ex) {
            log.error("Error fetching merchant transactions: {}", ex.getMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "ERROR FETCHING TRANSACTION", null);
        }
    }

    @Override
    public ApiResponse<?> fetchTransactionsByReferenceNumberAndAccountNumber(String accountNumber, String referenceNumber) {
        try {
            log.info("Fetching transactions by reference number: {} and account number: {}", referenceNumber, accountNumber);

            Optional<WalletTransaction> transaction = walletTransactionRepository.findFirstByAcctNumAndPaymentReference(accountNumber, referenceNumber);
            if (!transaction.isPresent()) {
                log.warn("Transaction not found for account number: {} and reference number: {}", accountNumber, referenceNumber);
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Transaction not found", null);
            }

            log.info("Found transaction: {}", transaction.get());
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "Success", transaction.get());
        } catch (Exception ex) {
            log.error("Error fetching transactions by reference number and account number: {}", ex.getMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "Oops!, unable to fetch transaction", null);
        }
    }


    @Override
    public ApiResponse<CustomerStatement> accountstatementReport2(Date fromdate, Date todate, String acctNo) {
        try {
            log.info("Generating account statement report for account number: {}", acctNo);

            CustomerStatement custStatement = new CustomerStatement();

            //get account details
            WalletAccount account = walletAccountRepository.findByAccountNo(acctNo);
            if (account == null) {
                log.warn("Invalid account number: {}", acctNo);
                return new ApiResponse<>(false, -1, "Invalid Account Number", null);
            }
            log.info("Found wallet account ---->>> {}", account);
            LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // get opening balance
            BigDecimal debit = walletTransactionRepository.allDebitAndCredit(fromDate, acctNo, "D");
            BigDecimal credit = walletTransactionRepository.allDebitAndCredit(fromDate, acctNo, "C");

            BigDecimal openBal = credit == null ? BigDecimal.ZERO : credit.subtract(debit == null ? BigDecimal.ZERO : debit);
            BigDecimal blockedAmount ;
            if (account.getBlockAmount() == null) {
                blockedAmount = BigDecimal.valueOf(0);
            }
            else {
                blockedAmount = account.getBlockAmount();
            }

            List<AccountStatement> tran = new ArrayList<>();
            List<WalletTransaction> transaction = walletTransactionRepository.transList(fromDate, toDate, acctNo);
            log.info("Transaction history: {}", transaction);
            BigDecimal curBal = BigDecimal.ZERO;
            BigDecimal deposit = BigDecimal.ZERO;
            BigDecimal with = BigDecimal.ZERO;
            BigDecimal clearedBal = new BigDecimal(account.getClr_bal_amt());
            BigDecimal unclearedBal = new BigDecimal(account.getUn_clr_bal_amt());
            for (WalletTransaction transList : transaction) {
                AccountStatement state = new AccountStatement();

                curBal = (tran.isEmpty()
                        ? (transList.getPartTranType().equalsIgnoreCase("D")
                        ? openBal.subtract(transList.getTranAmount())
                        : openBal.add(transList.getTranAmount()))
                        : (transList.getPartTranType().equalsIgnoreCase("D")
                        ? curBal.subtract(transList.getTranAmount())
                        : curBal.add(transList.getTranAmount())));
                state.setBalance(curBal);

                state.setDate(transList.getTranDate().toString());
                state.setReceiver(transList.getReceiverName());
                state.setSender(transList.getSenderName());
                state.setValueDate(transList.getUpdatedAt().toString());
                state.setDeposits(transList.getPartTranType().equalsIgnoreCase("C") ? transList.getTranAmount().toString() : "");
                state.setWithdrawals(transList.getPartTranType().equalsIgnoreCase("D") ? transList.getTranAmount().toString() : "");
                with = transList.getPartTranType().equalsIgnoreCase("D") ? with.add(transList.getTranAmount()) : with;

                deposit = transList.getPartTranType().equalsIgnoreCase("C") ? deposit.add(transList.getTranAmount()) : deposit;
                state.setRef(transList.getPaymentReference());
                state.setDescription(transList.getTranNarrate());
                tran.add(state);
            }

            BigDecimal closingBalance = openBal.add(deposit).subtract(with);
            BigDecimal totalClosingBalance = blockedAmount.add(closingBalance);

            custStatement.setAccountName(account.getAcct_name());
            custStatement.setAccountNumber(acctNo);
            custStatement.setClearedal(clearedBal.setScale(2, RoundingMode.HALF_EVEN));
            custStatement.setUnclearedBal(unclearedBal.setScale(2, RoundingMode.HALF_EVEN));
            custStatement.setOpeningBal(openBal);
            custStatement.setTransaction(tran);
            custStatement.setClosingBal(totalClosingBalance);
            custStatement.setBlockedAmount(blockedAmount);

            log.info("Generated account statement: {}", custStatement);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION LIST SUCCESSFULLY", custStatement);

        } catch (Exception e) {
            log.error("Error generating account statement: {}", e.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "Error occurred", null);
        }
    }

}
