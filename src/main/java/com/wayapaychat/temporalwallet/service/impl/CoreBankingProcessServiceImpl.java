package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.wayapaychat.temporalwallet.enumm.TransactionChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.waya.security.auth.pojo.UserIdentityData;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.WalletProcess;
import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.enumm.CBAAction;
import com.wayapaychat.temporalwallet.enumm.EventCharge;
import com.wayapaychat.temporalwallet.enumm.ResponseCodes;
import com.wayapaychat.temporalwallet.pojo.CBATransaction;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.repository.WalletProcessRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.service.CoreBankingProcessService;
import com.wayapaychat.temporalwallet.service.CoreBankingService;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.SuccessResponse;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoreBankingProcessServiceImpl implements CoreBankingProcessService {

    private final WalletTransAccountRepository walletTransAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletProcessRepository walletProcessRepository;
    private final CoreBankingService coreBankingService;
    private final SwitchWalletService switchWalletService;

    @Autowired
    public CoreBankingProcessServiceImpl(WalletTransAccountRepository walletTransAccountRepository,
            WalletTransactionRepository walletTransactionRepository, WalletProcessRepository walletProcessRepository,
            CoreBankingService coreBankingService, SwitchWalletService switchWalletService) {
        this.walletTransAccountRepository = walletTransAccountRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletProcessRepository = walletProcessRepository;
        this.coreBankingService = coreBankingService;
        this.switchWalletService = switchWalletService;
    }

    @Override
    public ResponseEntity<?> runProcess(String processName) {

        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (userToken == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> processResponse = new ResponseEntity<>(
                new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        LocalDate processDate = getLastProcessedDate(processName);
        List<WalletTransaction> allCustomerTransaction = walletTransactionRepository
                .findByAllCustomerTransaction(processDate, "");

        for (WalletTransaction walletTransaction : allCustomerTransaction) {
            processResponse = validateEntries(processName, walletTransaction);
            if (!processResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Error validation entries for process {} transaction:{} ", processName,
                        walletTransaction.getTranId());
                return processResponse;
            }
        }

        WalletProcess walletProcess = new WalletProcess(processName, "SUCCESSFUL", "", processDate,
                LocalDateTime.now(), LocalDateTime.now(), userToken.getEmail(), userToken.getEmail());
        walletProcessRepository.save(walletProcess);

        return processResponse;

    }

    @Override
    public ResponseEntity<?> validateEntries(String processName, WalletTransaction walletTransaction) {

        Optional<List<WalletTransaction>> relatedTrransactions = walletTransactionRepository
                .findByRelatedTrans(Long.toString(walletTransaction.getRelatedTransId()));
        if (relatedTrransactions.isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if (relatedTrransactions.get().size() <= 1) {
            log.error("No entries for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        Optional<WalletTransAccount> transLog = walletTransAccountRepository
                .findById(walletTransaction.getRelatedTransId());
        if (transLog.isEmpty()) {
            log.error("Unable to get transaction log for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if("WAYATRAN".equalsIgnoreCase(transLog.get().getEventId()) && relatedTrransactions.get().size() < 10 ){
            log.error("missing entries for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if("NIP_PAYOUT".equalsIgnoreCase(transLog.get().getEventId()) && relatedTrransactions.get().size() < 13 ){
            log.error("missing entries for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if("NIP_FUNDING".equalsIgnoreCase(transLog.get().getEventId()) && relatedTrransactions.get().size() < 5 ){
            log.error("missing entries for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if("PAYSTACK_FUNDING".equalsIgnoreCase(transLog.get().getEventId()) && relatedTrransactions.get().size() < 5 ){
            log.error("missing entries for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if("QUICKTELLER".equalsIgnoreCase(transLog.get().getEventId()) && relatedTrransactions.get().size() < 5 ){
            log.error("missing entries for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
    }

    @Override
    public LocalDate getLastProcessedDate(String processName) {

        WalletProcess lastProcess = walletProcessRepository.findFirstByProcessNameOrderByIdDesc(processName);
        if (!ObjectUtils.isEmpty(lastProcess)) {
            return lastProcess.getProcessDate().plusDays(1);
        }

        WalletTransaction transaction = walletTransactionRepository.findFirstByAcctNumNotLikeOrderByIdAsc("NGN%");
        if (!ObjectUtils.isEmpty(transaction)) {
            return transaction.getTranDate();
        }

        return LocalDate.of(2022, 1, 1);
    }

    @Override
    public ResponseEntity<?> fixTransactionEntries(HttpServletRequest request, String transactionId) {
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (userToken == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> processResponse = new ResponseEntity<>(
                new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);

        Optional<WalletTransAccount> transLog = walletTransAccountRepository
                .findById(Long.valueOf(transactionId));
        if (transLog.isEmpty()) {
            log.error("Unable to get transaction log for transaction {}", transactionId);
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        Optional<List<WalletTransaction>> relatedTrransactions = walletTransactionRepository
                .findByRelatedTrans(transactionId);
        if (relatedTrransactions.isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if (relatedTrransactions.get().size() < 1) {
            log.error("No entries for transaction {}", transactionId);
            return new ResponseEntity<>(
                    new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        processResponse = processActualAmountEntries(transLog, relatedTrransactions, userToken);
        if (!processResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error processing entries for actual amount transaction:{} ", transactionId);
            return processResponse;
        }

        processResponse = processFeeAmountEntries(transLog, relatedTrransactions, userToken);
        if (!processResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error processing entries for actual amount transaction:{} ", transactionId);
            return processResponse;
        }

        processResponse = processVatAmountEntries(transLog, relatedTrransactions, userToken);
        if (!processResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error processing entries for actual amount transaction:{} ", transactionId);
            return processResponse;
        }
        
        return processResponse;
    }

    @Override
    public  ResponseEntity<?> processVatAmountEntries(Optional<WalletTransAccount> transLog,
            Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken) {
        
        if(BigDecimal.ZERO.compareTo(transLog.get().getVatAmount()) == 0){
            return new ResponseEntity<>(
            new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        }
        
        List<WalletTransaction> vatAmountEntries = relatedTrransactions.get().stream()
                .filter(accTrans -> 
                accTrans.getAcctNum().contains("NGN") 
                && accTrans.getTranAmount().equals(transLog.get().getVatAmount()))
                .collect(Collectors.toList());

        if(vatAmountEntries.size() == 4){
            return new ResponseEntity<>(
            new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        }

        Provider provider = switchWalletService.getActiveProvider();
        List<WalletTransaction> customerWalletTransaction = relatedTrransactions.get().stream()
                .filter(accTrans -> !accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());
                
        String customerDepositGL = coreBankingService.getEventAccountNumber(EventCharge.WAYATRAN.name());
        String transitAccount = coreBankingService.getEventAccountNumber(transLog.get().getEventId());
        String debitAccountNumber; String creditAccountNumber;  
        if(transLog.get().getCreditAccountNumber().contains("NGN")){
            debitAccountNumber = customerDepositGL;
            creditAccountNumber = transLog.get().getCreditAccountNumber();
        }
        else if(transLog.get().getDebitAccountNumber().contains("NGN")){
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = transLog.get().getDebitAccountNumber();
        }
        else{
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = customerDepositGL;
        }
        String tChannel = TransactionChannel.WAYABANK.name();
        BigDecimal commissionFee = BigDecimal.ZERO;
        if(customerWalletTransaction.get(0).getTransChannel() != null)
            tChannel = customerWalletTransaction.get(0).getTransChannel();

        if(customerWalletTransaction.get(0).getCommissionFee() != null)
            commissionFee = customerWalletTransaction.get(0).getCommissionFee();

        CBATransaction cbaTransaction = new CBATransaction(transLog.get().getSenderName(), transLog.get().getBeneficiaryName(), userToken,
        customerWalletTransaction.get(0).getPaymentReference(), 
        transitAccount, creditAccountNumber, debitAccountNumber, null,
        customerWalletTransaction.get(0).getTranNarrate(),
        customerWalletTransaction.get(0).getTranCategory().name(), customerWalletTransaction.get(0).getTranType().name(),
        transLog.get().getTranAmount(), transLog.get().getChargeAmount(), transLog.get().getVatAmount(), 
        provider, transLog.get().getEventId(), customerWalletTransaction.get(0).getRelatedTransId(),
                CBAAction.MOVE_GL_TO_GL,commissionFee,tChannel);

        String vatCollectionAccount = coreBankingService.getEventAccountNumber("VAT_".concat(cbaTransaction.getChannelEvent()));
        if (vatCollectionAccount == null || cbaTransaction.getVat().doubleValue() <= 0) {
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        }

        cbaTransaction.setCreditGLAccount(vatCollectionAccount);
        cbaTransaction.setAmount(cbaTransaction.getVat());

                                                
        return coreBankingService.processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

    }

    @Override
    public  ResponseEntity<?> processFeeAmountEntries(Optional<WalletTransAccount> transLog,
            Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken) {
        if(BigDecimal.ZERO.compareTo(transLog.get().getChargeAmount()) == 0){
            return new ResponseEntity<>(
            new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        }

        List<WalletTransaction> feeAmountEntries = relatedTrransactions.get().stream()
                .filter(accTrans -> 
                accTrans.getAcctNum().contains("NGN") 
                && accTrans.getTranAmount().equals(transLog.get().getChargeAmount()))
                .collect(Collectors.toList());
        
        if(feeAmountEntries.size() == 4){
            return new ResponseEntity<>(
            new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        }
        
        Provider provider = switchWalletService.getActiveProvider();
        List<WalletTransaction> customerWalletTransaction = relatedTrransactions.get().stream()
                .filter(accTrans -> !accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());
                
        String customerDepositGL = coreBankingService.getEventAccountNumber(EventCharge.WAYATRAN.name());
        String transitAccount = coreBankingService.getEventAccountNumber(transLog.get().getEventId());
        String debitAccountNumber; String creditAccountNumber;  
        if(transLog.get().getCreditAccountNumber().contains("NGN")){
            debitAccountNumber = customerDepositGL;
            creditAccountNumber = transLog.get().getCreditAccountNumber();
        }
        else if(transLog.get().getDebitAccountNumber().contains("NGN")){
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = transLog.get().getDebitAccountNumber();
        }
        else{
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = customerDepositGL;
        }
        String tChannel = TransactionChannel.WAYABANK.name();
        BigDecimal commissionFee = BigDecimal.ZERO;
        if(customerWalletTransaction.get(0).getTransChannel() != null)
            tChannel = customerWalletTransaction.get(0).getTransChannel();

        if(customerWalletTransaction.get(0).getCommissionFee() != null)
            commissionFee = customerWalletTransaction.get(0).getCommissionFee();

        CBATransaction cbaTransaction = new CBATransaction(transLog.get().getSenderName(), transLog.get().getBeneficiaryName(), userToken,
        customerWalletTransaction.get(0).getPaymentReference(), 
        transitAccount, creditAccountNumber, debitAccountNumber, null,
        customerWalletTransaction.get(0).getTranNarrate(),
        customerWalletTransaction.get(0).getTranCategory().name(), customerWalletTransaction.get(0).getTranType().name(),
        transLog.get().getTranAmount(), transLog.get().getChargeAmount(), transLog.get().getVatAmount(), 
        provider, transLog.get().getEventId(), customerWalletTransaction.get(0).getRelatedTransId(),
                CBAAction.MOVE_GL_TO_GL,commissionFee,tChannel);
 
        String chargeCollectionAccount = coreBankingService.getEventAccountNumber("INCOME_".concat(cbaTransaction.getChannelEvent()));
        if (chargeCollectionAccount == null || cbaTransaction.getCharge().doubleValue() <= 0) {
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        }

        cbaTransaction.setCreditGLAccount(chargeCollectionAccount);
        cbaTransaction.setAmount(cbaTransaction.getCharge());
                                        
        return coreBankingService.processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

    }

    @Override
    public  ResponseEntity<?> processActualAmountEntries(Optional<WalletTransAccount> transLog,
            Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken) {

        List<WalletTransaction> actualAmountEntries = relatedTrransactions.get().stream()
                .filter(accTrans -> 
                accTrans.getAcctNum().contains("NGN") 
                && accTrans.getTranAmount().equals(transLog.get().getTranAmount()))
                .collect(Collectors.toList());
        
        if(actualAmountEntries.size() == 4){
            return new ResponseEntity<>(
            new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        }
        
        Provider provider = switchWalletService.getActiveProvider();
        List<WalletTransaction> customerWalletTransaction = relatedTrransactions.get().stream()
                .filter(accTrans -> !accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());
                
        String customerDepositGL = coreBankingService.getEventAccountNumber(EventCharge.WAYATRAN.name());
        String transitAccount = coreBankingService.getEventAccountNumber(transLog.get().getEventId());
        String debitAccountNumber; String creditAccountNumber;  
        if(transLog.get().getCreditAccountNumber().contains("NGN")){
            debitAccountNumber = customerDepositGL;
            creditAccountNumber = transLog.get().getCreditAccountNumber();
        }
        else if(transLog.get().getDebitAccountNumber().contains("NGN")){
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = transLog.get().getDebitAccountNumber();
        }
        else{
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = customerDepositGL;
        }
        String tChannel = TransactionChannel.WAYABANK.name();
        BigDecimal commissionFee = BigDecimal.ZERO;
        if(customerWalletTransaction.get(0).getTransChannel() != null)
            tChannel = customerWalletTransaction.get(0).getTransChannel();

        if(customerWalletTransaction.get(0).getCommissionFee() != null)
            commissionFee = customerWalletTransaction.get(0).getCommissionFee();

        CBATransaction cbaTransaction = new CBATransaction(transLog.get().getSenderName(), transLog.get().getBeneficiaryName(), userToken,
        customerWalletTransaction.get(0).getPaymentReference(), 
        transitAccount, creditAccountNumber, debitAccountNumber, null,
        customerWalletTransaction.get(0).getTranNarrate(),
        customerWalletTransaction.get(0).getTranCategory().name(), customerWalletTransaction.get(0).getTranType().name(),
        transLog.get().getTranAmount(), transLog.get().getChargeAmount(), transLog.get().getVatAmount(), 
        provider, transLog.get().getEventId(), customerWalletTransaction.get(0).getRelatedTransId(),
                CBAAction.MOVE_GL_TO_GL,commissionFee,tChannel);
                                                
        return coreBankingService.processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

    }

}
