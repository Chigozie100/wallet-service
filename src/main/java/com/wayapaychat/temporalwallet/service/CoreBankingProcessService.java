package com.wayapaychat.temporalwallet.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.entity.WalletTransaction;

public interface CoreBankingProcessService {

    ResponseEntity<?> runProcess(String processName);

    ResponseEntity<?> validateEntries(String processName, WalletTransaction walletTransaction);

    LocalDate getLastProcessedDate(String processName);
    
}
