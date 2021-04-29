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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
        Optional<Users> user = userRepository.findByUserId(accountPojo.getUserId());
        if (!user.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Accounts account = new Accounts();
        account.setUser(user.get());
        account.setAccountName(randomGenerators.generateAlphabet(7));
        account.setAccountType(AccountType.SAVINGS);
        account.setAccountNo(randomGenerators.generateAlphanumeric(10));
        try {
            accountRepository.save(account);
            userRepository.save(user.get());
            List<Accounts> userAccount = user.get().getAccounts();
            userAccount.add(account);
            user.get().setAccounts(userAccount);
            userRepository.save(user.get());
            return new ResponseEntity<>(new SuccessResponse("Account Created Successfully.", account), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity getUserAccountList(long userId) {
    	Optional<Users> user = userRepository.findByUserId(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        List<Accounts> accounts = accountRepository.findByUser(user.get());
        return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getUserCommissionList(long userId) {
    	Optional<Users> user = userRepository.findByUserId(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Accounts accounts = accountRepository.findByUserAndAccountType(user.get(), AccountType.COMMISSION);
        return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getAllAccount() {
    	Pageable paging = PageRequest.of(0, 10);
    	Page<Accounts> pagedResult = accountRepository.findAll(paging);
//        List<Accounts> accountList = accountRepository.findAll();
        return new ResponseEntity<>(new SuccessResponse("Success.", pagedResult), HttpStatus.OK);
    }

    @Override
    public ResponseEntity getDefaultWallet(long userId) {
    	Optional<Users> user = userRepository.findByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Accounts account = accountRepository.findByIsDefaultAndUser(true, user.get());
        return new ResponseEntity<>(new SuccessResponse("Default Wallet", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity makeDefaultWallet(long userId, String accountNo) {
    	Optional<Users> user = userRepository.findByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        Optional<Accounts> account = accountRepository.findByAccountNo(accountNo);
        if (!account.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Account No"), HttpStatus.BAD_REQUEST);
        }
        // Check if account belongs to user
        if (account.get().getUser() != user.get()){
            return new ResponseEntity<>(new ErrorResponse("Invalid Account Access"), HttpStatus.BAD_REQUEST);
        }
        // Get Default Wallet
        Accounts defAccount = accountRepository.findByIsDefaultAndUser(true, user.get());
        if (defAccount != null){
            defAccount.setDefault(false);
            accountRepository.save(defAccount);
        }
        account.get().setDefault(true);
        accountRepository.save(account.get());
        return new ResponseEntity<>(new SuccessResponse("Default wallet set", account.get()), HttpStatus.OK);

    }

    @Override
    public ResponseEntity getAccountInfo(String accountNo) {
    	Optional<Accounts> account = accountRepository.findByAccountNo(accountNo);
        return new ResponseEntity<>(new SuccessResponse("Success.", account.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity editAccountName(String accountNo, String newName) {
    	Optional<Accounts> account = accountRepository.findByAccountNo(accountNo);
        account.get().setAccountName(newName);
        try {
            accountRepository.save(account.get());
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
            Optional<Users> user = userRepository.findByUserId(id);
            if(user.isPresent()){
                commissionAccount = accountRepository.findByUserAndAccountType(user.get(), AccountType.COMMISSION);
            }
            accounts.add(commissionAccount);
        }
        return new ResponseEntity<>(new SuccessResponse("Account name changed", accounts), HttpStatus.OK);
    }


}
