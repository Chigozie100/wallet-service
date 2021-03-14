package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.Users;
import com.wayapaychat.temporalwallet.enumm.AccountType;
import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
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
    public ResponseEntity createAccount(AccountPojo2 accountPojo) {
        Users user = userRepository.findByUserId(accountPojo.getUserId());
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Accounts account = new ModelMapper().map(accountPojo, Accounts.class);
        account.setUser(user);
        account.setAccountName(user.getUserId()+randomGenerators.generateAlphabet(7));
        account.setAccountType(AccountType.SAVINGS);
        account.setAccountNo(randomGenerators.generateAlphanumeric(10));
        try {
            userRepository.save(user);
            accountRepository.save(account);
            List<Accounts> userAccount = user.getAccounts();
            userAccount.add(account);
            userRepository.save(user);
            return new ResponseEntity<>(new SuccessResponse("Account Created Successfully.", account), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity getUserAccountList(long userId) {
        Users user = userRepository.findByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        List<Accounts> accounts = accountRepository.findByUser(user);
        return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getAllAccount() {
        List<Accounts> accountList = accountRepository.findAll();
        return new ResponseEntity<>(new SuccessResponse("Success.", accountList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getAccountInfo(String accountNo) {
        Accounts account = accountRepository.findByAccountNo(accountNo);
        return new ResponseEntity<>(new SuccessResponse("Success.", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity editAccountName(String newName, String accountNo) {
        Accounts account = accountRepository.findByAccountNo(accountNo);
        account.setAccountName(newName);
        try {
            accountRepository.save(account);
            return new ResponseEntity<>(new SuccessResponse("Account name changed", account), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
        }
    }


}
