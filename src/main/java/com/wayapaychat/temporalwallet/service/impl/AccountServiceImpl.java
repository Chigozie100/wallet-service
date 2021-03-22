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

import java.util.ArrayList;
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
        Accounts account = new Accounts();
        account.setUser(user);
        account.setAccountName(randomGenerators.generateAlphabet(7));
        account.setAccountType(AccountType.SAVINGS);
        account.setAccountNo(randomGenerators.generateAlphanumeric(10));
        try {
            accountRepository.save(account);
            userRepository.save(user);
            List<Accounts> userAccount = user.getAccounts();
            userAccount.add(account);
            user.setAccounts(userAccount);
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
    public ResponseEntity getUserCommissionList(long userId) {
        Users user = userRepository.findByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Accounts accounts = accountRepository.findByUserAndAccountType(user, AccountType.COMMISSION);
        return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getAllAccount() {
        List<Accounts> accountList = accountRepository.findAll();
        return new ResponseEntity<>(new SuccessResponse("Success.", accountList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getDefaultWallet(long userId) {
        Users user = userRepository.findByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Accounts account = accountRepository.findByIsDefault(true);
        return new ResponseEntity<>(new SuccessResponse("Default Wallet", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity makeDefaultWallet(long userId, String accountNo) {
        Users user = userRepository.findByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Accounts account = accountRepository.findByAccountNo(accountNo);
        if (account ==  null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Account No"), HttpStatus.BAD_REQUEST);
        }
        // Check if account belongs to user
        if (account.getUser() != user){
            return new ResponseEntity<>(new ErrorResponse("Invalid Account Access"), HttpStatus.BAD_REQUEST);
        }
        // Get Default Wallet
        Accounts defAccount = accountRepository.findByIsDefault(true);
        if (defAccount != null){
            defAccount.setDefault(false);
            accountRepository.save(defAccount);
        }
        account.setDefault(true);
        accountRepository.save(account);
        return new ResponseEntity<>(new SuccessResponse("Default wallet set", account), HttpStatus.OK);

    }

    @Override
    public ResponseEntity getAccountInfo(String accountNo) {
        Accounts account = accountRepository.findByAccountNo(accountNo);
        return new ResponseEntity<>(new SuccessResponse("Success.", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity editAccountName(String accountNo, String newName) {
        Accounts account = accountRepository.findByAccountNo(accountNo);
        account.setAccountName(newName);
        try {
            accountRepository.save(account);
            return new ResponseEntity<>(new SuccessResponse("Account name changed", account), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity getCommissionAccountListByArray(List<Integer> ids) {
        List<Accounts> accounts = new ArrayList<>();
        for (int id: ids) {
            Accounts commissionAccount = null;
            Users user = userRepository.findByUserId(id);
            if(user != null){
                commissionAccount = accountRepository.findByUserAndAccountType(user, AccountType.COMMISSION);
            }
            accounts.add(commissionAccount);
        }
        return new ResponseEntity<>(new SuccessResponse("Account name changed", accounts), HttpStatus.OK);
    }


}
