<<<<<<< HEAD
/*package com.wayapaychat.temporalwallet.controller;
=======
package com.wayapaychat.temporalwallet.controller;
>>>>>>> master

import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.pojo.UserPojo;
import com.wayapaychat.temporalwallet.service.AccountService;
import com.wayapaychat.temporalwallet.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallet")
public class AccountController {


    @Autowired
    AccountService accountService;

    @ApiOperation(value = "Create a Wallet")
    @PostMapping(path = "/create-wallet")
    public ResponseEntity creteAccount(@RequestBody AccountPojo2 accountPojo) {
        return accountService.createAccount(accountPojo);
    }

    @ApiOperation(value = "Get List of Commission Accounts by UserId Array")
    @PostMapping(path = "/commission-wallets")
    public ResponseEntity getCommissionAccounts(@RequestBody List<Integer> ids) {
        return accountService.getCommissionAccountListByArray(ids);
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

    @ApiOperation(value = "Get User list of wallets", hidden = false)
    @GetMapping(path = "/accounts/{user_id}")
    public ResponseEntity getAccounts(@PathVariable long user_id) {
        return accountService.getUserAccountList(user_id);
    }


    @ApiOperation(value = "Get User Commission wallets", hidden = false)
    @GetMapping(path = "/commission-accounts/{user_id}")
    public ResponseEntity getCommissionAccounts(@PathVariable long user_id) {
        return accountService.getUserCommissionList(user_id);
    }

    @ApiOperation(value = "Get Default Wallet Commission wallets")
    @GetMapping(path = "/default-account/{user_id}")
    public ResponseEntity getDefaultWallet(@PathVariable long user_id) {
        return accountService.getDefaultWallet(user_id);
    }

    @ApiOperation(value = "Get Default Wallet Commission wallets")
    @GetMapping(path = "/set-default-account/{user_id}/{accountNo}")
    public ResponseEntity setDefaultWallet(@PathVariable long user_id, @PathVariable String accountNo) {
        return accountService.makeDefaultWallet(user_id, accountNo);
    }

    @ApiOperation(value = "Get All Wallets - (Admin COnsumption Only)", hidden = false)
    @GetMapping(path = "/all-wallets")
    public ResponseEntity<?> getAllAccounts() {
        return accountService.getAllAccount();
    }

}
<<<<<<< HEAD
*/
=======
>>>>>>> master
