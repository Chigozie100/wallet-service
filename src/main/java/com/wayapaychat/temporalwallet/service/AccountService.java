package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AccountService {

    ResponseEntity createAccount(AccountPojo2 accountPojo);
    ResponseEntity getUserAccountList(long userId);
    ResponseEntity getUserCommissionList(long userId);
    ResponseEntity getAllAccount();
    ResponseEntity getDefaultWallet(long userId);
    ResponseEntity makeDefaultWallet(long userId, String accountNo);
    ResponseEntity getAccountInfo(String accountNo);
    ResponseEntity editAccountName(String newName, String accountNo);
    ResponseEntity getCommissionAccountListByArray(List<Integer> ids);


}
