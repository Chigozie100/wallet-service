package com.wayapaychat.temporalwallet.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.pojo.MyData;

public interface CoreBankingProcessService {

    ResponseEntity<?> runProcess(String processName);

    ResponseEntity<?> validateEntries(String processName, WalletTransaction walletTransaction);

    LocalDate getLastProcessedDate(String processName);

    ResponseEntity<?> fixTransactionEntries(HttpServletRequest request, String transactionId);

    ResponseEntity<?> processVatAmountEntries(Optional<WalletTransAccount> transLog,
            Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken);

    ResponseEntity<?> processFeeAmountEntries(Optional<WalletTransAccount> transLog,
            Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken);

    ResponseEntity<?> processActualAmountEntries(Optional<WalletTransAccount> transLog,
            Optional<List<WalletTransaction>> relatedTrransactions, MyData userToken);
    
}
