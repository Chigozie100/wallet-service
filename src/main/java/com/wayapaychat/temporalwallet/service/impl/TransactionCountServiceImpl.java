package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.dto.AccountSumary;
import com.wayapaychat.temporalwallet.dto.TransactionCountDto;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDataDto;
import com.wayapaychat.temporalwallet.pojo.CBAEntryTransaction;
import com.wayapaychat.temporalwallet.pojo.TransactionReport;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.repository.TransactionCountRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.service.MessageQueueProducer;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.util.Constant;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service @Slf4j
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
    public ResponseEntity<?> getUserCount(String userId,String profileId) {
        List<TransactionCountDto> allList = new ArrayList<>();
        List<TransactionCountDto> trdto = transactionCountRepository.findSurveyCount();
        for (TransactionCountDto transactionCountDto : trdto) {
            WalletUser user = walletUserRepository.findByUserIdAndProfileId(Long.parseLong(transactionCountDto.getUserId()),profileId);
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
//            WalletUser user = walletUserRepository.findByUserId(Long.parseLong(transactionCountDto.getUserId()));
            List<WalletUser> walletUserList = walletUserRepository.findAllWalletByUserId(Long.parseLong(transactionCountDto.getUserId()));
            for (WalletUser user: walletUserList){
                transactionCountDtos.add(new TransactionCountDto(transactionCountDto.getUserId(), transactionCountDto.getTotalCount(), user));
            }
        }
        return new ResponseEntity<>(transactionCountDtos, HttpStatus.ACCEPTED);
    }

    @Override
    public void pushTransactionToEventQueue(AccountSumary accountSumary, CBAEntryTransaction transaction,double currentBalance,String transType) {
        WalletTransactionDataDto referralTransDto = new WalletTransactionDataDto();
        referralTransDto.setTransactionReferenceNumber(transaction.getPaymentReference());
        referralTransDto.setUserId(accountSumary.getUId());
        referralTransDto.setAccountNo(accountSumary.getAccountNo());
        referralTransDto.setEmail(accountSumary.getEmail());
        referralTransDto.setPhone(accountSumary.getPhone());
        referralTransDto.setCustName(accountSumary.getCustName());
        referralTransDto.setCurrentBalance(currentBalance);
        BeanUtils.copyProperties(transaction, referralTransDto);
        referralTransDto.setCommissionFee(transaction.getFee());
        referralTransDto.setTransactionChannel(transaction.getTransactionChannel());
        referralTransDto.setPartTranType(transType);
        // push to kafka
        log.info("::::PUSHING TRANSACTION DTO TO REFERRAL SERVICE KAFKA QUEUE::: {}",referralTransDto);
        CompletableFuture.runAsync(() ->{
            MessageQueueProducer.send(Constant.TRANSACTION_KAFKA_QUEUE, referralTransDto);
            log.info(":::SUCCESS, PUBLISHING REFERRAL TXN TO REFERRAL-SERVICE KAFKA QUEUE::::: {}",referralTransDto);
        });
    }

    @Override
    public void transReport(TransactionReport report) {
        // push to kafka
        MessageQueueProducer.send(Constant.STATEMENT_REPORTING, report);
    }
}
