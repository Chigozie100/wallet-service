package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import org.springframework.http.ResponseEntity;

public interface AccountService {

    ResponseEntity createAccount(AccountPojo accountPojo);
    ResponseEntity getUserAccountList(long userId);
    ResponseEntity getAccountInfo(String accountNo);
    ResponseEntity editAccountName(String newName, String accountNo);


}
