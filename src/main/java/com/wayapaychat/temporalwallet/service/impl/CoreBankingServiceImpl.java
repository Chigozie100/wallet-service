package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import com.wayapaychat.temporalwallet.util.Constant;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.waya.security.auth.pojo.UserIdentityData;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AccountSumary;
import com.wayapaychat.temporalwallet.dto.ExternalCBAResponse;
import com.wayapaychat.temporalwallet.dto.MifosTransaction;
import com.wayapaychat.temporalwallet.dto.MifosTransfer;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.CBATransaction;
import com.wayapaychat.temporalwallet.pojo.CreateAccountData;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.proxy.MifosWalletProxy;
import com.wayapaychat.temporalwallet.repository.UserPricingRepository;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.response.ExternalCBAAccountCreationResponse;
import com.wayapaychat.temporalwallet.service.CoreBankingService;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import com.wayapaychat.temporalwallet.util.Util;

import lombok.extern.slf4j.Slf4j;

import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.UserPricing;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.CBAAction;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.EventCharge;
import com.wayapaychat.temporalwallet.enumm.PriceCategory;
import com.wayapaychat.temporalwallet.enumm.ProductPriceStatus;
import com.wayapaychat.temporalwallet.enumm.ProviderType;
import com.wayapaychat.temporalwallet.enumm.ExternalCBAResponseCodes;
import com.wayapaychat.temporalwallet.enumm.ResponseCodes;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.enumm.WalletTransStatus;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.notification.CustomNotification;
import com.wayapaychat.temporalwallet.pojo.TransactionReport;

@Service
@Slf4j
public class CoreBankingServiceImpl implements CoreBankingService {

    private final SwitchWalletService switchWalletService;
    private final WalletTransAccountRepository walletTransAccountRepository;
    private final WalletAccountRepository walletAccountRepository;
    private final WalletEventRepository walletEventRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MifosWalletProxy mifosWalletProxy;
    private final TemporalWalletDAO tempwallet;
    private final CustomNotification customNotification;
    private final UserPricingRepository userPricingRepository;
    private final TransactionCountService transactionCountService;
    private final TokenImpl tokenImpl;

    @Autowired
    public CoreBankingServiceImpl(SwitchWalletService switchWalletService,
            WalletTransAccountRepository walletTransAccountRepository, WalletAccountRepository walletAccountRepository,
            WalletEventRepository walletEventRepository, WalletTransactionRepository walletTransactionRepository,
            MifosWalletProxy mifosWalletProxy, TemporalWalletDAO tempwallet, CustomNotification customNotification,
            UserPricingRepository userPricingRepository, TransactionCountService transactionCountService,
            TokenImpl tokenImpl) {
        this.switchWalletService = switchWalletService;
        this.walletTransAccountRepository = walletTransAccountRepository;
        this.walletAccountRepository = walletAccountRepository;
        this.walletEventRepository = walletEventRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.mifosWalletProxy = mifosWalletProxy;
        this.tempwallet = tempwallet;
        this.customNotification = customNotification;
        this.userPricingRepository = userPricingRepository;
        this.transactionCountService = transactionCountService;
        this.tokenImpl = tokenImpl;
    }

    @Override
    public ResponseEntity<?> externalCBACreateAccount(WalletUser accountOwnerUser, WalletAccount accountDetails,
            Provider provider) {
        log.info("externalCBACreateAccount: {} ", accountDetails.getAccountNo());
        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse(ResponseCodes.NO_PROVIDER.getValue()),
                HttpStatus.BAD_REQUEST);

        ExternalCBAAccountCreationResponse externalResponse = null;
        CreateAccountData createAccountRequest = new CreateAccountData();
        createAccountRequest.setAccountNumber(accountDetails.getNubanAccountNo());
        createAccountRequest.setEmail(accountOwnerUser.getEmailAddress());
        createAccountRequest.setFirstName(accountOwnerUser.getFirstName());
        createAccountRequest.setMobileNumber(accountOwnerUser.getMobileNo());
        createAccountRequest.setLastName(accountOwnerUser.getLastName());
        createAccountRequest.setAccountName(accountDetails.getAcct_name());
        createAccountRequest.setProduct(accountDetails.getAcct_ownership());

        try {
            if (ProviderType.MIFOS.equalsIgnoreCase(provider.getName())) {
                externalResponse = mifosWalletProxy.createAccount(createAccountRequest);
            } else {
                externalResponse = new ExternalCBAAccountCreationResponse(ExternalCBAResponseCodes.R_00);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (externalResponse == null) {
            return response;
        }

        if (!ExternalCBAResponseCodes.R_00.getRespCode().equals(externalResponse.getResponseCode())) {
            return response;
        }

        return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> createAccount(WalletUser userInfo, WalletAccount sAcct) {
        log.info("createAccount: {} ", sAcct.getAccountNo());
        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.NO_PROVIDER.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> response = externalCBACreateAccount(userInfo, sAcct, provider);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("External CBA failed to process crear account:{} ", sAcct);
            return response;
        }

        walletAccountRepository.save(sAcct);

        return new ResponseEntity<>(new SuccessResponse("Account Created Successfully.", sAcct), HttpStatus.CREATED);
    }

    public ResponseEntity<?> getAccountDetails(String accountNo) {
        ResponseEntity<?> response = securityCheckOwner(accountNo);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNo);
        if (!account.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new SuccessResponse("Wallet", account.get()), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> creditAccount(CBAEntryTransaction transactionPojo) {
        log.info("Processing credit transaction {}", transactionPojo.toString());

        try {
            WalletAccount accountCredit = walletAccountRepository.findByAccountNo(transactionPojo.getAccountNo());

            double cumbalCrAmt = accountCredit.getCum_cr_amt() + transactionPojo.getAmount().doubleValue();
            accountCredit.setLast_tran_id_dr(transactionPojo.getTranId());
            accountCredit.setCum_cr_amt(cumbalCrAmt);
            accountCredit.setLast_tran_date(LocalDate.now());

            double unClrbalAmt = accountCredit.getCum_cr_amt() - accountCredit.getCum_dr_amt();
            accountCredit.setClr_bal_amt(Precision.round(unClrbalAmt - accountCredit.getLien_amt(), 2));
            accountCredit.setUn_clr_bal_amt(Precision.round(unClrbalAmt, 2));
            walletAccountRepository.saveAndFlush(accountCredit);

            WalletTransaction tranCredit = new WalletTransaction(transactionPojo.getSessionID(),
                    transactionPojo.getTranId(),
                    accountCredit.getAccountNo(), transactionPojo.getAmount(),
                    transactionPojo.getTranType(), transactionPojo.getTranNarration(), LocalDate.now(),
                    accountCredit.getAcct_crncy_code(), "C",
                    accountCredit.getGl_code(), transactionPojo.getPaymentReference(),
                    String.valueOf(transactionPojo.getUserToken().getId()), transactionPojo.getUserToken().getEmail(),
                    transactionPojo.getTranPart(), transactionPojo.getTransactionCategory(),
                    transactionPojo.getSenderName(), transactionPojo.getReceiverName());
            walletTransactionRepository.saveAndFlush(tranCredit);

            CompletableFuture.runAsync(() -> sendTransactionNotification(Constant.CREDIT_TRANSACTION_ALERT,
                    accountCredit.getAcct_name(), transactionPojo, accountCredit.getClr_bal_amt(), "CR"));

            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.SUCCESSFUL_CREDIT.getValue()),
                    HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.FAILED_CREDIT.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<?> debitAccount(CBAEntryTransaction transactionPojo) {
        log.info("Processing debit transaction {}", transactionPojo.toString());
        try {
            WalletAccount accountDebit = walletAccountRepository.findByAccountNo(transactionPojo.getAccountNo());

            double cumbalDrAmt = accountDebit.getCum_dr_amt() + transactionPojo.getAmount().doubleValue();
            // lien of same amount debited is also removed
            double lienAmt = accountDebit.getLien_amt() - transactionPojo.getAmount().doubleValue();
            if (lienAmt <= 0) {
                accountDebit.setLien_reason("");
                lienAmt = 0;
            }

            accountDebit.setLien_amt(lienAmt);
            accountDebit.setLast_tran_id_dr(transactionPojo.getTranId());
            accountDebit.setCum_dr_amt(cumbalDrAmt);
            accountDebit.setLast_tran_date(LocalDate.now());

            double unClrbalAmt = accountDebit.getCum_cr_amt() - accountDebit.getCum_dr_amt();
            accountDebit.setClr_bal_amt(Precision.round(unClrbalAmt - lienAmt, 2));
            accountDebit.setUn_clr_bal_amt(Precision.round(unClrbalAmt, 2));
            walletAccountRepository.saveAndFlush(accountDebit);

            WalletTransaction tranDebit = new WalletTransaction(transactionPojo.getSessionID(),
                    transactionPojo.getTranId(),
                    accountDebit.getAccountNo(), transactionPojo.getAmount(),
                    transactionPojo.getTranType(), transactionPojo.getTranNarration(), LocalDate.now(),
                    accountDebit.getAcct_crncy_code(), "D",
                    accountDebit.getGl_code(), transactionPojo.getPaymentReference(),
                    String.valueOf(transactionPojo.getUserToken().getId()), transactionPojo.getUserToken().getEmail(),
                    transactionPojo.getTranPart(), transactionPojo.getTransactionCategory(),
                    transactionPojo.getSenderName(), transactionPojo.getReceiverName());
            walletTransactionRepository.saveAndFlush(tranDebit);

            CompletableFuture.runAsync(() -> sendTransactionNotification(Constant.DEBIT_TRANSACTION_ALERT,
                    accountDebit.getAcct_name(), transactionPojo, accountDebit.getClr_bal_amt(), "DR"));

            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.SUCCESSFUL_DEBIT.getValue()),
                    HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.FAILED_DEBIT.getValue()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> processTransaction(TransferTransactionDTO transferTransactionRequestData,
            String channelEventId, HttpServletRequest request) {
        log.info("Processing transfer transaction {}", transferTransactionRequestData.toString());

        if (Objects.isNull(transferTransactionRequestData.getBeneficiaryName())) {
            transferTransactionRequestData.setBeneficiaryName("");
        }

        if (Objects.isNull(transferTransactionRequestData.getSenderName())) {
            transferTransactionRequestData.setSenderName("");
        }

        if (transferTransactionRequestData.getDebitAccountNumber()
                .equals(transferTransactionRequestData.getBenefAccountNumber())) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.SAME_ACCOUNT.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (walletAccountRepository.countAccount(transferTransactionRequestData.getBenefAccountNumber()) < 1) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_BENEFICIARY_ACCOUNT.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> response = securityCheck(transferTransactionRequestData.getDebitAccountNumber(),
                transferTransactionRequestData.getAmount(), request);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.NO_PROVIDER.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        MyData userData = (MyData) response.getBody();
        String transitAccount = getEventAccountNumber(channelEventId);
        String customerDepositGL = getEventAccountNumber(EventCharge.WAYATRAN.name());
        if (transitAccount == null || customerDepositGL == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        BigDecimal chargeAmount = computeTransactionFee(transferTransactionRequestData.getDebitAccountNumber(),
                transferTransactionRequestData.getAmount(), channelEventId);
        BigDecimal vatAmount = computeVatFee(chargeAmount, channelEventId);
        String customerAccount;
        String receiverName = transferTransactionRequestData.getBeneficiaryName();
        String senderName = transferTransactionRequestData.getSenderName();

        Long tranId = logTransaction(receiverName, senderName, transferTransactionRequestData.getDebitAccountNumber(),
                transferTransactionRequestData.getBenefAccountNumber(),
                transferTransactionRequestData.getAmount(), chargeAmount, vatAmount,
                transferTransactionRequestData.getTransactionCategory(),
                transferTransactionRequestData.getTranCrncy(), channelEventId, WalletTransStatus.PENDING);
        if (tranId == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (transferTransactionRequestData.getDebitAccountNumber().length() > 10
                && transferTransactionRequestData.getBenefAccountNumber().length() > 10) {
            response = processCBATransactionGLDoubleEntryWithTransit(
                    new CBATransaction(senderName, receiverName, userData,
                            transferTransactionRequestData.getPaymentReference(), transitAccount,
                            transferTransactionRequestData.getBenefAccountNumber(),
                            transferTransactionRequestData.getDebitAccountNumber(), null,
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(),
                            chargeAmount, vatAmount, provider, channelEventId, tranId,
                            CBAAction.MOVE_GL_TO_GL));
            customerAccount = transferTransactionRequestData.getDebitAccountNumber();
        } else if (transferTransactionRequestData.getDebitAccountNumber().length() > 10
                && transferTransactionRequestData.getBenefAccountNumber().length() == 10) {
            response = processCBACustomerDepositTransactionWithDoubleEntryTransit(
                    new CBATransaction(senderName, receiverName, userData,
                            transferTransactionRequestData.getPaymentReference(), transitAccount,
                            customerDepositGL, transferTransactionRequestData.getDebitAccountNumber(),
                            transferTransactionRequestData.getBenefAccountNumber(),
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(),
                            BigDecimal.valueOf(0), BigDecimal.valueOf(0), provider, channelEventId,
                            tranId, CBAAction.DEPOSIT));
            customerAccount = transferTransactionRequestData.getBenefAccountNumber();
        } else if (transferTransactionRequestData.getDebitAccountNumber().length() == 10
                && transferTransactionRequestData.getBenefAccountNumber().length() > 10) {

            response = processCBACustomerWithdrawTransactionWithDoubleEntryTransit(
                    new CBATransaction(senderName, receiverName, userData,
                            transferTransactionRequestData.getPaymentReference(), transitAccount,
                            transferTransactionRequestData.getBenefAccountNumber(), customerDepositGL,
                            transferTransactionRequestData.getDebitAccountNumber(),
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(),
                            chargeAmount, vatAmount, provider, channelEventId, tranId,
                            CBAAction.WITHDRAWAL));
            customerAccount = transferTransactionRequestData.getDebitAccountNumber();
        } else {
            response = processCBACustomerTransferTransactionWithDoubleEntryTransit(
                    new CBATransaction(senderName, receiverName, userData,
                            transferTransactionRequestData.getPaymentReference(), transitAccount,
                            transferTransactionRequestData.getBenefAccountNumber(),
                            transferTransactionRequestData.getDebitAccountNumber(),
                            customerDepositGL,
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(),
                            chargeAmount, vatAmount, provider, channelEventId, tranId,
                            CBAAction.MOVE_CUSTOMER_TO_CUSTOMER));
            customerAccount = transferTransactionRequestData.getDebitAccountNumber();
        }

        WalletTransStatus transactionStatus = response.getStatusCode().is2xxSuccessful() ? WalletTransStatus.SUCCESSFUL
                : WalletTransStatus.REVERSED;
        updateTransactionLog(tranId, transactionStatus);

        Optional<List<WalletTransaction>> transaction = walletTransactionRepository
                .findByReferenceAndAccount(transferTransactionRequestData.getPaymentReference(), customerAccount);

        if (transaction.isEmpty()) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue(), transaction),
                HttpStatus.CREATED);

    }

    @Override
    public ResponseEntity<?> processTransactionReversal(ReverseTransactionDTO reverseDTO, HttpServletRequest request) {
        log.info("processTransactionReversal TranId:{} ", reverseDTO.getTranId());
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (ObjectUtils.isEmpty(userToken)) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.NO_PROVIDER.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<List<WalletTransaction>> transactioonList = walletTransactionRepository
                .findByTranIdIgnoreCase(reverseDTO.getTranId());
        if (transactioonList.isEmpty()) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (isCustomerTransaction(transactioonList.get())) {
            reverseDTO.setTranId(transactioonList.get().get(0).getPaymentReference());
            return processCustomerTransactionReversalByRef(reverseDTO, request);
        } else {
            return reverseGLTransaction(userToken, provider, transactioonList.get());
        }

    }

    @Override
    public ResponseEntity<?> reverseCustomerTransaction(MyData userToken, Provider provider,
            WalletTransaction walletTransaction) {
        log.info("reverseCustomerTransaction account:{} amount:{} type:{} ",
                walletTransaction.getAcctNum(), walletTransaction.getTranAmount(), walletTransaction.getTranType());

        if (!"D".equalsIgnoreCase(walletTransaction.getPartTranType())) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.TRANSACTION_NOT_SUPPORTED.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        // reverse customer transaction
        CBATransaction reversalTransaction = new CBATransaction(
                walletTransaction.getSenderName(), walletTransaction.getReceiverName(),
                userToken, walletTransaction.getPaymentReference(),
                null, null, null,
                walletTransaction.getAcctNum(), "Revsrl ".concat(walletTransaction.getTranNarrate()),
                CategoryType.REVERSAL.name(),
                TransactionTypeEnum.TRANSFER.name(), walletTransaction.getTranAmount(),
                new BigDecimal(0), new BigDecimal(0), provider, null,
                walletTransaction.getRelatedTransId(),
                "D".equalsIgnoreCase(walletTransaction.getPartTranType()) ? CBAAction.DEPOSIT : CBAAction.WITHDRAWAL);

        return processCBATransactionCustomerEntry(reversalTransaction);

    }

    @Override
    public ResponseEntity<?> reverseGLTransaction(MyData userToken, Provider provider,
            List<WalletTransaction> walletTransaction) {
        log.info("reverseGLTransaction account:{} amount:{} type:{} ",
                walletTransaction.get(0).getAcctNum(), walletTransaction.get(0).getTranAmount(),
                walletTransaction.get(0).getTranType());

        if (walletTransaction.size() != 2) {
            log.error("Invalid  GL Posting or already reversed");
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        CBATransaction reversalTransaction = new CBATransaction(
                walletTransaction.get(0).getSenderName(), walletTransaction.get(0).getReceiverName(),
                userToken, walletTransaction.get(0).getPaymentReference(),
                null, null, null,
                walletTransaction.get(0).getAcctNum(), "Revsrl ".concat(walletTransaction.get(0).getTranNarrate()),
                CategoryType.REVERSAL.name(),
                TransactionTypeEnum.TRANSFER.name(), walletTransaction.get(0).getTranAmount(),
                new BigDecimal(0), new BigDecimal(0), provider, null,
                walletTransaction.get(0).getRelatedTransId(), CBAAction.MOVE_GL_TO_GL);

        if ("C".equalsIgnoreCase(walletTransaction.get(0).getPartTranType())
                && "D".equalsIgnoreCase(walletTransaction.get(1).getPartTranType())) {
            reversalTransaction.setDebitGLAccount(walletTransaction.get(0).getAcctNum());
            reversalTransaction.setCreditGLAccount(walletTransaction.get(1).getAcctNum());
        } else {
            reversalTransaction.setDebitGLAccount(walletTransaction.get(1).getAcctNum());
            reversalTransaction.setCreditGLAccount(walletTransaction.get(0).getAcctNum());
        }

        return processCBATransactionGLDoubleEntry(reversalTransaction);

    }

    @Override
    public boolean isCustomerTransaction(List<WalletTransaction> list) {
        return list.size() == 1 && list.stream().anyMatch((accTrans) -> {
            return !accTrans.getAcctNum().contains("NGN");
        });
    }

    @Override
    public ResponseEntity<?> processCustomerTransactionReversalByRef(ReverseTransactionDTO reverseDTO, HttpServletRequest request) {
        log.info("processCustomerTransactionReversalByRef TranId:{} ", reverseDTO.getTranId());
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (ObjectUtils.isEmpty(userToken)) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.NO_PROVIDER.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<List<WalletTransaction>> transactioonList = walletTransactionRepository
                .findByReference(reverseDTO.getTranId());
        if (transactioonList.isEmpty()) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        List<WalletTransaction> customerWalletTransaction = transactioonList.get().stream()
                .filter(accTrans -> !accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());

        List<WalletTransaction> glWalletTransaction = transactioonList.get().stream()
                .filter(accTrans -> accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());

        if (customerWalletTransaction.size() != 1
                || (glWalletTransaction.size() % 2 != 0 && glWalletTransaction.size() <= 12)) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.TRANSACTION_NOT_SUPPORTED.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> response = reverseCustomerTransaction(userToken, provider,
                customerWalletTransaction.get(0));
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        updateTransactionLog(Long.valueOf(customerWalletTransaction.get(0).getRelatedTransId()), WalletTransStatus.REVERSED);

        Map<String, List<WalletTransaction>> glPostings = glWalletTransaction.stream()
                .collect(Collectors.groupingBy(WalletTransaction::getTranId));

        for (Map.Entry<String, List<WalletTransaction>> glPosting : glPostings.entrySet()) {
            if (glPosting.getValue().size() != 2) {
                log.info("invalid GL Transaction:{} ", glPosting.getValue());
                continue;
            }
            response = reverseGLTransaction(userToken, provider, glPosting.getValue());
        }

        return response;

    }

    @Override
    public String getEventAccountNumber(String channelEventId) {

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(channelEventId);
        if (!eventInfo.isPresent()) {
            log.error("no event found for transaction category {}", channelEventId);
            return null;
        }

        Optional<WalletAccount> accountDebitTeller = walletAccountRepository
                .findByUserPlaceholder(eventInfo.get().getPlaceholder(), eventInfo.get().getCrncyCode(), "0000");
        if (!accountDebitTeller.isPresent()) {
            log.error("no transit account found for transaction channel EventId {}", channelEventId);
            return null;
        }

        return accountDebitTeller.get().getAccountNo();

    }

    @Override
    public BigDecimal computeTotalTransactionFee(String accountNumber, BigDecimal amount, String eventId) {
        BigDecimal priceAmount = computeTransactionFee(accountNumber, amount, eventId);
        if (priceAmount.doubleValue() <= 0) {
            return priceAmount;
        }

        // add vat to fee
        BigDecimal vatAmount = computeVatFee(priceAmount, eventId);
        priceAmount = BigDecimal.valueOf(Precision.round(priceAmount.doubleValue() + vatAmount.doubleValue(), 2));
        log.info(" Transaction Total Fee {}", priceAmount.doubleValue());

        return priceAmount;
    }

    @Override
    public BigDecimal computeTransactionFee(String accountNumber, BigDecimal amount, String eventId) {
        BigDecimal priceAmount = new BigDecimal(0);

        if (ObjectUtils.isEmpty(amount)) {
            return priceAmount;
        }

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
        if (!eventInfo.isPresent()) {
            return priceAmount;
        }

        AccountSumary account = tempwallet.getAccountSumaryLookUp(accountNumber);
        if (account == null) {
            return priceAmount;
        }

        UserPricing userPricingOptional = userPricingRepository.findDetailsByCode(account.getUId(), eventId)
                .orElse(null);
        if (userPricingOptional == null) {
            return priceAmount;
        }

        priceAmount = userPricingOptional.getStatus().equals(ProductPriceStatus.GENERAL)
                ? userPricingOptional.getGeneralAmount() // Get general amount
                : userPricingOptional.getCustomAmount(); // Get custom amount

        priceAmount = userPricingOptional.getPriceType().equals(PriceCategory.FIXED)
                ? priceAmount // compute percentage
                : BigDecimal.valueOf(Precision.round(amount.doubleValue() * priceAmount.doubleValue() / 100, 2)); // Get
        // fixed
        // amount

        if (priceAmount.doubleValue() <= 0) {
            return priceAmount;
        }

        if (priceAmount.doubleValue() > userPricingOptional.getCapPrice().doubleValue()) {
            priceAmount = userPricingOptional.getCapPrice();
        }

        log.info(" Transaction Fee {}", priceAmount.doubleValue());
        return priceAmount;
    }

    @Override
    public BigDecimal computeVatFee(BigDecimal fee, String eventId) {
        BigDecimal vatAmount = new BigDecimal(0);

        if (ObjectUtils.isEmpty(fee)) {
            return vatAmount;
        }

        if (fee.doubleValue() <= 0) {
            return vatAmount;
        }

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
        if (!eventInfo.isPresent()) {
            return vatAmount;
        }

        if (eventInfo.get().getTaxAmt().doubleValue() > 0) {
            vatAmount = BigDecimal
                    .valueOf(Precision.round(fee.doubleValue() * eventInfo.get().getTaxAmt().doubleValue() / 100, 2));
        }

        return vatAmount;
    }

    @Override
    public Long logTransaction(String beneficiaryName, String senderName, String fromAccountNumber,
            String toAccountNumber, BigDecimal amount, BigDecimal chargeAmount, BigDecimal vatAmount,
            String transCategory, String tranCrncy, String eventId, WalletTransStatus status) {

        String code = new Util().generateRandomNumber(9);
        try {
            WalletTransAccount walletTransAccount = new WalletTransAccount();
            walletTransAccount.setCreatedAt(LocalDateTime.now());
            walletTransAccount.setDebitAccountNumber(fromAccountNumber);
            walletTransAccount.setCreditAccountNumber(toAccountNumber);
            walletTransAccount.setTranAmount(amount);
            walletTransAccount.setChargeAmount(chargeAmount);
            walletTransAccount.setVatAmount(vatAmount);
            walletTransAccount.setTranId(code);
            walletTransAccount.setTransactionType(transCategory);
            walletTransAccount.setTranCrncy(tranCrncy);
            walletTransAccount.setEventId(eventId);
            walletTransAccount.setStatus(status);
            walletTransAccount.setSenderName(senderName);
            walletTransAccount.setBeneficiaryName(beneficiaryName);
            return walletTransAccountRepository.save(walletTransAccount).getId();
        } catch (CustomException ex) {
            log.info("logTransaction ::::" + ex.getMessage());
            throw new CustomException("Error in loggin transaction :: " + ex.getMessage(),
                    HttpStatus.EXPECTATION_FAILED);
        }

    }

    @Override
    public void updateTransactionLog(Long tranId, WalletTransStatus status) {
        try {
            Optional<WalletTransAccount> walletTransAccount = walletTransAccountRepository.findById(tranId);
            if (walletTransAccount.isPresent()) {
                WalletTransAccount walletTransAccount1 = walletTransAccount.get();
                walletTransAccount1.setStatus(status);
                walletTransAccountRepository.save(walletTransAccount1);
            }
        } catch (CustomException ex) {
            log.error("An error updateTransactionLog: {}", ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    private String generateSessionId() {
        long randomNum = (long) Math.floor(Math.random() * 9_000_000_000_00L) + 1_000_000_000_00L;
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + Long.toString(randomNum);
    }

    @Override
    public void addLien(WalletAccount account, BigDecimal amount) {

        account.setLien_amt(amount.doubleValue());
        walletAccountRepository.saveAndFlush(account);

    }

    @Override
    public void sendTransactionNotification(String subject, String accountName, CBAEntryTransaction transactionPojo,
            double currentBalance, String tranType) {

        AccountSumary account = tempwallet.getAccountSumaryLookUp(transactionPojo.getAccountNo());
        if (account == null) {
            return;
        }

        transactionCountService.pushTransactionToEventQueue(account, transactionPojo, currentBalance);

        String systemToken = tokenImpl.getToken();
        if (systemToken == null) {
            return;
        }

        String tranDate = LocalDate.now().toString();
        if (transactionPojo.getAccountNo().contains("NGN")) {
            subject = "GL Posting ".concat(subject);
        }

        StringBuilder transactionType = new StringBuilder();
        if ("CR".equalsIgnoreCase(tranType)) {
            transactionType.append("Credit");
        } else {
            transactionType.append("Debit");
        }

        String notifyEmail = !ObjectUtils.isEmpty(account.getNotifyEmail()) ? account.getNotifyEmail()
                : account.getEmail();
        if (!ObjectUtils.isEmpty(notifyEmail)) {
            StringBuilder _email_message = new StringBuilder();
            _email_message.append(String.format("A %s transaction has occurred with reference: %s ", tranType,
                    transactionPojo.getTranId()));
            _email_message.append("on your account see details below. \n");
            _email_message
                    .append(String.format("Amount: %s", Precision.round(transactionPojo.getAmount().doubleValue(), 2)));
            _email_message.append("\n");
            _email_message.append(String.format("Date: %s", tranDate));
            _email_message.append("\n");
            _email_message.append(String.format("Narration :%s ", transactionPojo.getTranNarration()));

            customNotification.pushTranEMAIL(subject, systemToken, accountName,
                    account.getEmail(), _email_message.toString(), account.getUId(),
                    String.valueOf(Precision.round(transactionPojo.getAmount().doubleValue(), 2)),
                    transactionPojo.getTranId(), tranDate, transactionPojo.getTranNarration(), account.getAccountNo(),
                    transactionType.append("alert").toString().toUpperCase(),
                    String.valueOf(Precision.round(currentBalance, 2)));
        }

        if (!ObjectUtils.isEmpty(account.getPhone())) {
            StringBuilder _sms_message = new StringBuilder();
            _sms_message.append(String.format("Tnx: %s", transactionType.toString()));
            _sms_message.append("\n");
            _sms_message.append(String.format("Acct: %s", transactionPojo.getAccountNo()));
            _sms_message.append("\n");
            _sms_message.append(String.format("Amt: %s %s",
                    Precision.round(transactionPojo.getAmount().doubleValue(), 2), tranType));
            _sms_message.append("\n");
            _sms_message.append(String.format("Tran ID: %s", transactionPojo.getTranId()));
            _sms_message.append("\n");
            _sms_message.append(String.format("Date: %s", tranDate));
            _sms_message.append("\n");
            _sms_message.append(String.format("Naration: %s", transactionPojo.getTranNarration()));
            _sms_message.append("\n");
            _sms_message.append(String.format("Avail Bal: %s", Precision.round(currentBalance, 2)));

            customNotification.pushWayaSMS(systemToken, accountName, account.getPhone(),
                    _sms_message.toString(), account.getUId(), account.getEmail());
        }

    }

    @Override
    public ResponseEntity<?> securityCheckOwner(String accountNumber) {
        log.info("securityCheck Ownership:: " + accountNumber);
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (userToken == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        AccountSumary account = tempwallet.getAccountSumaryLookUp(accountNumber);
        log.info("AccountSumary :: " + account);
        if (account == null) {
            return new ResponseEntity<>(
                    new ErrorResponse(
                            String.format("%s  %s", ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber)),
                    HttpStatus.BAD_REQUEST);
        }

        boolean isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_OWNER"::equalsIgnoreCase);
        isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_APP"::equalsIgnoreCase) ? true : isWriteAdmin;
        boolean isOwner = Long.compare(account.getUId(), userToken.getId()) == 0;

        if (!isOwner && !isWriteAdmin) {
            log.error("owner check {} {}", isOwner, isWriteAdmin);
            return new ResponseEntity<>(new ErrorResponse(String.format("%s %s %s %s",
                    ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber, isOwner, isWriteAdmin)),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(account, HttpStatus.ACCEPTED);

    }

    @Override
    public ResponseEntity<?> securityCheck(String accountNumber, BigDecimal amount, HttpServletRequest request) {
        log.info("securityCheck to debit account{} amount{}", accountNumber, amount);
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (userToken == null) {
            log.error("token validation failed for debiting account{} with amount{}", accountNumber, amount);
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (!tokenImpl.validatePIN(request.getHeader("authorization"), request.getHeader("pin"))) {
            log.error("pin {} validation failed for debiting account {} with amount{}", request.getHeader("pin"),
                    accountNumber, amount);
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_PIN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (amount.doubleValue() <= 0) {
            log.error("amount is less than zero for debiting account{} with amount{}", accountNumber, amount);
            return new ResponseEntity<>(
                    new ErrorResponse(String.format("%s : %s", ResponseCodes.INVALID_AMOUNT.getValue(), amount)),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<WalletAccount> ownerAccount = walletAccountRepository.findByAccount(accountNumber);
        if (!ownerAccount.isPresent()) {
            log.error("ownerAccount not found:: {}", accountNumber);
            return new ResponseEntity<>(
                    new ErrorResponse(
                            String.format("%s %s", ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber)),
                    HttpStatus.BAD_REQUEST);
        }

        AccountSumary account = tempwallet.getAccountSumaryLookUp(accountNumber);
        log.debug("AccountSumary :: {}", account);
        if (account == null) {
            log.error("unable to get AccountSumary :: {}", account);
            return new ResponseEntity<>(
                    new ErrorResponse(
                            String.format("%s %s", ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber)),
                    HttpStatus.BAD_REQUEST);
        }

        double sufficientFunds = ownerAccount.get().getCum_cr_amt() - ownerAccount.get().getCum_dr_amt()
                - ownerAccount.get().getLien_amt() - amount.doubleValue();

        if (sufficientFunds < 0 && ownerAccount.get().getAcct_ownership().equals("C")) {
            log.error("insufficientFunds :: {}", sufficientFunds);
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INSUFFICIENT_FUNDS.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (amount.doubleValue() > Double.parseDouble(account.getDebitLimit())) {
            log.error("Debit limit reached :: {}", account.getDebitLimit());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.DEBIT_LIMIT_REACHED.getValue() + " " + account.getDebitLimit() + " "
                            + ResponseCodes.DEBIT_LIMIT_REACHED_EX.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalTransactionToday = walletTransactionRepository.totalTransactionAmountToday(accountNumber,
                LocalDate.now());
        log.info("totalTransactionToday {}", totalTransactionToday);
        totalTransactionToday = totalTransactionToday == null ? new BigDecimal(0) : totalTransactionToday;
        if (totalTransactionToday.doubleValue() >= Double.parseDouble(account.getDebitLimit())) {
            log.error("Debit limit reached :: {}", account.getDebitLimit());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.DEBIT_LIMIT_REACHED.getValue() + " " + account.getDebitLimit() + " "
                            + ResponseCodes.DEBIT_LIMIT_REACHED_EX.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        boolean isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_OWNER"::equalsIgnoreCase);
        isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_APP"::equalsIgnoreCase) ? true : isWriteAdmin;
        boolean isOwner = Long.compare(account.getUId(), userToken.getId()) == 0;

        if (!isOwner && !isWriteAdmin) {
            log.error("owner check {} {}", isOwner, isWriteAdmin);
            return new ResponseEntity<>(new ErrorResponse(String.format("%s %s %s %s",
                    ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber, isOwner, isWriteAdmin)),
                    HttpStatus.BAD_REQUEST);
        }

        addLien(ownerAccount.get(), amount);

        return new ResponseEntity<>(userToken, HttpStatus.ACCEPTED);

    }

    private String getDebitAccountNumber(CBATransaction cbaTransaction) {
        String account = cbaTransaction.getDebitGLAccount();
        if (cbaTransaction.getAction().equals(CBAAction.MOVE_FROM_TRANSIT)) {
            return cbaTransaction.getTransitGLAccount();
        }
        return account;
    }

    private String getCreditAccountNumber(CBATransaction cbaTransaction) {
        String account = cbaTransaction.getCreditGLAccount();
        if (cbaTransaction.getAction().equals(CBAAction.MOVE_TO_TRANSIT)) {
            return cbaTransaction.getTransitGLAccount();
        }
        return account;
    }

    @Override
    public ResponseEntity<?> processExternalCBATransactionGLDoubleEntry(CBATransaction cbaTransaction,
            boolean reversal) {
        log.info("processExternalCBATransactionDoubleEntry amount:{} ", cbaTransaction.getAmount());
        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                HttpStatus.BAD_REQUEST);

        WalletAccount accountDebit = walletAccountRepository.findByAccountNo(getDebitAccountNumber(cbaTransaction));
        WalletAccount accountCredit = walletAccountRepository.findByAccountNo(getCreditAccountNumber(cbaTransaction));

        MifosTransfer mifosTransfer = new MifosTransfer();
        mifosTransfer.setRequestId(generateSessionId());
        mifosTransfer.setAmount(cbaTransaction.getAmount());
        mifosTransfer.setNarration(cbaTransaction.getNarration());
        mifosTransfer.setTransactionType(TransactionTypeEnum.TRANSFER.getValue());

        mifosTransfer.setDestinationAccountType("SAVINGS");
        mifosTransfer.setSourceAccountType("SAVINGS");

        if (reversal) {
            mifosTransfer.setSourceCurrency(accountCredit.getAcct_crncy_code());
            mifosTransfer.setDestinationCurrency(accountDebit.getAcct_crncy_code());
            mifosTransfer.setDestinationAccountNumber(accountDebit.getNubanAccountNo());
            mifosTransfer.setSourceAccountNumber(accountCredit.getNubanAccountNo());
        } else {
            mifosTransfer.setSourceCurrency(accountDebit.getAcct_crncy_code());
            mifosTransfer.setDestinationCurrency(accountCredit.getAcct_crncy_code());
            mifosTransfer.setDestinationAccountNumber(accountCredit.getNubanAccountNo());
            mifosTransfer.setSourceAccountNumber(accountDebit.getNubanAccountNo());
        }

        ExternalCBAResponse externalResponse = null;

        try {
            if (ProviderType.MIFOS.equalsIgnoreCase(cbaTransaction.getProvider().getName())) {
                externalResponse = mifosWalletProxy.applyGLEntry(mifosTransfer);
            } else {
                externalResponse = new ExternalCBAResponse(ExternalCBAResponseCodes.R_00);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (externalResponse == null) {
            return response;
        }

        if (!ExternalCBAResponseCodes.R_00.getRespCode().equals(externalResponse.getResponseCode())) {
            return response;
        }

        return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> processCBATransactionGLDoubleEntry(CBATransaction cbaTransaction) {
        log.info("processCBATransactionDoubleEntry ");
        if (cbaTransaction.getAmount().doubleValue() <= 0) {
            log.error("Invalid transaction amount:{} ", cbaTransaction.getAmount());
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_AMOUNT.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        String tranId = tempwallet.TransactionGenerate();
        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(cbaTransaction.getType());
        CategoryType tranCategory = CategoryType.valueOf(cbaTransaction.getCategory());
        ResponseEntity<?> response = processExternalCBATransactionGLDoubleEntry(cbaTransaction, false);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("External CBA failed to process transaction amount:{} ", cbaTransaction.getAmount());
            return response;
        }

        try {
            response = debitAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(),
                    cbaTransaction.getSessionID(), tranId,
                    cbaTransaction.getPaymentReference(), tranCategory, getDebitAccountNumber(cbaTransaction),
                    cbaTransaction.getNarration(), cbaTransaction.getAmount(), 1, tranType,
                    cbaTransaction.getSenderName(), cbaTransaction.getReceiverName()));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        try {
            response = creditAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(),
                    cbaTransaction.getSessionID(), tranId,
                    cbaTransaction.getPaymentReference(), tranCategory, getCreditAccountNumber(cbaTransaction),
                    cbaTransaction.getNarration(), cbaTransaction.getAmount(), 2, tranType,
                    cbaTransaction.getSenderName(), cbaTransaction.getReceiverName()));
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        // Reverse debit if credit failed
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Reecersing transaction amount:{} from:{} to:{}", cbaTransaction.getAmount());
            cbaTransaction.setNarration("Revesal: ".concat(cbaTransaction.getNarration()));
            processExternalCBATransactionGLDoubleEntry(cbaTransaction, true);
            creditAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(),
                    cbaTransaction.getSessionID(), tranId,
                    cbaTransaction.getPaymentReference(), tranCategory, getDebitAccountNumber(cbaTransaction),
                    cbaTransaction.getNarration(), cbaTransaction.getAmount(), 2, tranType, cbaTransaction.getSenderName(), cbaTransaction.getReceiverName()));
        }

        return response;
    }

    @Override
    public ResponseEntity<?> processCBATransactionGLDoubleEntryWithTransit(CBATransaction cbaTransaction) {
        log.info("Processing CBA double entry transaction from:{} to:{} using transit:{}",
                cbaTransaction.getDebitGLAccount(), cbaTransaction.getCreditGLAccount(),
                cbaTransaction.getTransitGLAccount());

        cbaTransaction.setAction(CBAAction.MOVE_TO_TRANSIT);
        ResponseEntity<?> response = processCBATransactionGLDoubleEntry(cbaTransaction);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        cbaTransaction.setAction(CBAAction.MOVE_FROM_TRANSIT);
        response = processCBATransactionGLDoubleEntry(cbaTransaction);

        if (!response.getStatusCode().is2xxSuccessful()) {
            cbaTransaction.setNarration("Reversal: ".concat(cbaTransaction.getNarration()));
            cbaTransaction.setAction(CBAAction.REVERSE_FROM_TRANSIT);
            processCBATransactionGLDoubleEntry(cbaTransaction);
        }

        return response;
    }

    @Override
    public ResponseEntity<?> processExternalCBATransactionCustomerEntry(CBATransaction cbaTransaction) {
        log.info("processExternalCBATransactionCustomerEntry amount:{} ", cbaTransaction.getAmount());
        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                HttpStatus.BAD_REQUEST);

        WalletAccount customer = walletAccountRepository.findByAccountNo(cbaTransaction.getCustomerAccount());

        MifosTransaction mifosTransfer = new MifosTransaction();
        mifosTransfer.setRequestId(generateSessionId());
        mifosTransfer.setAccountNumber(customer.getNubanAccountNo());
        mifosTransfer.setCurrency(customer.getAcct_crncy_code());
        mifosTransfer.setAccountType("SAVINGS");
        mifosTransfer.setAmount(BigDecimal.valueOf(Precision.round(cbaTransaction.getAmount().doubleValue()
                + cbaTransaction.getCharge().doubleValue() + cbaTransaction.getVat().doubleValue(), 2)));
        mifosTransfer.setNarration(cbaTransaction.getNarration());
        mifosTransfer.setTransactionType(cbaTransaction.getAction().name());

        ExternalCBAResponse externalResponse = null;

        try {
            if (ProviderType.MIFOS.equalsIgnoreCase(cbaTransaction.getProvider().getName())) {
                externalResponse = mifosWalletProxy.processTransaction(mifosTransfer);
            } else {
                externalResponse = new ExternalCBAResponse(ExternalCBAResponseCodes.R_00);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (externalResponse == null) {
            return response;
        }

        if (!ExternalCBAResponseCodes.R_00.getRespCode().equals(externalResponse.getResponseCode())) {
            return response;
        }

        return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> processCBATransactionCustomerEntry(CBATransaction cbaTransaction) {
        log.info("processCBATransactionCustomerEntry ");
        if (cbaTransaction.getAmount().doubleValue() <= 0) {
            log.error("Invalid transaction amount:{} ", cbaTransaction.getAmount());
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_AMOUNT.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        String tranId = tempwallet.TransactionGenerate();
        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(cbaTransaction.getType());
        CategoryType tranCategory = CategoryType.valueOf(cbaTransaction.getCategory());
        ResponseEntity<?> response = processExternalCBATransactionCustomerEntry(cbaTransaction);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("External CBA failed to process transaction amount:{} ", cbaTransaction.getAmount());
            return response;
        }

        BigDecimal totalAmount = BigDecimal.valueOf(Precision.round(cbaTransaction.getAmount().doubleValue()
                + cbaTransaction.getCharge().doubleValue() + cbaTransaction.getVat().doubleValue(), 2));
        try {
            if (cbaTransaction.getAction().equals(CBAAction.DEPOSIT)) {
                creditAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(),
                        cbaTransaction.getSessionID(), tranId,
                        cbaTransaction.getPaymentReference(), tranCategory, cbaTransaction.getCustomerAccount(),
                        cbaTransaction.getNarration(), totalAmount, 1, tranType, cbaTransaction.getSenderName(), cbaTransaction.getReceiverName()));
            } else {
                response = debitAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(),
                        cbaTransaction.getSessionID(), tranId,
                        cbaTransaction.getPaymentReference(), tranCategory, cbaTransaction.getCustomerAccount(),
                        cbaTransaction.getNarration(), totalAmount, 1, tranType, cbaTransaction.getSenderName(), cbaTransaction.getReceiverName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Reecersing transaction amount:{} from:{} to:{}", cbaTransaction.getAmount());
            cbaTransaction.setNarration("Revesal: ".concat(cbaTransaction.getNarration()));
            cbaTransaction.setAction(cbaTransaction.getAction().equals(CBAAction.WITHDRAWAL) ? CBAAction.DEPOSIT
                    : CBAAction.WITHDRAWAL);
            processExternalCBATransactionCustomerEntry(cbaTransaction);
            return response;
        }

        return response;
    }

    @Override
    public ResponseEntity<?> processCBACustomerDepositTransactionWithDoubleEntryTransit(CBATransaction cbaTransaction) {
        log.info("Processing CBA customer deposit entry transaction customer:{} from:{} to:{} using transit:{}",
                cbaTransaction.getCustomerAccount(),
                cbaTransaction.getDebitGLAccount(), cbaTransaction.getCreditGLAccount(),
                cbaTransaction.getTransitGLAccount());

        ResponseEntity<?> response = processCBATransactionCustomerEntry(cbaTransaction);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        cbaTransaction.setAction(CBAAction.MOVE_GL_TO_GL);
        processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

        /**
         * Todo rever customer deposit on failure
         */
        return response;
    }

    @Override
    public ResponseEntity<?> processCBACustomerWithdrawTransactionWithDoubleEntryTransit(
            CBATransaction cbaTransaction) {
        log.info("Processing CBA customer withdraw entry transaction customer:{} from:{} to:{} using transit:{}",
                cbaTransaction.getCustomerAccount(),
                cbaTransaction.getDebitGLAccount(), cbaTransaction.getCreditGLAccount(),
                cbaTransaction.getTransitGLAccount());
        ResponseEntity<?> response = processCBATransactionCustomerEntry(cbaTransaction);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        cbaTransaction.setAction(CBAAction.MOVE_GL_TO_GL);
        processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

        if (cbaTransaction.getCharge().doubleValue() <= 0) {
            return response;
        }

        log.info("applying charge for transaction on {}", cbaTransaction.getCustomerAccount());
        String chargeCollectionAccount = getEventAccountNumber("INCOME_".concat(cbaTransaction.getChannelEvent()));
        if (chargeCollectionAccount == null || cbaTransaction.getCharge().doubleValue() <= 0) {
            return response;
        }

        cbaTransaction.setCreditGLAccount(chargeCollectionAccount);
        cbaTransaction.setAmount(cbaTransaction.getCharge());
        processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

        log.info("applying VAT for transaction on {}", cbaTransaction.getCustomerAccount());
        String vatCollectionAccount = getEventAccountNumber("VAT_".concat(cbaTransaction.getChannelEvent()));
        if (vatCollectionAccount == null) {
            return response;
        }

        cbaTransaction.setCreditGLAccount(vatCollectionAccount);
        cbaTransaction.setAmount(cbaTransaction.getVat());
        processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

        /**
         * Todo rever customer withdrawal on failure
         */
        return response;
    }

    @Override
    public ResponseEntity<?> processCBACustomerTransferTransactionWithDoubleEntryTransit(
            CBATransaction cbaTransaction) {
        log.info("Processing CBA customer transfer entry transaction depositGL:{} from:{} to:{} using transit:{}",
                cbaTransaction.getCustomerAccount(),
                cbaTransaction.getDebitGLAccount(), cbaTransaction.getCreditGLAccount(),
                cbaTransaction.getTransitGLAccount());
        String debitAccount = cbaTransaction.getDebitGLAccount();
        String creditAccount = cbaTransaction.getCreditGLAccount();
        BigDecimal amount = cbaTransaction.getAmount();
        String eventGL = cbaTransaction.getCustomerAccount();

        cbaTransaction.setAction(CBAAction.WITHDRAWAL);
        cbaTransaction.setCustomerAccount(debitAccount);
        cbaTransaction.setDebitGLAccount(eventGL);
        cbaTransaction.setCreditGLAccount(eventGL);
        ResponseEntity<?> response = processCBACustomerWithdrawTransactionWithDoubleEntryTransit(cbaTransaction);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        cbaTransaction.setAction(CBAAction.DEPOSIT);
        cbaTransaction.setCustomerAccount(creditAccount);
        cbaTransaction.setCharge(BigDecimal.valueOf(0));
        cbaTransaction.setVat(BigDecimal.valueOf(0));
        cbaTransaction.setAmount(amount);
        cbaTransaction.setDebitGLAccount(eventGL);
        cbaTransaction.setCreditGLAccount(eventGL);
        response = processCBACustomerDepositTransactionWithDoubleEntryTransit(cbaTransaction);

        return response;
    }

    private void transactionReport(CBAEntryTransaction transactionPojo) {
        WalletAccount accountCredit = walletAccountRepository.findByAccountNo(transactionPojo.getAccountNo());

        TransactionReport transReport = new TransactionReport();
        transReport.setAccountNo(accountCredit.getAccountNo());
        transReport.setCustomer_type(accountCredit.getUser().isCorporate() ? "B" : "P");
        transReport.setTrans_amt(transactionPojo.getAmount().doubleValue());
        transReport.setTrans_category(transactionPojo.getTransactionCategory().getValue());
        transReport.setTrans_ref(transactionPojo.getTranId());
        transReport.setTrans_date(LocalDate.now());
        transReport.setCum_cr_amt(accountCredit.getCum_cr_amt());
        transReport.setCum_dr_amt(accountCredit.getCum_dr_amt());
        transReport.setRelated_trans_id(transactionPojo.getPaymentReference());
        transactionCountService.transReport(transReport);
    }
}
