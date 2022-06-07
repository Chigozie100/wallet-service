package com.wayapaychat.temporalwallet.service;

public interface TransactionCountService {
    long getCount(String userId);

    void makeCount(String userId, String transactionRef);
}
