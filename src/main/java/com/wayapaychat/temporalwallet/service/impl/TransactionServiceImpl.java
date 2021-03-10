package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.Account;
import com.wayapaychat.temporalwallet.entity.Transaction;
import com.wayapaychat.temporalwallet.enumm.TransactionType;
import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo;
import com.wayapaychat.temporalwallet.repository.AccountRepository;
import com.wayapaychat.temporalwallet.repository.TransactionRepository;
import com.wayapaychat.temporalwallet.repository.UserRepository;
import com.wayapaychat.temporalwallet.service.TransactionService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.RandomGenerators;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wayapaychat.temporalwallet.util.Constant.WAYA_SETTLEMENT_ACCOUNT_NO;


@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RandomGenerators randomGenerators;

    @Override
    @Transactional
    public ResponseEntity transactAmount(TransactionPojo transactionPojo) {
        Account account = accountRepository.findByAccountNo(transactionPojo.getAccountNo());
        Account wayaAccount = accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
        if (account == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.BAD_REQUEST);
        }
        if (transactionPojo.getAmount() < 1) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Amount"), HttpStatus.BAD_REQUEST);
        }
        // Register Transaction

        String ref = randomGenerators.generateAlphanumeric(12);
        if (transactionPojo.getTransactionType() == TransactionType.CREDIT){

            // Handle Credit User Account
            Transaction transaction = new Transaction();
            transaction.setTransactionType(transactionPojo.getTransactionType());
            transaction.setAccount(account);
            transaction.setAmount(transactionPojo.getAmount());
            transaction.setRefCode(ref);

            transactionRepository.save(transaction);
            account.setBalance(account.getBalance() + transactionPojo.getAmount());
            List<Transaction> transactionList = account.getTransactions();
            transactionList.add(transaction);
            accountRepository.save(account);

            // Handle Debit Waya Account
            Transaction transaction2 = new Transaction();
            transaction2.setTransactionType(TransactionType.DEBIT);
            transaction2.setAccount(wayaAccount);
            transaction2.setAmount(transactionPojo.getAmount());
            transaction2.setRefCode(ref);

            transactionRepository.save(transaction2);
            wayaAccount.setBalance(wayaAccount.getBalance() - transactionPojo.getAmount());
            List<Transaction> transactionList2 = wayaAccount.getTransactions();
            transactionList2.add(transaction2);
        }
        else {

            if (account.getBalance() < transactionPojo.getAmount()) {
                return new ResponseEntity<>(new ErrorResponse("Insufficient Balance"), HttpStatus.BAD_REQUEST);
            }

            // Handle Debit User Account
            Transaction transaction = new Transaction();
            transaction.setTransactionType(TransactionType.DEBIT);
            transaction.setAccount(account);
            transaction.setAmount(transactionPojo.getAmount());
            transaction.setRefCode(ref);

            transactionRepository.save(transaction);
            account.setBalance(account.getBalance() - transactionPojo.getAmount());
            List<Transaction> transactionList = account.getTransactions();
            transactionList.add(transaction);
            accountRepository.save(account);

            // Handle Debit Waya Account
            Transaction transaction2 = new Transaction();
            transaction2.setTransactionType(TransactionType.CREDIT);
            transaction2.setAccount(wayaAccount);
            transaction2.setAmount(transactionPojo.getAmount());
            transaction2.setRefCode(ref);

            transactionRepository.save(transaction2);
            wayaAccount.setBalance(wayaAccount.getBalance() + transactionPojo.getAmount());
            List<Transaction> transactionList2 = wayaAccount.getTransactions();
            transactionList2.add(transaction2);
            accountRepository.save(wayaAccount);
        }
        return new ResponseEntity<>(new SuccessResponse("Success.", null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity transferTransaction(TransactionTransferPojo transactionTransferPojo) {

        Account fromAccount = accountRepository.findByAccountNo(transactionTransferPojo.getFromAccount());
        Account toAccount = accountRepository.findByAccountNo(transactionTransferPojo.getToAccount());
        String ref = randomGenerators.generateAlphanumeric(12);
        if (fromAccount == null || toAccount == null) {
            return new ResponseEntity<>(new ErrorResponse("Possible Invalid Account"), HttpStatus.BAD_REQUEST);
        }
        if (transactionTransferPojo.getAmount() < 1) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Amount"), HttpStatus.BAD_REQUEST);
        }
        if (fromAccount.getBalance() < transactionTransferPojo.getAmount()) {
            return new ResponseEntity<>(new ErrorResponse("Insufficient Fund"), HttpStatus.BAD_REQUEST);
        }

        // Handle Debit User Account
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setAccount(fromAccount);
        transaction.setAmount(transactionTransferPojo.getAmount());
        transaction.setRefCode(ref);

        transactionRepository.save(transaction);
        fromAccount.setBalance(fromAccount.getBalance() - transactionTransferPojo.getAmount());
        List<Transaction> transactionList = fromAccount.getTransactions();
        transactionList.add(transaction);
        accountRepository.save(fromAccount);

        // Handle Debit Waya Account
        Transaction transaction2 = new Transaction();
        transaction2.setTransactionType(TransactionType.CREDIT);
        transaction2.setAccount(toAccount);
        transaction2.setAmount(transactionTransferPojo.getAmount());
        transaction2.setRefCode(ref);

        transactionRepository.save(transaction2);
        toAccount.setBalance(toAccount.getBalance() + transactionTransferPojo.getAmount());
        List<Transaction> transactionList2 = toAccount.getTransactions();
        transactionList2.add(transaction2);
        accountRepository.save(toAccount);

        return new ResponseEntity<>(new SuccessResponse("Transfer Completed.", null), HttpStatus.OK);

    }
}
