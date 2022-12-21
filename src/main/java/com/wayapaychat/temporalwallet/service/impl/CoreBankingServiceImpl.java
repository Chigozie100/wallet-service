package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AccountSumary;
import com.wayapaychat.temporalwallet.dto.ExternalCBAResponse;
import com.wayapaychat.temporalwallet.dto.MifosTransfer;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
import com.wayapaychat.temporalwallet.proxy.MifosWalletProxy;
import com.wayapaychat.temporalwallet.repository.UserPricingRepository;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.service.CoreBankingService;
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
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.PriceCategory;
import com.wayapaychat.temporalwallet.enumm.ProductPriceStatus;
import com.wayapaychat.temporalwallet.enumm.ProviderType;
import com.wayapaychat.temporalwallet.enumm.ExternalCBAResponseCodes;
import com.wayapaychat.temporalwallet.enumm.ResponseCodes;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.enumm.WalletTransStatus;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.notification.CustomNotification;

@Service
@Slf4j
public class CoreBankingServiceImpl implements CoreBankingService {

    @Value("${jwt.secret:2YuUlb+t36yVzrTkYLl8xBlBJSC41CE7uNF3somMDxdYDfcACv9JYIU54z17s4Ah313uKu/4Ll+vDNKpxx6v4Q==")
    private String appToken;
 
    private final SwitchWalletService switchWalletService;
    private final WalletTransAccountRepository walletTransAccountRepository;
    private final WalletAccountRepository walletAccountRepository;
    private final WalletEventRepository walletEventRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MifosWalletProxy mifosWalletProxy;
	private final TemporalWalletDAO tempwallet;
    private final CustomNotification customNotification;
	private final UserPricingRepository userPricingRepository;

    @Autowired
    public CoreBankingServiceImpl(SwitchWalletService switchWalletService,
            WalletTransAccountRepository walletTransAccountRepository, WalletAccountRepository walletAccountRepository,
            WalletEventRepository walletEventRepository, WalletTransactionRepository walletTransactionRepository,
            MifosWalletProxy mifosWalletProxy, TemporalWalletDAO tempwallet, CustomNotification customNotification,
            UserPricingRepository userPricingRepository) {
        this.switchWalletService = switchWalletService;
        this.walletTransAccountRepository = walletTransAccountRepository;
        this.walletAccountRepository = walletAccountRepository;
        this.walletEventRepository = walletEventRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.mifosWalletProxy = mifosWalletProxy;
        this.tempwallet = tempwallet;
        this.customNotification = customNotification;
        this.userPricingRepository = userPricingRepository;
    }

    @Override
    public ResponseEntity<?> createAccount(TransactionPojo transactionPojo) {
        return null;
    }

    public ResponseEntity<?> getAccountDetails(String accountNo){

		try{
			Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNo);
			if (!account.isPresent()) {
				return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
			}
			BigDecimal totalDr = walletTransactionRepository.totalTransactionAmount(accountNo, "D");
			BigDecimal totalCr = walletTransactionRepository.totalTransactionAmount(accountNo, "C");

			if(totalDr != null && totalCr != null){
				double unClrbalAmt = totalCr.doubleValue() - totalDr.doubleValue();
                account.get().setCum_cr_amt(totalCr.doubleValue());
                account.get().setCum_dr_amt(totalDr.doubleValue());
				account.get().setClr_bal_amt(Precision.round(unClrbalAmt-account.get().getLien_amt(), 2));
				account.get().setUn_clr_bal_amt(Precision.round(unClrbalAmt, 2));
				walletAccountRepository.saveAndFlush(account.get());
			}

			return new ResponseEntity<>(new SuccessResponse("Wallet", account), HttpStatus.OK);
		}catch (Exception ex){
            ex.printStackTrace();
			return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),  HttpStatus.BAD_REQUEST);
		}
	}

    @Override
    public ResponseEntity<?> creditAccount(CBAEntryTransaction transactionPojo) {
        log.info("Processing credit transaction {}", transactionPojo.toString());
        try {
            WalletAccount accountCredit = walletAccountRepository.findByAccountNo(transactionPojo.getAccountNo());

            WalletTransaction tranCredit = new WalletTransaction(transactionPojo.getTranId(), accountCredit.getAccountNo(), transactionPojo.getAmount(), 
                            transactionPojo.getTranType(), transactionPojo.getTranNarration(), LocalDate.now(), accountCredit.getAcct_crncy_code(), "C",
                            accountCredit.getGl_code(), transactionPojo.getPaymentReference(), String.valueOf(transactionPojo.getUserToken().getId()), transactionPojo.getUserToken().getEmail(),
                            transactionPojo.getTranPart(), transactionPojo.getTransactionCategory(), transactionPojo.getSenderName(), accountCredit.getAcct_name());
            walletTransactionRepository.saveAndFlush(tranCredit);

            double cumbalCrAmt = accountCredit.getCum_cr_amt() + transactionPojo.getAmount().doubleValue();
            accountCredit.setLast_tran_id_dr(transactionPojo.getTranId());
            accountCredit.setCum_cr_amt(cumbalCrAmt);
            accountCredit.setLast_tran_date(LocalDate.now());

            double unClrbalAmt = accountCredit.getCum_cr_amt() - accountCredit.getCum_dr_amt();
            accountCredit.setClr_bal_amt(Precision.round(unClrbalAmt-accountCredit.getLien_amt(), 2));
            accountCredit.setUn_clr_bal_amt(Precision.round(unClrbalAmt, 2));

            walletAccountRepository.saveAndFlush(accountCredit);
            
            CompletableFuture.runAsync(() -> logNotification(transactionPojo));

            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.SUCCESSFUL_CREDIT.getValue()), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.FAILED_CREDIT.getValue()),  HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<?> debitAccount(CBAEntryTransaction transactionPojo) {
        log.info("Processing debit transaction {}", transactionPojo.toString());
        try{
            WalletAccount accountDebit = walletAccountRepository.findByAccountNo(transactionPojo.getAccountNo());
            
            WalletTransaction tranDebit = new WalletTransaction(transactionPojo.getTranId(), accountDebit.getAccountNo(), transactionPojo.getAmount(), 
                        transactionPojo.getTranType(), transactionPojo.getTranNarration(), LocalDate.now(), accountDebit.getAcct_crncy_code(), "D",
                        accountDebit.getGl_code(), transactionPojo.getPaymentReference(), String.valueOf(transactionPojo.getUserToken().getId()), transactionPojo.getUserToken().getEmail(),
                        transactionPojo.getTranPart(), transactionPojo.getTransactionCategory(), accountDebit.getAcct_name(), transactionPojo.getReceiverName());
            walletTransactionRepository.saveAndFlush(tranDebit);

            double cumbalDrAmt = accountDebit.getCum_dr_amt() + transactionPojo.getAmount().doubleValue();
            //lien of same amount debited is also removed
            double lienAmt = accountDebit.getLien_amt() - transactionPojo.getAmount().doubleValue();
            if(lienAmt <= 0){
                accountDebit.setLien_reason("");
                lienAmt = 0;
            }

            accountDebit.setLien_amt(lienAmt);
            accountDebit.setLast_tran_id_dr(transactionPojo.getTranId());
            accountDebit.setCum_dr_amt(cumbalDrAmt);
            accountDebit.setLast_tran_date(LocalDate.now());

            double unClrbalAmt = accountDebit.getCum_cr_amt() - accountDebit.getCum_dr_amt();
            accountDebit.setClr_bal_amt(Precision.round(unClrbalAmt-lienAmt, 2));
            accountDebit.setUn_clr_bal_amt(Precision.round(unClrbalAmt, 2));

            walletAccountRepository.saveAndFlush(accountDebit);

            CompletableFuture.runAsync(() -> logNotification(transactionPojo));

            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.SUCCESSFUL_DEBIT.getValue()), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.FAILED_DEBIT.getValue()),  HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?>  processCBATransactionDoubleEntry(MyData userToken, String paymentReference,  String fromAccount, String toAccount, String narration, CategoryType category, BigDecimal amount, Provider provider){

        String tranId = tempwallet.TransactionGenerate();
        TransactionTypeEnum tranType = TransactionTypeEnum.BANK;
        ResponseEntity<?> response = processExternalCBATransactionDoubleEntry(paymentReference, fromAccount, toAccount, narration, category, amount, provider);
        if (!provider.getName().equals(ProviderType.TEMPORAL) && !response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        try {
            response = debitAccount(new CBAEntryTransaction(userToken, tranId, paymentReference, category, fromAccount, narration, amount, 1, tranType, ""));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        try {
            response = creditAccount(new CBAEntryTransaction(userToken, tranId, paymentReference, category, toAccount, narration, amount, 2, tranType, ""));
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        //Reverse debit if credit failed
        if (!response.getStatusCode().is2xxSuccessful()) {
            String reversalNarration = "Reversal ".concat(narration);
            processExternalCBATransactionDoubleEntry(paymentReference, toAccount, fromAccount, reversalNarration, category, amount, provider);
            creditAccount(new CBAEntryTransaction(userToken, tranId, paymentReference, category, fromAccount, reversalNarration, amount,  2, tranType, ""));
        }

        return response;
    }


    @Override
    public ResponseEntity<?> processExternalCBATransactionDoubleEntry(String paymentReference, String fromAccount, String toAccount,
            String narration, CategoryType category, BigDecimal amount, Provider provider) {

        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        WalletAccount accountDebit = walletAccountRepository.findByAccountNo(fromAccount);
        WalletAccount accountCredit = walletAccountRepository.findByAccountNo(toAccount);
        

        MifosTransfer mifosTransfer = new MifosTransfer();

        mifosTransfer.setRequestId(generateSessionId());
        mifosTransfer.setAmount(amount);
        mifosTransfer.setNarration(narration);
        mifosTransfer.setTransactionType(TransactionTypeEnum.TRANSFER.getValue());

        mifosTransfer.setDestinationAccountNumber(accountCredit.getNubanAccountNo());
        mifosTransfer.setDestinationAccountType("SAVINGS");
        mifosTransfer.setDestinationCurrency(accountCredit.getAcct_crncy_code());

        mifosTransfer.setSourceAccountNumber(accountDebit.getNubanAccountNo());
        mifosTransfer.setSourceAccountType("SAVINGS");
        mifosTransfer.setSourceCurrency(accountDebit.getAcct_crncy_code());

        ExternalCBAResponse externalResponse = null;
        if(ProviderType.MIFOS.equalsIgnoreCase(provider.getName()))
        {
            externalResponse = mifosWalletProxy.transferMoney(mifosTransfer);
        }
        else{
            externalResponse = new ExternalCBAResponse(ExternalCBAResponseCodes.R_00);
        }

        if(externalResponse == null){
            return response;
        }

        if(!ExternalCBAResponseCodes.R_00.getRespCode().equals(externalResponse.getResponseCode())){
            return response;
        }

        return new ResponseEntity<>(new ErrorResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> processCBATransactionDoubleEntryWithTransit(MyData userToken, String paymentReference, String transitAccount, String fromAccount,
            String toAccount, String narration, String category, BigDecimal amount, Provider provider) {
        
        log.info("Processing CBA double entry transaction from:{} to:{} using transit:{}", fromAccount, toAccount, transitAccount);
        
        CategoryType categoryType = CategoryType.valueOf(category);
        ResponseEntity<?> response = processCBATransactionDoubleEntry(userToken, paymentReference, fromAccount, transitAccount, narration, categoryType, amount, provider);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        response = processCBATransactionDoubleEntry(userToken, paymentReference, transitAccount, toAccount, narration, categoryType, amount, provider);

        //Reverse debit if credit failed
        if (!response.getStatusCode().is2xxSuccessful()) {
            processCBATransactionDoubleEntry(userToken, paymentReference, transitAccount, fromAccount, "Reversal "+narration, categoryType, amount, provider);
        }

        return response;
    }

    @Override
    public ResponseEntity<?> transfer(TransferTransactionDTO transferTransactionRequestData, String channelEventId) {

        log.info(" #####  INSIDE TRANSFER ####### " + transferTransactionRequestData);
        ResponseEntity<?> response = securityCheck(transferTransactionRequestData.getDebitAccountNumber(), transferTransactionRequestData.getAmount());
		if(!response.getStatusCode().is2xxSuccessful()){
			return response;
		}
        
        log.info("Processing transfer transaction {}", transferTransactionRequestData.toString());
        if (transferTransactionRequestData.getDebitAccountNumber().equals(transferTransactionRequestData.getBenefAccountNumber())) {
            return new ResponseEntity<>(new ErrorResponse("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT"),  HttpStatus.BAD_REQUEST);
        }

        if (walletAccountRepository.countAccount(transferTransactionRequestData.getBenefAccountNumber()) < 1) {
            return new ResponseEntity<>(new ErrorResponse("INVALID BENEFICIARY ACCOUNT"), HttpStatus.BAD_REQUEST);
        }

        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) { return new ResponseEntity<>(new ErrorResponse("NO PROVIDER SWITCHED"), HttpStatus.BAD_REQUEST);   }

        Long tranId = logTransaction(transferTransactionRequestData.getDebitAccountNumber(), transferTransactionRequestData.getBenefAccountNumber(),
                                transferTransactionRequestData.getAmount(), transferTransactionRequestData.getTransactionCategory(), transferTransactionRequestData.getTranCrncy(), WalletTransStatus.PENDING);
        if (tranId == null) { return new ResponseEntity<>(new ErrorResponse("ERROR PROCESSING TRANSACTION"), HttpStatus.BAD_REQUEST);  }
        
        MyData userData = (MyData)response.getBody();
        String transitAccount = getEventAccountNumber(channelEventId);
        response = processCBATransactionDoubleEntryWithTransit(userData, transferTransactionRequestData.getPaymentReference(), transitAccount, transferTransactionRequestData.getDebitAccountNumber(),  transferTransactionRequestData.getBenefAccountNumber(), 
                                        transferTransactionRequestData.getTranNarration(), transferTransactionRequestData.getTransactionCategory(), transferTransactionRequestData.getAmount(), provider);
       

        if (!response.getStatusCode().is2xxSuccessful()) {
            updateTransactionLog(tranId, WalletTransStatus.REVERSED);
            return response;
        }

        // Async or schedule

        if(transitAccount !=null){
            final String finalTransitAccount = transitAccount;
            CompletableFuture.runAsync(() -> 
            applyCharge(userData, finalTransitAccount, transferTransactionRequestData.getDebitAccountNumber(), transferTransactionRequestData.getTranNarration(), 
                transferTransactionRequestData.getTranType(), transferTransactionRequestData.getTransactionCategory(),  transferTransactionRequestData.getAmount(), provider, channelEventId));
        }

        updateTransactionLog(tranId, WalletTransStatus.SUCCESSFUL);

        Optional<List<WalletTransaction>> transaction = walletTransactionRepository.findByReference(transferTransactionRequestData.getPaymentReference());
        if (transaction.isEmpty()) {
					return new ResponseEntity<>(new ErrorResponse("TRANSACTION FAILED TO CREATE"),
							HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(new SuccessResponse("TRANSACTION SUCCESSFULLY", transaction), HttpStatus.CREATED);
    }


    // auto move money WAYA MFB (Mifos) Paystack Intransit disbursement account is debited with 10,000
    // to User James Waya MFB (Mifos) Customer Account is credited with 10,000

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
    public BigDecimal computeTransactionFee(String accountNumber, BigDecimal amount,  String eventId){
        BigDecimal priceAmount = new BigDecimal(0);

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
        if (!eventInfo.isPresent()) { return priceAmount; }

        AccountSumary account = tempwallet.getAccountSumaryLookUp(accountNumber);
        if(account == null){ return priceAmount; }

        UserPricing userPricingOptional = userPricingRepository.findDetailsByCode(account.getUId(), eventId).orElse(null);
        if(userPricingOptional == null){ return priceAmount; }

        priceAmount = userPricingOptional.getStatus().equals(ProductPriceStatus.GENERAL) 
                                    ? userPricingOptional.getGeneralAmount() // Get general amount
                                    : userPricingOptional.getCustomAmount(); // Get custom amount

        priceAmount =  !userPricingOptional.getPriceType().equals(PriceCategory.FIXED)
                                    ? BigDecimal.valueOf(amount.doubleValue() * priceAmount.doubleValue() / 100) // compute percentage
                                    : priceAmount; // Get fixed amount
        
        if(priceAmount.doubleValue() <= 0){ return priceAmount; }

        if(priceAmount.doubleValue() > userPricingOptional.getCapPrice().doubleValue()){ 
            priceAmount = userPricingOptional.getCapPrice();
        }
        
        //add vat to fee
        priceAmount = BigDecimal.valueOf(priceAmount.doubleValue() + computeVatFee(priceAmount, eventId).doubleValue());

        return priceAmount;
    }

    @Override
    public BigDecimal computeVatFee(BigDecimal fee,  String eventId){
        BigDecimal vatAmount = new BigDecimal(0);

        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
        if (!eventInfo.isPresent()) { return vatAmount; }

       
        if(eventInfo.get().getTaxAmt().doubleValue() > 0){
            vatAmount = BigDecimal.valueOf(fee.doubleValue() * eventInfo.get().getTaxAmt().doubleValue()/100);
        } 

        return vatAmount;
    }


    @Override
    public void applyCharge(MyData userData, String transitAccount, String debitAccountNumber, String tranNarration,
            String transactionCategory, String transactionType, @NotNull BigDecimal amount, Provider provider, String channelEventId) {
        log.info("applying charge for transaction on {}", debitAccountNumber);

        String chargeCollectionAccount = getEventAccountNumber("INCOME_".concat(channelEventId));
        if(chargeCollectionAccount == null){ return; }

        BigDecimal priceAmount = this.computeTransactionFee(debitAccountNumber, amount, channelEventId);
        String tranId = tempwallet.TransactionGenerate();

        //get vat and deduct from charge to get income amount
        BigDecimal vatAmount = this.computeVatFee(priceAmount, channelEventId); 
        priceAmount = BigDecimal.valueOf(priceAmount.doubleValue() - vatAmount.doubleValue());
        

        log.info("applying income charge {}", priceAmount.doubleValue());
        processCBATransactionDoubleEntryWithTransit(userData, tranId, transitAccount, debitAccountNumber,  chargeCollectionAccount, tranNarration, transactionCategory, priceAmount, provider);
        
        processVAT(userData, transitAccount, debitAccountNumber,  chargeCollectionAccount, vatAmount, tranNarration, transactionCategory, transactionType, provider, channelEventId);

    }

    public void processVAT(MyData userData, String transitAccount, String customerDebitAccountNumber, String chargeCollectionAccount, BigDecimal vatAmount, String tranNarration,
                                    String transactionCategory, String transactionType, Provider provider, String channelEventId){
        log.info("applying VAT for transaction on {}", customerDebitAccountNumber);
        if(vatAmount.doubleValue() <= 0){ return;}

        String vatCollectionAccount = getEventAccountNumber("VAT_".concat(channelEventId));
        if(vatCollectionAccount == null){ return; }
        

        tranNarration = "VAT: ".concat(tranNarration);
        processCBATransactionDoubleEntryWithTransit(userData, tempwallet.TransactionGenerate(), transitAccount, customerDebitAccountNumber,  vatCollectionAccount, tranNarration, transactionCategory, vatAmount, provider);
        

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
            throw new CustomException("Error in loggin transaction :: " + ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
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
            // throw new CustomException("error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    private String generateSessionId() {
        long randomNum = (long) Math.floor(Math.random() * 9_000_000_000_00L) + 1_000_000_000_00L;
        return  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + Long.toString(randomNum);
    }

    @Override
    public void addLien(WalletAccount account, BigDecimal amount) {

        account.setLien_amt(amount.doubleValue());
        walletAccountRepository.saveAndFlush(account);
    
    }

    @Override
    public void logNotification(CBAEntryTransaction transactionPojo) {
        String tranDate = LocalDate.now().toString();

        AccountSumary account = tempwallet.getAccountSumaryLookUp(transactionPojo.getAccountNo());
        if(account == null){
            return;
        }

        if(account.getEmail() == null){
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append(String.format("A transaction has occurred with reference: %s on your account see details below. \n", transactionPojo.getTranId()));
        message.append(String.format("Amount :%s \n", transactionPojo.getAmount()));
        message.append(String.format("Date :%s \n", tranDate));
        //message.append(String.format("Currency :%s \n", transactionPojo.getTranCrncy()));
        message.append(String.format("Narration :%s ", transactionPojo.getTranNarration()));
        
        customNotification.pushEMAIL(this.appToken, account.getCustName(), account.getEmail(), message.toString(), account.getUId());

    }

    @Override
    public ResponseEntity<?> securityCheck(String accountNumber, BigDecimal amount) {
        log.info("securityCheck :: " + accountNumber);
        MyData userToken = (MyData)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userToken == null) {
            return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
        }

        Optional<WalletAccount> ownerAccount = walletAccountRepository.findByAccount(accountNumber);
        log.info("ownerAccount :: " + ownerAccount);
        if (!ownerAccount.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse(String.format("INVALID SOURCE ACCOUNT %s", accountNumber)), HttpStatus.BAD_REQUEST);
        }
        log.info(" ###################### AFTER findByAccount :: #################### " );


        AccountSumary account = tempwallet.getAccountSumaryLookUp(accountNumber);
        log.info("AccountSumary :: " + account);
        if(account == null){
            return new ResponseEntity<>(new ErrorResponse(String.format("INVALID SOURCE ACCOUNT %s", accountNumber)), HttpStatus.BAD_REQUEST);
        }

        log.info(" ###################### AFTER getAccountSumaryLookUp :: #################### " );

        double sufficientFunds = ownerAccount.get().getCum_cr_amt() - ownerAccount.get().getCum_dr_amt() -  ownerAccount.get().getLien_amt() - amount.doubleValue();

        log.info(" sufficientFunds :: " + sufficientFunds);
        if(sufficientFunds < 0){
            return new ResponseEntity<>(new ErrorResponse("INSUFFICIENT FUNDS"), HttpStatus.BAD_REQUEST);
        }

        if(amount.doubleValue() > Double.parseDouble(account.getDebitLimit())){
            return new ResponseEntity<>(new ErrorResponse("DEBIT LIMIT REACHED"), HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalTransactionToday = walletTransactionRepository.totalTransactionAmountToday(accountNumber, LocalDate.now());
        log.info("totalTransactionToday {}", totalTransactionToday);
        totalTransactionToday = totalTransactionToday == null? new BigDecimal(0):totalTransactionToday;
        if(totalTransactionToday.doubleValue() >= Double.parseDouble(account.getDebitLimit())){
            return new ResponseEntity<>(new ErrorResponse("DEBIT LIMIT REACHED"), HttpStatus.BAD_REQUEST);
        }

        boolean isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_OWNER"::equalsIgnoreCase);
        isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_APP"::equalsIgnoreCase)? true : isWriteAdmin;
        boolean isOwner =  Long.compare(account.getUId(), userToken.getId()) == 0;

        if(StringUtils.isNumeric(accountNumber)){
            if(!isOwner && !isWriteAdmin){
                log.error("owner check {} {}", isOwner, isWriteAdmin);
                return new ResponseEntity<>(new ErrorResponse(String.format("INVALID SOURCE ACCOUNT %s %s %s", accountNumber, isOwner, isWriteAdmin)), HttpStatus.BAD_REQUEST);
            }
        }

        addLien(ownerAccount.get(),  amount);

        return new ResponseEntity<>(userToken, HttpStatus.ACCEPTED);

    }


}