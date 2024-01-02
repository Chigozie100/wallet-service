package com.wayapaychat.temporalwallet.service;


import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.WalletTransStatus;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.CBATransaction;
import com.wayapaychat.temporalwallet.pojo.MyData;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

public interface CoreBankingService {

    ResponseEntity<?> externalCBACreateAccount(WalletUser userInfo, WalletAccount sAcct, Provider provider);

    ResponseEntity<?> createAccount(WalletUser userInfo, WalletAccount sAcct);

    ResponseEntity<?> getAccountDetails(String accountNo);

    ResponseEntity<?> creditAccount(CBAEntryTransaction transactionPojo);

    ResponseEntity<?> debitAccount(CBAEntryTransaction transactionPojo);

    ResponseEntity<?> processExternalCBATransactionCustomerEntry(CBATransaction cbaTransaction);

    ResponseEntity<?> processExternalCBATransactionGLDoubleEntry(CBATransaction cbaTransaction, boolean reversal);

    ResponseEntity<?> processCBATransactionGLDoubleEntry(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBATransactionGLDoubleEntryWithTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBATransactionCustomerEntry(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBACustomerDepositTransactionWithDoubleEntryTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBACustomerWithdrawTransactionWithDoubleEntryTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processCBACustomerTransferTransactionWithDoubleEntryTransit(CBATransaction cbaTransaction);

    ResponseEntity<?> processTransaction(TransferTransactionDTO transferTransactionDTO, String channelEventId, HttpServletRequest request);

    ResponseEntity<?> processTransactionReversal(ReverseTransactionDTO reverseDTO, HttpServletRequest request);

    ResponseEntity<?> processCustomerTransactionReversalByRef(ReverseTransactionDTO reverseDTO, HttpServletRequest request);

    ResponseEntity<?> reverseCustomerTransaction(MyData userToken, Provider provider, WalletTransaction walletTransaction);

    ResponseEntity<?> reverseGLTransaction(MyData userToken, Provider provider, List<WalletTransaction> list);

    boolean isCustomerTransaction(List<WalletTransaction> list);

    String getEventAccountNumber(String channelEventId);

    Long logTransaction(String receiverName,String senderName,String fromAccountNumber, String toAccountNumber, BigDecimal amount, BigDecimal chargeAmount, BigDecimal vatAmount,
                                String transCategory, String tranCrncy, String eventId, WalletTransStatus status);

    void updateTransactionLog(Long tranId, WalletTransStatus status);

    void sendTransactionNotification(String subject, String accountName, CBAEntryTransaction transactionPojo, double currentBalance, String tranType);

    void addLien(WalletAccount account, BigDecimal amount);

    void removeLien(String account, BigDecimal amount);

    ResponseEntity<?> securityCheckOwner(String accountNumber);

    ResponseEntity<?> securityCheck(String accountNumber, BigDecimal amount, HttpServletRequest request);

    BigDecimal computeTotalTransactionFee(String accountNumber, BigDecimal amount,  String eventId);

    BigDecimal computeTransactionFee(String accountNumber, BigDecimal amount,  String eventId);

    BigDecimal computeVatFee(BigDecimal fee,  String eventId);

}
