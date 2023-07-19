package com.wayapaychat.temporalwallet.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.waya.security.auth.pojo.UserIdentityData;
import com.wayapaychat.temporalwallet.entity.WalletProcess;
import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.enumm.ResponseCodes;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.repository.WalletProcessRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
import com.wayapaychat.temporalwallet.service.CoreBankingProcessService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoreBankingProcessServiceImpl implements CoreBankingProcessService {

    private final WalletTransAccountRepository walletTransAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletProcessRepository walletProcessRepository;

    @Autowired
    public CoreBankingProcessServiceImpl(WalletTransAccountRepository walletTransAccountRepository,
            WalletTransactionRepository walletTransactionRepository, WalletProcessRepository walletProcessRepository) {
        this.walletTransAccountRepository = walletTransAccountRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletProcessRepository = walletProcessRepository;
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
                new ErrorResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
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
                new ErrorResponse(ResponseCodes.TRANSACTION_SUCCESSFUL.getValue()), HttpStatus.ACCEPTED);
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

}
