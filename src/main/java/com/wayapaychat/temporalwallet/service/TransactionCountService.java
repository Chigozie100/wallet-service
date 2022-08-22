package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.entity.TransactionCount;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TransactionCountService {
    long getCount(String userId);

    ResponseEntity<?> getAllUserCount();

    void makeCount(String userId, String transactionRef);
}
