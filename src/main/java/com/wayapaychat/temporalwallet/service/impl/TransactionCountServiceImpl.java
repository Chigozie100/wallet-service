package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.TransactionCount;
import com.wayapaychat.temporalwallet.repository.TransactionCountRepository;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TransactionCountServiceImpl implements TransactionCountService {

    private final TransactionCountRepository transactionCountRepository;

    @Autowired
    public TransactionCountServiceImpl(TransactionCountRepository transactionCountRepository) {
        this.transactionCountRepository = transactionCountRepository;
    }


    @Override
    public long getCount(String userId) {
        return transactionCountRepository.countByUserId(userId);
    }

    @Override
    public void makeCount(String userId, String transactionRef) {
        TransactionCount transactionCount = new TransactionCount();
        transactionCount.setCreatedAt(new Date());
        transactionCount.setTransactionReference(transactionRef);
        transactionCount.setUserId(userId);
        transactionCountRepository.save(transactionCount);
    }


}
