package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.pojo.UserPojo;
import com.wayapaychat.temporalwallet.service.AccountService;
import com.wayapaychat.temporalwallet.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class AccountController {


    @Autowired
    AccountService accountService;

    @ApiOperation(value = "Create a Wallet", hidden = false)
    @PostMapping(path = "/create-wallet")
    public ResponseEntity creteAccount(@RequestBody AccountPojo accountPojo) {
        return accountService.createAccount(accountPojo);
    }

    @ApiOperation(value = "Edit Wallet", hidden = false)
    @GetMapping(path = "/edit-wallet/accountno/{accountNo}/name/{newName}")
    public ResponseEntity editAccount(@PathVariable String accountNo, @PathVariable String newName) {
        return accountService.editAccountName(accountNo, newName);
    }

    @ApiOperation(value = "Get Wallet Info", hidden = false)
    @GetMapping(path = "/info/{accountNo}")
    public ResponseEntity getInfo(@PathVariable String accountNo) {
        return accountService.getAccountInfo(accountNo);
    }

    @ApiOperation(value = "Get Wallet Info", hidden = false)
    @GetMapping(path = "/accounts/{user_id}")
    public ResponseEntity getAccounts(@PathVariable long user_id) {
        return accountService.getUserAccountList(user_id);
    }

    @ApiOperation(value = "Get All Wallets", hidden = false)
    @GetMapping(path = "/all-wallets}")
    public ResponseEntity getAllAccounts() {
        return accountService.getAllAccount();
    }

}
