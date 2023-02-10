package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AccountSumary;
import com.wayapaychat.temporalwallet.dto.ExternalCBAResponse;
import com.wayapaychat.temporalwallet.dto.MifosTransaction;
import com.wayapaychat.temporalwallet.dto.MifosTransfer;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.CBATransaction;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
import com.wayapaychat.temporalwallet.proxy.MifosWalletProxy;
import com.wayapaychat.temporalwallet.repository.UserPricingRepository;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
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
    public ResponseEntity<?> createAccount(TransactionPojo transactionPojo) {
        /**
         * create on temp
         * create on mifos (is customer - savings, [official GL income-rev,
         * collection-asset, disburs-asset])
         */
        return null;
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

        /**
         * 
         */

        try {
            WalletAccount accountCredit = walletAccountRepository.findByAccountNo(transactionPojo.getAccountNo());

            WalletTransaction tranCredit = new WalletTransaction(transactionPojo.getTranId(),
                    accountCredit.getAccountNo(), transactionPojo.getAmount(),
                    transactionPojo.getTranType(), transactionPojo.getTranNarration(), LocalDate.now(),
                    accountCredit.getAcct_crncy_code(), "C",
                    accountCredit.getGl_code(), transactionPojo.getPaymentReference(),
                    String.valueOf(transactionPojo.getUserToken().getId()), transactionPojo.getUserToken().getEmail(),
                    transactionPojo.getTranPart(), transactionPojo.getTransactionCategory(),
                    transactionPojo.getSenderName(), accountCredit.getAcct_name());
            walletTransactionRepository.saveAndFlush(tranCredit);

            double cumbalCrAmt = accountCredit.getCum_cr_amt() + transactionPojo.getAmount().doubleValue();
            accountCredit.setLast_tran_id_dr(transactionPojo.getTranId());
            accountCredit.setCum_cr_amt(cumbalCrAmt);
            accountCredit.setLast_tran_date(LocalDate.now());

            double unClrbalAmt = accountCredit.getCum_cr_amt() - accountCredit.getCum_dr_amt();
            accountCredit.setClr_bal_amt(Precision.round(unClrbalAmt - accountCredit.getLien_amt(), 2));
            accountCredit.setUn_clr_bal_amt(Precision.round(unClrbalAmt, 2));

            walletAccountRepository.saveAndFlush(accountCredit);

            CompletableFuture.runAsync(() -> logNotification(transactionPojo, accountCredit.getClr_bal_amt(), "CR"));

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

            WalletTransaction tranDebit = new WalletTransaction(transactionPojo.getTranId(),
                    accountDebit.getAccountNo(), transactionPojo.getAmount(),
                    transactionPojo.getTranType(), transactionPojo.getTranNarration(), LocalDate.now(),
                    accountDebit.getAcct_crncy_code(), "D",
                    accountDebit.getGl_code(), transactionPojo.getPaymentReference(),
                    String.valueOf(transactionPojo.getUserToken().getId()), transactionPojo.getUserToken().getEmail(),
                    transactionPojo.getTranPart(), transactionPojo.getTransactionCategory(),
                    accountDebit.getAcct_name(), transactionPojo.getReceiverName());
            walletTransactionRepository.saveAndFlush(tranDebit);

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

            CompletableFuture.runAsync(() -> logNotification(transactionPojo, accountDebit.getClr_bal_amt(), "DR"));

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
            String channelEventId) {
        log.info("Processing transfer transaction {}", transferTransactionRequestData.toString());

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
                transferTransactionRequestData.getAmount());
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

        if (transferTransactionRequestData.getDebitAccountNumber().length() > 10
                && transferTransactionRequestData.getBenefAccountNumber().length() > 10) {
            response = processCBATransactionGLDoubleEntryWithTransit(
                    new CBATransaction(userData, transferTransactionRequestData.getPaymentReference(), transitAccount,
                            transferTransactionRequestData.getBenefAccountNumber(),
                            transferTransactionRequestData.getDebitAccountNumber(), null,
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(), 
                            chargeAmount, vatAmount, provider, channelEventId,CBAAction.MOVE_GL_TO_GL));
        } else if (transferTransactionRequestData.getDebitAccountNumber().length() > 10
                && transferTransactionRequestData.getBenefAccountNumber().length() == 10) {
            response = processCBACustomerDepositTransactionWithDoubleEntryTransit(
                    new CBATransaction(userData, transferTransactionRequestData.getPaymentReference(), transitAccount,
                            customerDepositGL, transferTransactionRequestData.getDebitAccountNumber(),
                            transferTransactionRequestData.getBenefAccountNumber(),
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(), 
                            chargeAmount, vatAmount, provider, channelEventId, CBAAction.DEPOSIT));
        } else if (transferTransactionRequestData.getDebitAccountNumber().length() == 10
                && transferTransactionRequestData.getBenefAccountNumber().length() > 10) {
            
            response = processCBACustomerWithdrawTransactionWithDoubleEntryTransit(
                    new CBATransaction(userData, transferTransactionRequestData.getPaymentReference(), transitAccount,
                            transferTransactionRequestData.getBenefAccountNumber(), customerDepositGL,
                            transferTransactionRequestData.getDebitAccountNumber(),
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(), 
                            chargeAmount, vatAmount, provider, channelEventId, CBAAction.WITHDRAWAL));
        } else {
            response = processCBACustomerTransferTransactionWithDoubleEntryTransit(
                    new CBATransaction(userData, transferTransactionRequestData.getPaymentReference(), transitAccount,
                            transferTransactionRequestData.getBenefAccountNumber(), transferTransactionRequestData.getDebitAccountNumber(),
                            customerDepositGL,
                            transferTransactionRequestData.getTranNarration(),
                            transferTransactionRequestData.getTransactionCategory(),
                            transferTransactionRequestData.getTranType(),
                            transferTransactionRequestData.getAmount(), 
                            chargeAmount, vatAmount, provider, channelEventId, CBAAction.MOVE_CUSTOMER_TO_CUSTOMER));
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

        if(ObjectUtils.isEmpty(amount)){ 
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

        if(ObjectUtils.isEmpty(fee)){ 
            return vatAmount; 
        }

        if(fee.doubleValue() <= 0){ 
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
    public Long logTransaction(String fromAccountNumber, String toAccountNumber, BigDecimal amount,
            String transCategory, String tranCrncy, WalletTransStatus status) {

        String code = new Util().generateRandomNumber(9);
        try {
            WalletTransAccount walletTransAccount = new WalletTransAccount();
            walletTransAccount.setCreatedAt(LocalDateTime.now());
            walletTransAccount.setDebitAccountNumber(fromAccountNumber);
            walletTransAccount.setCreditAccountNumber(toAccountNumber);
            walletTransAccount.setTranAmount(amount);
            walletTransAccount.setTranId(code);
            walletTransAccount.setTransactionType(transCategory);
            walletTransAccount.setTranCrncy(tranCrncy);
            walletTransAccount.setStatus(status);
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
    public void logNotification(CBAEntryTransaction transactionPojo, double currentBalance, String tranType) {
        String tranDate = LocalDate.now().toString();

        AccountSumary account = tempwallet.getAccountSumaryLookUp(transactionPojo.getAccountNo());
        if (account == null) {
            return;
        }

        String systemToken = null;

        try {
            systemToken = tokenImpl.getToken();
            transactionCountService.makeCount(account.getUId().toString(), transactionPojo.getPaymentReference());
        } catch (Exception e) {
            log.error("Unable to get system token :: {}", e);
        }

        String notifyEmail = !ObjectUtils.isEmpty(account.getNotifyEmail())? account.getNotifyEmail():account.getEmail();
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
            customNotification.pushEMAIL(systemToken, account.getCustName(), account.getEmail(),
                    _email_message.toString(), account.getUId());
        }

        if (!ObjectUtils.isEmpty(account.getPhone())) {
            StringBuilder _sms_message = new StringBuilder();
            _sms_message.append(String.format("Acct: %s", transactionPojo.getAccountNo()));
            _sms_message.append("\n");
            _sms_message.append(String.format("Amt: %s %s",
                    Precision.round(transactionPojo.getAmount().doubleValue(), 2), tranType));
            _sms_message.append("\n");
            _sms_message.append(String.format("Desc: %s", transactionPojo.getTranNarration()));
            _sms_message.append("\n");
            _sms_message.append(String.format("Avail Bal: %s", Precision.round(currentBalance, 2)));
            _sms_message.append("\n");
            _sms_message.append(String.format("Date: %s", tranDate));
            customNotification.pushWayaSMS(systemToken, account.getCustName(), account.getPhone(),
                    _sms_message.toString(), account.getUId(), account.getEmail());
        }

    }

    @Override
    public ResponseEntity<?> securityCheckOwner(String accountNumber) {
        log.info("securityCheck Ownership:: " + accountNumber);
        MyData userToken = (MyData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
    public ResponseEntity<?> securityCheck(String accountNumber, BigDecimal amount) {
        log.info("securityCheck to debit account{} amount{}", accountNumber, amount);
        MyData userToken = (MyData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userToken == null) {
            log.error("token validation failed for debiting account{} with amount{}", accountNumber, amount);
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        // if(tokenImpl.validatePIN(null, null)){
        // log.error("pin validation failed for debiting account{} with amount{}",
        // accountNumber, amount);
        // return new ResponseEntity<>(new
        // ErrorResponse(ResponseCodes.INVALID_PIN.getValue()),
        // HttpStatus.BAD_REQUEST);
        // }

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
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.DEBIT_LIMIT_REACHED.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalTransactionToday = walletTransactionRepository.totalTransactionAmountToday(accountNumber,
                LocalDate.now());
        log.info("totalTransactionToday {}", totalTransactionToday);
        totalTransactionToday = totalTransactionToday == null ? new BigDecimal(0) : totalTransactionToday;
        if (totalTransactionToday.doubleValue() >= Double.parseDouble(account.getDebitLimit())) {
            log.error("Debit limit reached :: {}", account.getDebitLimit());
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.DEBIT_LIMIT_REACHED.getValue()),
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
        if(cbaTransaction.getAction().equals(CBAAction.MOVE_FROM_TRANSIT)){
            return cbaTransaction.getTransitGLAccount();
        }
        return account;
    }
    
    private String getCreditAccountNumber(CBATransaction cbaTransaction) {
        String account = cbaTransaction.getCreditGLAccount();
        if(cbaTransaction.getAction().equals(CBAAction.MOVE_TO_TRANSIT)){
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

        return new ResponseEntity<>(new ErrorResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
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
            response = debitAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(), tranId,
                    cbaTransaction.getPaymentReference(), tranCategory, getDebitAccountNumber(cbaTransaction),
                    cbaTransaction.getNarration(), cbaTransaction.getAmount(), 1, tranType, ""));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        try {
            response = creditAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(), tranId,
                    cbaTransaction.getPaymentReference(), tranCategory, getCreditAccountNumber(cbaTransaction),
                    cbaTransaction.getNarration(), cbaTransaction.getAmount(), 2, tranType, ""));
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
            creditAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(), tranId,
                    cbaTransaction.getPaymentReference(), tranCategory, getDebitAccountNumber(cbaTransaction),
                    cbaTransaction.getNarration(), cbaTransaction.getAmount(), 2, tranType, ""));
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
        mifosTransfer.setAmount(BigDecimal.valueOf(Precision.round(cbaTransaction.getAmount().doubleValue() + cbaTransaction.getCharge().doubleValue(), 2)));
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

        return new ResponseEntity<>(new ErrorResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
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

        BigDecimal totalAmount = BigDecimal.valueOf(Precision.round(cbaTransaction.getAmount().doubleValue() + cbaTransaction.getCharge().doubleValue(), 2));
        try {
            if (cbaTransaction.getAction().equals(CBAAction.WITHDRAWAL)) {
                creditAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(), tranId,
                        cbaTransaction.getPaymentReference(), tranCategory, getDebitAccountNumber(cbaTransaction),
                        cbaTransaction.getNarration(), totalAmount, 1, tranType, ""));
            } else {
                response = creditAccount(new CBAEntryTransaction(cbaTransaction.getUserToken(), tranId,
                        cbaTransaction.getPaymentReference(), tranCategory, getDebitAccountNumber(cbaTransaction),
                        cbaTransaction.getNarration(), totalAmount, 1, tranType, ""));
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
         * Todo
         * rever customer deposit on failure
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

        if(cbaTransaction.getCharge().doubleValue() <= 0){
            return response;
        }


        log.info("applying charge for transaction on {}", cbaTransaction.getCustomerAccount());
        String chargeCollectionAccount = getEventAccountNumber("INCOME_".concat(cbaTransaction.getChannelEvent()));
        if (chargeCollectionAccount == null || cbaTransaction.getCharge().doubleValue() <= 0) {
            return response;
        }
        
        cbaTransaction.setDebitGLAccount(cbaTransaction.getCreditGLAccount());
        cbaTransaction.setCreditGLAccount(chargeCollectionAccount);
        cbaTransaction.setAmount( cbaTransaction.getCharge());
        processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

        log.info("applying VAT for transaction on {}", cbaTransaction.getCustomerAccount());
        String vatCollectionAccount = getEventAccountNumber("VAT_".concat(cbaTransaction.getChannelEvent()));
        if (vatCollectionAccount == null) {
            return response;
        }

        cbaTransaction.setCreditGLAccount(vatCollectionAccount);
        cbaTransaction.setAmount( cbaTransaction.getCharge());
        processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

        /**
         * Todo
         * rever customer withdrawal on failure
         */

        return response;
    }

    @Override
    public ResponseEntity<?> processCBACustomerTransferTransactionWithDoubleEntryTransit( CBATransaction cbaTransaction) {
        log.info("Processing CBA customer transfer entry transaction depositGL:{} from:{} to:{} using transit:{}",
                cbaTransaction.getCustomerAccount(),
                cbaTransaction.getDebitGLAccount(), cbaTransaction.getCreditGLAccount(),
                cbaTransaction.getTransitGLAccount());
        String debitAccoount = cbaTransaction.getDebitGLAccount();
        String creditAccoount = cbaTransaction.getCreditGLAccount();

        cbaTransaction.setDebitGLAccount(cbaTransaction.getCustomerAccount());
        cbaTransaction.setCreditGLAccount(cbaTransaction.getCustomerAccount());

        cbaTransaction.setAction(CBAAction.WITHDRAWAL);
        cbaTransaction.setCustomerAccount(debitAccoount);
        ResponseEntity<?> response = processCBACustomerWithdrawTransactionWithDoubleEntryTransit(cbaTransaction);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        cbaTransaction.setAction(CBAAction.DEPOSIT);
        cbaTransaction.setCustomerAccount(creditAccoount);
        response = processCBACustomerDepositTransactionWithDoubleEntryTransit(cbaTransaction);

        return response;
    }

}