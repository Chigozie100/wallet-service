package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import org.springframework.http.ResponseEntity;

public interface AccountService {

    ResponseEntity createAccount(AccountPojo2 accountPojo);
    ResponseEntity getUserAccountList(long userId);
    ResponseEntity getAllAccount();
    ResponseEntity getAccountInfo(String accountNo);
    ResponseEntity editAccountName(String newName, String accountNo);


}
