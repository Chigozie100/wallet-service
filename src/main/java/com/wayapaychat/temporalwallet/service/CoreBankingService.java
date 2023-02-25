package com.wayapaychat.temporalwallet.service;


import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.WalletTransStatus;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.CBATransaction;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

public interface CoreBankingService {
    ResponseEntity<?> externalCBACreateAccount(WalletUser userInfo, WalletAccount sAcct, Provider provider);

    ResponseEntity<?> createAccount(WalletUser userInfo, WalletAccount sAcct);

    ResponseEntity<?> getAccountDetails(String accountNo);

    ResponseEntity<?> creditAccount(CBAEntryTransaction transactionPojo);

    ResponseEntity<?> debitAccount(CBAEntryTransaction transactionPojo);

    ResponseEntity<?> processExternalCBATransactionGLDoubleEntry(CBATransaction cbaTransaction, boolean reversal);

    ResponseEntity<?> processCBATransactionGLDoubleEntry(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBATransactionGLDoubleEntryWithTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processExternalCBATransactionCustomerEntry(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBATransactionCustomerEntry(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBACustomerDepositTransactionWithDoubleEntryTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBACustomerWithdrawTransactionWithDoubleEntryTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBACustomerTransferTransactionWithDoubleEntryTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processTransaction(TransferTransactionDTO transferTransactionDTO, String channelEventId, HttpServletRequest request);

    String getEventAccountNumber(String channelEventId);

    Long logTransaction(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String transCategory,String tranCrncy, WalletTransStatus status);

    void updateTransactionLog(Long tranId, WalletTransStatus status);

    void addLien(WalletAccount account, BigDecimal amount);

    void logNotification(String subject, CBAEntryTransaction transactionPojo, double currentBalance, String tranType);

    ResponseEntity<?> securityCheckOwner(String accountNumber);

    ResponseEntity<?> securityCheck(String accountNumber, BigDecimal amount, HttpServletRequest request);

    BigDecimal computeTotalTransactionFee(String accountNumber, BigDecimal amount,  String eventId);

    BigDecimal computeTransactionFee(String accountNumber, BigDecimal amount,  String eventId);

    BigDecimal computeVatFee(BigDecimal fee,  String eventId);
}
