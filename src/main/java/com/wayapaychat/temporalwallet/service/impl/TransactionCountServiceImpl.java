package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.dto.TransactionCountDto;
import com.wayapaychat.temporalwallet.pojo.TransactionReport;
import com.wayapaychat.temporalwallet.entity.TransactionCount;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.repository.TransactionCountRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.service.MessageQueueProducer;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.util.Constant;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionCountServiceImpl implements TransactionCountService {

    private final TransactionCountRepository transactionCountRepository;
    private final WalletUserRepository walletUserRepository;
    private final MessageQueueProducer MessageQueueProducer;

    @Autowired
    public TransactionCountServiceImpl(TransactionCountRepository transactionCountRepository, WalletUserRepository walletUserRepository, com.wayapaychat.temporalwallet.service.MessageQueueProducer messageQueueProducer) {
        this.transactionCountRepository = transactionCountRepository;
        this.walletUserRepository = walletUserRepository;
        MessageQueueProducer = messageQueueProducer;
    }

    @Override
    public ResponseEntity<?> getUserCount(String userId) {
        List<TransactionCountDto> allList = new ArrayList<>();
        List<TransactionCountDto> trdto = transactionCountRepository.findSurveyCount();
        for (TransactionCountDto transactionCountDto : trdto) {
            WalletUser user = walletUserRepository.findByUserId(Long.parseLong(transactionCountDto.getUserId()));
            allList.add(new TransactionCountDto(transactionCountDto.getUserId(), transactionCountDto.getTotalCount(), user));
        }

        return new ResponseEntity<>(new SuccessResponse(allList), HttpStatus.ACCEPTED);
    }

    private List<TransactionCountDto> getGetTransactionConuntList() {
        return new ArrayList<>();
    }

    @Override
    public ResponseEntity<?> getAllUserCount() {

        List<TransactionCountDto> transactionCountDtos = getGetTransactionConuntList();
        List<TransactionCountDto> trdto = transactionCountRepository.findSurveyCount();
        for (TransactionCountDto transactionCountDto : trdto) {
            WalletUser user = walletUserRepository.findByUserId(Long.parseLong(transactionCountDto.getUserId()));
            transactionCountDtos.add(new TransactionCountDto(transactionCountDto.getUserId(), transactionCountDto.getTotalCount(), user));
        }

        return new ResponseEntity<>(transactionCountDtos, HttpStatus.ACCEPTED);
    }

    @Override
    public void makeCount(String userId, String transactionRef) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("transactionId", transactionRef);

        // push to kafka
        MessageQueueProducer.send(Constant.REFERRAL_TRANSACTION_COUNT, map);
    }

    @Override
    public void transReport(TransactionReport report) {
        // push to kafka
        MessageQueueProducer.send(Constant.STATEMENT_REPORTING, report);
    }
}
