package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.Account;
import com.wayapaychat.temporalwallet.entity.User;
import com.wayapaychat.temporalwallet.enumm.AccountType;
import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.repository.AccountRepository;
import com.wayapaychat.temporalwallet.repository.UserRepository;
import com.wayapaychat.temporalwallet.service.AccountService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.RandomGenerators;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RandomGenerators randomGenerators;


    @Override
    public ResponseEntity createAccount(AccountPojo accountPojo) {
        User user = userRepository.findById(accountPojo.getUserId()).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
        }
        Account account = new ModelMapper().map(accountPojo, Account.class);
        account.setUser(user);
        account.setAccountName(user.getUserId()+randomGenerators.generateAlphabet(7));
        account.setAccountType(AccountType.SAVINGS);
        account.setAccountNo(randomGenerators.generateAlphanumeric(10));
        try {
            userRepository.save(user);
            accountRepository.save(account);
            List<Account> userAccount = user.getAccounts();
            userAccount.add(account);
            userRepository.save(user);
            return new ResponseEntity<>(new SuccessResponse("Account Created Successfully.", account), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity getUserAccountList(long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new SuccessResponse("Success.", user.getAccounts()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getAllAccount() {
        List<Account> accountList = accountRepository.findAll();
        return new ResponseEntity<>(new SuccessResponse("Success.", accountList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getAccountInfo(String accountNo) {
        Account account = accountRepository.findByAccountNo(accountNo);
        return new ResponseEntity<>(new SuccessResponse("Success.", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity editAccountName(String newName, String accountNo) {
        Account account = accountRepository.findByAccountNo(accountNo);
        account.setAccountName(newName);
        try {
            accountRepository.save(account);
            return new ResponseEntity<>(new SuccessResponse("Account name changed", account), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
        }
    }


}
