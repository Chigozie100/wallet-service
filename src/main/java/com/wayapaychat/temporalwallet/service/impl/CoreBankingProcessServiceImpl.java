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
        log.info("Running process: {}", processName);

        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (userToken == null) {
            log.error("Invalid user token");
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()), HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> processResponse = new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
        LocalDate processDate = getLastProcessedDate(processName);
        List<WalletTransaction> allCustomerTransaction = walletTransactionRepository.findByAllCustomerTransaction(processDate, "");

        for (WalletTransaction walletTransaction : allCustomerTransaction) {
            processResponse = validateEntries(processName, walletTransaction);
            if (!processResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Error validating entries for process {} transaction: {}", processName, walletTransaction.getTranId());
                return processResponse;
            }
        }

        WalletProcess walletProcess = new WalletProcess(processName, "SUCCESSFUL", "", processDate, LocalDateTime.now(), LocalDateTime.now(), userToken.getEmail(), userToken.getEmail());
        walletProcessRepository.save(walletProcess);

        log.info("Process {} completed successfully", processName);
        return processResponse;
    }


    @Override
    public ResponseEntity<?> validateEntries(String processName, WalletTransaction walletTransaction) {
        log.info("Validating entries for process: {} and transaction: {}", processName, walletTransaction.getTranId());

        Optional<List<WalletTransaction>> relatedTransactions = walletTransactionRepository.findByRelatedTrans(Long.toString(walletTransaction.getRelatedTransId()));
        if (relatedTransactions.isEmpty()) {
            log.error("No related transactions found for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        if (relatedTransactions.get().size() <= 1) {
            log.error("No entries found for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        Optional<WalletTransAccount> transLog = walletTransAccountRepository.findById(walletTransaction.getRelatedTransId());
        if (transLog.isEmpty()) {
            log.error("Unable to get transaction log for transaction {}", walletTransaction.getTranId());
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
        }

        switch (transLog.get().getEventId()) {
            case "WAYATRAN":
                if (relatedTransactions.get().size() < 10) {
                    log.error("Missing entries for transaction {}", walletTransaction.getTranId());
                    return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
                }
                break;
            case "NIP_PAYOUT":
                if (relatedTransactions.get().size() < 13) {
                    log.error("Missing entries for transaction {}", walletTransaction.getTranId());
                    return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
                }
                break;
            case "NIP_FUNDING":
                if (relatedTransactions.get().size() < 5) {
                    log.error("Missing entries for transaction {}", walletTransaction.getTranId());
                    return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
                }
                break;
            case "PAYSTACK_FUNDING":
                if (relatedTransactions.get().size() < 5) {
                    log.error("Missing entries for transaction {}", walletTransaction.getTranId());
                    return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
                }
                break;
            case "QUICKTELLER":
                if (relatedTransactions.get().size() < 5) {
                    log.error("Missing entries for transaction {}", walletTransaction.getTranId());
                    return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()), HttpStatus.BAD_REQUEST);
                }
                break;
            default:
                log.warn("Unexpected event ID: {}", transLog.get().getEventId());
        }

        log.info("Entries validated successfully for transaction {}", walletTransaction.getTranId());
        return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
    }

    @Override
    public LocalDate getLastProcessedDate(String processName) {
        log.info("Getting last processed date for process: {}", processName);

        WalletProcess lastProcess = walletProcessRepository.findFirstByProcessNameOrderByIdDesc(processName);
        if (!ObjectUtils.isEmpty(lastProcess)) {
            LocalDate lastDate = lastProcess.getProcessDate().plusDays(1);
            log.info("Last processed date found: {}", lastDate);
            return lastDate;
        }

        WalletTransaction transaction = walletTransactionRepository.findFirstByAcctNumNotLikeOrderByIdAsc("NGN%");
        if (!ObjectUtils.isEmpty(transaction)) {
            LocalDate lastDate = transaction.getTranDate();
            log.info("Last transaction date found: {}", lastDate);
            return lastDate;
        }

        LocalDate defaultDate = LocalDate.of(2022, 1, 1);
        log.warn("No last processed date found. Returning default date: {}", defaultDate);
        return defaultDate;
    }


    @Override
    public ResponseEntity<?> fixTransactionEntries(HttpServletRequest request, String transactionId) {
        log.info("Fixing transaction entries...");

        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (userToken == null) {
            log.error("Invalid user token. Returning error response.");
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> processResponse = new ResponseEntity<>(
                new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);

        Optional<WalletTransAccount> transLog = walletTransAccountRepository.findById(Long.valueOf(transactionId));
        if (transLog.isEmpty()) {
            log.error("Unable to get transaction log for transaction {}", transactionId);
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<List<WalletTransaction>> relatedTransactions = walletTransactionRepository.findByRelatedTrans(transactionId);
        if (relatedTransactions.isEmpty()) {
            log.error("No related transactions found for transaction {}", transactionId);
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        if (relatedTransactions.get().isEmpty()) {
            log.error("No entries found for transaction {}", transactionId);
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.PROCESSING_ERROR.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        log.info("Processing actual amount entries...");
        processResponse = processActualAmountEntries(transLog, relatedTransactions, userToken);
        if (!processResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error processing entries for actual amount transaction: {}", transactionId);
            return processResponse;
        }

        log.info("Processing fee amount entries...");
        processResponse = processFeeAmountEntries(transLog, relatedTransactions, userToken);
        if (!processResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error processing entries for fee amount transaction: {}", transactionId);
            return processResponse;
        }

        log.info("Processing VAT amount entries...");
        processResponse = processVatAmountEntries(transLog, relatedTransactions, userToken);
        if (!processResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error processing entries for VAT amount transaction: {}", transactionId);
            return processResponse;
        }

        log.info("Transaction entries fixed successfully. Returning response: {}", processResponse);
        return processResponse;
    }

    @Override
    public ResponseEntity<?> processVatAmountEntries(Optional<WalletTransAccount> transLog,
                                                     Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken) {
        log.info("Processing VAT amount entries...");

        if (BigDecimal.ZERO.compareTo(transLog.get().getVatAmount()) == 0) {
            log.info("VAT amount is zero. Returning success response.");
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                    HttpStatus.ACCEPTED);
        }

        List<WalletTransaction> vatAmountEntries = relatedTrransactions.get().stream()
                .filter(accTrans -> accTrans.getAcctNum().contains("NGN")
                        && accTrans.getTranAmount().equals(transLog.get().getVatAmount()))
                .collect(Collectors.toList());

        log.debug("Found {} VAT amount entries.", vatAmountEntries.size());

        if (vatAmountEntries.size() == 4) {
            log.info("All VAT amount entries found. Proceeding with response.");
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                    HttpStatus.ACCEPTED);
        }

        Provider provider = switchWalletService.getActiveProvider();
        List<WalletTransaction> customerWalletTransaction = relatedTrransactions.get().stream()
                .filter(accTrans -> !accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());

        log.debug("Filtered {} customer wallet transactions.", customerWalletTransaction.size());

        String customerDepositGL = coreBankingService.getEventAccountNumber(EventCharge.WAYATRAN.name());
        String transitAccount = coreBankingService.getEventAccountNumber(transLog.get().getEventId());

        log.debug("Customer deposit GL: {}, Transit account: {}", customerDepositGL, transitAccount);

        String debitAccountNumber;
        String creditAccountNumber;
        if (transLog.get().getCreditAccountNumber().contains("NGN")) {
            debitAccountNumber = customerDepositGL;
            creditAccountNumber = transLog.get().getCreditAccountNumber();
        } else if (transLog.get().getDebitAccountNumber().contains("NGN")) {
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = transLog.get().getDebitAccountNumber();
        } else {
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = customerDepositGL;
        }

        String tChannel = TransactionChannel.WAYABANK.name();
        if (customerWalletTransaction.get(0).getTransChannel() != null)
            tChannel = customerWalletTransaction.get(0).getTransChannel();

        log.debug("Debit account number: {}, Credit account number: {}, Transaction channel: {}", debitAccountNumber,
                creditAccountNumber, tChannel);

        CBATransaction cbaTransaction = new CBATransaction(transLog.get().getSenderName(),
                transLog.get().getBeneficiaryName(), userToken, customerWalletTransaction.get(0).getPaymentReference(),
                transitAccount, creditAccountNumber, debitAccountNumber, null,
                customerWalletTransaction.get(0).getTranNarrate(),
                customerWalletTransaction.get(0).getTranCategory().name(),
                customerWalletTransaction.get(0).getTranType().name(), transLog.get().getTranAmount(),
                transLog.get().getChargeAmount(), transLog.get().getVatAmount(), provider, transLog.get().getEventId(),
                customerWalletTransaction.get(0).getRelatedTransId(), CBAAction.MOVE_GL_TO_GL, tChannel);

        String vatCollectionAccount = coreBankingService
                .getEventAccountNumber("VAT_".concat(cbaTransaction.getChannelEvent()));
        if (vatCollectionAccount == null || cbaTransaction.getVat().doubleValue() <= 0) {
            log.info("VAT collection account not found or VAT amount is zero. Returning success response.");
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                    HttpStatus.ACCEPTED);
        }

        log.debug("VAT collection account: {}", vatCollectionAccount);

        cbaTransaction.setCreditGLAccount(vatCollectionAccount);
        cbaTransaction.setAmount(cbaTransaction.getVat());

        log.info("Sending request to core banking service for processing...");
        return coreBankingService.processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

    }


    @Override
    public ResponseEntity<?> processFeeAmountEntries(Optional<WalletTransAccount> transLog,
                                                     Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken) {
        log.info("Processing fee amount entries...");

        if (BigDecimal.ZERO.compareTo(transLog.get().getChargeAmount()) == 0) {
            log.info("Charge amount is zero. Returning success response.");
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                    HttpStatus.ACCEPTED);
        }

        List<WalletTransaction> feeAmountEntries = relatedTrransactions.get().stream()
                .filter(accTrans -> accTrans.getAcctNum().contains("NGN")
                        && accTrans.getTranAmount().equals(transLog.get().getChargeAmount()))
                .collect(Collectors.toList());

        log.debug("Found {} fee amount entries.", feeAmountEntries.size());

        if (feeAmountEntries.size() == 4) {
            log.info("All fee amount entries found. Proceeding with response.");
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                    HttpStatus.ACCEPTED);
        }

        Provider provider = switchWalletService.getActiveProvider();
        List<WalletTransaction> customerWalletTransaction = relatedTrransactions.get().stream()
                .filter(accTrans -> !accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());

        log.debug("Filtered {} customer wallet transactions.", customerWalletTransaction.size());

        String customerDepositGL = coreBankingService.getEventAccountNumber(EventCharge.WAYATRAN.name());
        String transitAccount = coreBankingService.getEventAccountNumber(transLog.get().getEventId());

        log.debug("Customer deposit GL: {}, Transit account: {}", customerDepositGL, transitAccount);

        String debitAccountNumber;
        String creditAccountNumber;
        if (transLog.get().getCreditAccountNumber().contains("NGN")) {
            debitAccountNumber = customerDepositGL;
            creditAccountNumber = transLog.get().getCreditAccountNumber();
        } else if (transLog.get().getDebitAccountNumber().contains("NGN")) {
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = transLog.get().getDebitAccountNumber();
        } else {
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = customerDepositGL;
        }

        String tChannel = TransactionChannel.WAYABANK.name();
        if (customerWalletTransaction.get(0).getTransChannel() != null)
            tChannel = customerWalletTransaction.get(0).getTransChannel();

        log.debug("Debit account number: {}, Credit account number: {}, Transaction channel: {}", debitAccountNumber,
                creditAccountNumber, tChannel);

        CBATransaction cbaTransaction = new CBATransaction(transLog.get().getSenderName(),
                transLog.get().getBeneficiaryName(), userToken, customerWalletTransaction.get(0).getPaymentReference(),
                transitAccount, creditAccountNumber, debitAccountNumber, null,
                customerWalletTransaction.get(0).getTranNarrate(),
                customerWalletTransaction.get(0).getTranCategory().name(),
                customerWalletTransaction.get(0).getTranType().name(), transLog.get().getTranAmount(),
                transLog.get().getChargeAmount(), transLog.get().getVatAmount(), provider, transLog.get().getEventId(),
                customerWalletTransaction.get(0).getRelatedTransId(), CBAAction.MOVE_GL_TO_GL, tChannel);

        String chargeCollectionAccount = coreBankingService
                .getEventAccountNumber("INCOME_".concat(cbaTransaction.getChannelEvent()));
        if (chargeCollectionAccount == null || cbaTransaction.getCharge().doubleValue() <= 0) {
            log.info("Charge collection account not found or charge amount is zero. Returning success response.");
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                    HttpStatus.ACCEPTED);
        }

        log.debug("Charge collection account: {}", chargeCollectionAccount);

        cbaTransaction.setCreditGLAccount(chargeCollectionAccount);
        cbaTransaction.setAmount(cbaTransaction.getCharge());

        log.info("Sending request to core banking service for processing...");
        return coreBankingService.processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);

    }


    @Override
    public ResponseEntity<?> processActualAmountEntries(Optional<WalletTransAccount> transLog,
                                                        Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken) {
        log.info("Processing actual amount entries...");

        List<WalletTransaction> actualAmountEntries = relatedTrransactions.get().stream()
                .filter(accTrans -> accTrans.getAcctNum().contains("NGN")
                        && accTrans.getTranAmount().equals(transLog.get().getTranAmount()))
                .collect(Collectors.toList());

        log.debug("Found {} actual amount entries.", actualAmountEntries.size());

        if (actualAmountEntries.size() == 4) {
            log.info("All actual amount entries found. Proceeding with response.");
            return new ResponseEntity<>(new SuccessResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()),
                    HttpStatus.ACCEPTED);
        }

        Provider provider = switchWalletService.getActiveProvider();
        List<WalletTransaction> customerWalletTransaction = relatedTrransactions.get().stream()
                .filter(accTrans -> !accTrans.getAcctNum().contains("NGN")).collect(Collectors.toList());

        log.debug("Filtered {} customer wallet transactions.", customerWalletTransaction.size());

        String customerDepositGL = coreBankingService.getEventAccountNumber(EventCharge.WAYATRAN.name());
        String transitAccount = coreBankingService.getEventAccountNumber(transLog.get().getEventId());

        log.debug("Customer deposit GL: {}, Transit account: {}", customerDepositGL, transitAccount);

        String debitAccountNumber;
        String creditAccountNumber;
        if (transLog.get().getCreditAccountNumber().contains("NGN")) {
            debitAccountNumber = customerDepositGL;
            creditAccountNumber = transLog.get().getCreditAccountNumber();
        } else if (transLog.get().getDebitAccountNumber().contains("NGN")) {
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = transLog.get().getDebitAccountNumber();
        } else {
            creditAccountNumber = customerDepositGL;
            debitAccountNumber = customerDepositGL;
        }

        log.debug("Debit account number: {}, Credit account number: {}", debitAccountNumber, creditAccountNumber);

        String tChannel = TransactionChannel.WAYABANK.name();
        if (customerWalletTransaction.get(0).getTransChannel() != null)
            tChannel = customerWalletTransaction.get(0).getTransChannel();

        log.debug("Transaction channel: {}", tChannel);

        CBATransaction cbaTransaction = new CBATransaction(transLog.get().getSenderName(),
                transLog.get().getBeneficiaryName(), userToken, customerWalletTransaction.get(0).getPaymentReference(),
                transitAccount, creditAccountNumber, debitAccountNumber, null,
                customerWalletTransaction.get(0).getTranNarrate(),
                customerWalletTransaction.get(0).getTranCategory().name(),
                customerWalletTransaction.get(0).getTranType().name(), transLog.get().getTranAmount(),
                transLog.get().getChargeAmount(), transLog.get().getVatAmount(), provider, transLog.get().getEventId(),
                customerWalletTransaction.get(0).getRelatedTransId(), CBAAction.MOVE_GL_TO_GL, tChannel);

        log.info("Sending request to core banking service for processing...");
        return coreBankingService.processCBATransactionGLDoubleEntryWithTransit(cbaTransaction);
    }


}
