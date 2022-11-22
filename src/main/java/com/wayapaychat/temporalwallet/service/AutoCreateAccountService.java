package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AutoCreateAccountService   {

    private final UserAccountService userAccountService;

    @Autowired
    public AutoCreateAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public ResponseEntity<?> createEventAccount(WalletEventAccountDTO user) {
        try{
            return userAccountService.createEventAccount(user);
        }catch (CustomException ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }

    }

}
