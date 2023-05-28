package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.dto.AccountSumary;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.TransactionReport;
import org.springframework.http.ResponseEntity;

public interface TransactionCountService {
    ResponseEntity<?> getUserCount(String userId);

    ResponseEntity<?> getAllUserCount();

//    void makeCount(String userId, String transactionRef);
    
    void transReport(TransactionReport report);
    void pushTransactionToEventQueue(AccountSumary accountSumary, CBAEntryTransaction transaction,double currentBalance);
}
