package com.wayapaychat.temporalwallet.service;

import org.springframework.http.ResponseEntity;

public interface TransactionCountService {
    ResponseEntity<?> getUserCount(String userId);

    ResponseEntity<?> getAllUserCount();

    void makeCount(String userId, String transactionRef);
}
