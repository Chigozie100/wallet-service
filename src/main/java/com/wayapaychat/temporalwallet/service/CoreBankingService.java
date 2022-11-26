package com.wayapaychat.temporalwallet.service;


import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.WalletTransStatus;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TransactionPojo;

import java.math.BigDecimal;
 

import org.springframework.http.ResponseEntity;

public interface CoreBankingService {

    ResponseEntity<?> createAccount(TransactionPojo transactionPojo);

    ResponseEntity<?> creditAccount(CBAEntryTransaction transactionPojo);

    ResponseEntity<?> debitAccount(CBAEntryTransaction transactionPojo);

    ResponseEntity<?> processCBATransactionDoubleEntry(MyData userToken, String paymentReference, String fromAccount, String toAccount, String narration, CategoryType category, BigDecimal amount, Provider provider);

    ResponseEntity<?> processExternalCBATransactionDoubleEntry(String paymentReference, String fromAccount, String toAccount, String narration, CategoryType category, BigDecimal amount, Provider provider);

    ResponseEntity<?> processCBATransactionDoubleEntryWithTransit(MyData userToken, String paymentReference, String transitAccount, String fromAccount, String toAccount, String narration, String category, BigDecimal amount, Provider provider);

    ResponseEntity<?> transfer(TransferTransactionDTO transferTransactionDTO, String channelEventId);

    ResponseEntity<?> applyCharge(String transitAccount, String debitAccountNumber, String tranNarration, String transactionCategory, BigDecimal doubleValue, Provider provider);

    Long logTransaction(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String transCategory,String tranCrncy, WalletTransStatus status);
    
    void updateTransactionLog(Long tranId, WalletTransStatus status);

    void addLien(WalletAccount account, BigDecimal amount); 

    ResponseEntity<?> securityCheck(String accountNumber, BigDecimal amount);

}
