package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.Users;
import com.wayapaychat.temporalwallet.enumm.AccountType;
import com.wayapaychat.temporalwallet.pojo.UserPojo;
import com.wayapaychat.temporalwallet.repository.AccountRepository;
import com.wayapaychat.temporalwallet.repository.UserRepository;
import com.wayapaychat.temporalwallet.service.UserService;
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
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RandomGenerators randomGenerators;

    @Autowired
    AccountRepository accountRepository;



    @Override
    public ResponseEntity createUser(UserPojo userRequest) {
        Users user = new ModelMapper().map(userRequest, Users.class);
        try {
           Users u =  userRepository.save(user);
            // Create Account
            createAccount(u, userRequest.isCorporate());
            return new ResponseEntity<>(new SuccessResponse("Account created successfully.", null), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private void createAccount(Users user, boolean corporate) {
        Accounts account = new Accounts();
        account.setUser(user);
        account.setAccountNo(randomGenerators.generateAlphanumeric(10));
        account.setAccountName("Default Wallet");
        account.setAccountType(AccountType.SAVINGS);
        accountRepository.save(account);


        if (corporate){
            Accounts caccount = new Accounts();
            account.setUser(user);
            caccount.setAccountNo(randomGenerators.generateAlphanumeric(10));
            caccount.setAccountName("Commission Wallet");
            caccount.setAccountType(AccountType.COMMISSION);
            accountRepository.save(caccount);
        }
        userRepository.save(user);
    }
}
