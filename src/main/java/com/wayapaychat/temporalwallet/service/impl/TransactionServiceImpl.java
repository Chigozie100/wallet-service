
 package com.wayapaychat.temporalwallet.service.impl;

 import com.wayapaychat.temporalwallet.exception.CustomException;
 import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
 import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo;
 import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo2;
 import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
 import com.wayapaychat.temporalwallet.service.TransactionService;
 import com.wayapaychat.temporalwallet.util.ErrorResponse;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Service;

 import java.util.Map;

 @Service
 @Slf4j
public class TransactionServiceImpl implements TransactionService {

     private final WalletTransactionRepository walletTransactionRepository;

     @Autowired
     public TransactionServiceImpl(WalletTransactionRepository walletTransactionRepository) {
         this.walletTransactionRepository = walletTransactionRepository;
     }


     @Override
     public ResponseEntity<?> transactAmount(TransactionPojo transactionPojo) {
         return null;
     }

     @Override
     public ResponseEntity<?> transferTransaction(TransactionTransferPojo transactionTransferPojo) {
         return null;
     }

     @Override
     public ResponseEntity<?> transferTransactionWithId(TransactionTransferPojo2 transactionTransferPojo2) {
         return null;
     }


     @Override
     public boolean processPayment(Map<String, Object> map) {

         String fromAccountNumber = (String) map.get("debitAccountNumber");
         String toAccountNumber = (String) map.get("benefAccountNumber");
         String eventId = (String) map.get("eventId");
         String transType = (String) map.get("transType");
         String transCategory = (String) map.get("transCategory");
         String tranCrncy = (String) map.get("tranCrncy");

         if(fromAccountNumber.equals(toAccountNumber)) {
             throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.EXPECTATION_FAILED);
         }

         if (fromAccountNumber.trim().equals(toAccountNumber.trim())) {
             log.info(toAccountNumber + "|" + fromAccountNumber);
             throw new CustomException("DEBIT AND CREDIT ON THE SAME ACCOUNT",HttpStatus.EXPECTATION_FAILED);
         }


         // save transaction
         // check KYC
         // check
         // check fraud rules
         return false;
     }
 }
