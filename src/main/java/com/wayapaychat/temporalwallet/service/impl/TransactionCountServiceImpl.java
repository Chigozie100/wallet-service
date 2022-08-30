package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.dto.TransactionCountDto;
import com.wayapaychat.temporalwallet.entity.TransactionCount;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.repository.TransactionCountRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TransactionCountServiceImpl implements TransactionCountService {

    private final TransactionCountRepository transactionCountRepository;
    private final WalletUserRepository walletUserRepository;

    @Autowired
    public TransactionCountServiceImpl(TransactionCountRepository transactionCountRepository, WalletUserRepository walletUserRepository) {
        this.transactionCountRepository = transactionCountRepository;
        this.walletUserRepository = walletUserRepository;
    }


    @Override
    public ResponseEntity<?> getUserCount(String userId) {
        List<TransactionCountDto> allList = new ArrayList<>();
        List<TransactionCountDto> trdto =  transactionCountRepository.findSurveyCount();
        for (TransactionCountDto transactionCountDto :trdto){
            WalletUser user = walletUserRepository.findByUserId(Long.parseLong(transactionCountDto.getUserId()));
            allList.add(new TransactionCountDto(transactionCountDto.getUserId(), transactionCountDto.getTotalCount(), user));
        }

        return new ResponseEntity<>(new SuccessResponse(allList),HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> getAllUserCount() {

        List<TransactionCountDto> allList = new ArrayList<>();
        List<TransactionCountDto> trdto =  transactionCountRepository.findSurveyCount();
        for (TransactionCountDto transactionCountDto :trdto){
            WalletUser user = walletUserRepository.findByUserId(Long.parseLong(transactionCountDto.getUserId()));
            allList.add(new TransactionCountDto(transactionCountDto.getUserId(), transactionCountDto.getTotalCount(), user));
        }

        return new ResponseEntity<>(allList,HttpStatus.ACCEPTED);
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
