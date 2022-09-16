package com.wayapaychat.temporalwallet.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import com.wayapaychat.temporalwallet.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.UserAccountService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "USER-ACCOUNT-WALLET", description = "User Account Wallet Service API")
@Validated
@Slf4j
public class WalletUserAccountController {
	
	@Autowired
    UserAccountService userAccountService;
	
	@ApiOperation(value = "Create a User", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.createUser(user);       
        //return userAccountService.createUser(user);
    }
	
	//Wallet call by other service
	@ApiOperation(value = "Create User Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/user/account")
    public ResponseEntity<?> createUserAccount(@Valid @RequestBody WalletUserDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.createUserAccount(user);
    }
	
	@ApiOperation(value = "Modify User Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/user/account/modify")
    public ResponseEntity<?> createUserAccount(@Valid @RequestBody UserAccountDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.modifyUserAccount(user);
        //return userAccountService.modifyUserAccount(user);
    }
	
	@ApiOperation(value = "Account Toggle", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/user/account/toggle")
    public ResponseEntity<?> createAccountToggle(@Valid @RequestBody AccountToggleDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.ToggleAccount(user);
        //return userAccountService.modifyUserAccount(user);
    }
	
	@ApiOperation(value = "Delete,Pause and Block User Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/user/account/access")
    public ResponseEntity<?> postAccountRestriction(@Valid @RequestBody AdminAccountRestrictionDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.UserAccountAccess(user);
    }
	
	@ApiOperation(value = "Delete User Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/user/account/delete")
    public ResponseEntity<?> postAccountUser(@Valid @RequestBody UserAccountDelete user) {
		log.info("Request input: {}",user);
		return userAccountService.AccountAccessDelete(user);
    }
	
	@ApiOperation(value = "Pause Account / Freeze Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/account/pause")
    public ResponseEntity<?> postAccountPause(@Valid @RequestBody AccountFreezeDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.AccountAccessPause(user);
    }


    @ApiOperation(value = " Block / UnBlock", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/account/block")
    public ResponseEntity<?> postAccountBlock(@Valid @RequestBody AccountBlockDTO user) {
        log.info("Request input: {}",user);
        return userAccountService.AccountAccessBlockAndUnblock(user);
    }


    @ApiOperation(value = "Delete Account / Block / UnBlock", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/account/closure")
    public ResponseEntity<?> postAccountClosure(@Valid @RequestBody AccountCloseDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.AccountAccessClosure(user);
    }

    @ApiOperation(value = "Delete Multiple Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/account/closure-multiple")
    public ResponseEntity<?> postAccountClosureMultiple(@Valid @RequestBody List<AccountCloseDTO> user) {
        log.info("Request input: {}",user);
        return userAccountService.AccountAccessClosureMultiple(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Transaction account block / unblock", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/account/lien/transaction")
    public ResponseEntity<?> postAccountLien(@Valid @RequestBody AccountLienDTO user) {
		log.info("Request input: {}",user);
		return userAccountService.AccountAccessLien(user);
    }
	
	 @ApiOperation(value = "Create Admin Cash Wallet - (Admin COnsumption Only)", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
	 @PostMapping(path = "/cash/account")
	 public ResponseEntity<?> createCashAccounts(@Valid @RequestBody WalletCashAccountDTO user) {
		 return userAccountService.createCashAccount(user);
	        //return userAccountService.createCashAccount(user);
	 }

	 @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	 @ApiOperation(value = "Create Event Wallet Account - (Admin COnsumption Only)", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
	 @PostMapping(path = "/event/account")
	 public ResponseEntity<?> createEventAccounts(@Valid @RequestBody WalletEventAccountDTO user) {
		 return userAccountService.createEventAccount(user);
	        //return userAccountService.createEventAccount(user);
	 }
	
	//Wallet call by other service
	@ApiOperation(value = "Create a Wallet", tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/create-wallet")
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountPojo2 accountPojo) {
		return userAccountService.createAccount(accountPojo);
    }
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create a wallet account", tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/official/user/account")
    public ResponseEntity<?> createUserAccount(@Valid @RequestBody AccountPojo2 accountPojo) {
		return userAccountService.createAccount(accountPojo);
    }
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create a waya official account", tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/official/waya/account")
    public ResponseEntity<?> createOfficialAccount(@Valid @RequestBody OfficialAccountDTO account) {
		return userAccountService.createOfficialAccount(account);
    }

    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a waya official account", tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/official/waya/account-multiple")
    public ArrayList<Object> createOfficialAccount(@Valid @RequestBody List<OfficialAccountDTO> account) {
        return userAccountService.createOfficialAccount(account);
    }


    @ApiOperation(value = "Create a Wallet", tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "/account/product")
    public ResponseEntity<?> createProductAccount(@Valid @RequestBody AccountProductDTO accountPojo) {
		return userAccountService.createAccountProduct(accountPojo);
        //return userAccountService.createAccount(accountPojo);
    }
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find wallet by walletId", notes = "Find user wallet by walletId", tags = { "USER-ACCOUNT-WALLET" })
	@GetMapping("/find/customer/{walletId}")
	public ResponseEntity<?> findCustomerById(@PathVariable("walletId") Long walletId) {
		ApiResponse<?> res = userAccountService.findCustWalletById(walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find wallet by walletId", notes = "Find user wallet by walletId", tags = { "USER-ACCOUNT-WALLET" })
	@GetMapping("/find/account/{walletId}")
	public ResponseEntity<?> findAccountById(@PathVariable("walletId") Long walletId) {
		ApiResponse<?> res = userAccountService.findAcctWalletById(walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "Account LookUp", notes = "Find Virtual Account", tags = { "USER-ACCOUNT-WALLET" })
	@PostMapping("/account/lookup/{accountNo}")
	public ResponseEntity<?> AccountLook(@PathVariable("accountNo") String accountNo,
			@Valid @RequestBody SecureDTO key ) {
		return userAccountService.AccountLookUp(accountNo, key);
	}
	
	@ApiOperation(value = "Get List of Commission Accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/commission-wallets")
    public ResponseEntity<?> ListAllCommissionAccounts(@RequestBody List<Integer> ids) {
        return userAccountService.getListCommissionAccount(ids);
    }
	
	@ApiOperation(value = "List all waya official accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/waya/official/account")
    public ResponseEntity<?> ListAllWayaAccount() {
        return userAccountService.getListWayaAccount();
    }
	
	@ApiOperation(value = "Get Wallet Account Info", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/info/{accountNo}")
    public ResponseEntity<?> getAcctInfo(@PathVariable String accountNo) {
        return userAccountService.getAccountInfo(accountNo);
    }

    @ApiOperation(value = "Get Wallet Selected Account Detail", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/account/{accountNo}")
    public ResponseEntity<?> GetAcctDetail(@PathVariable String accountNo) {
        return userAccountService.fetchAccountDetail(accountNo);
    }


    @ApiOperation(value = "Get Virtual Account Detail", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/account/virtual/{accountNo}")
    public ResponseEntity<?> GetVirtualAcctDetail(@PathVariable String accountNo) {
        return userAccountService.fetchVirtualAccountDetail(accountNo);
    }

    @ApiOperation(value = "Get User list of wallets", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/accounts/{user_id}")
    public ResponseEntity<?> getAccounts(@PathVariable long user_id) {
        return userAccountService.getUserAccountList(user_id);
    }

    @ApiOperation(value = "List User wallets", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/admin/user/accounts/{user_id}")
    public ResponseEntity<?> GetListAccount(@PathVariable long user_id) {
        return userAccountService.ListUserAccount(user_id);
    }
    
    @ApiOperation(value = "Get All Wallets - (Admin Consumption Only)", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/all-wallets")
    public ResponseEntity<?> getAllAccounts() {
        return userAccountService.getAllAccount();
    }
    
    @ApiOperation(value = "Get User Wallet Commission Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/commission-accounts/{user_id}")
    public ResponseEntity<?> getCommissionAccounts(@PathVariable long user_id) {
        return userAccountService.getUserCommissionList(user_id);
    }

    @ApiOperation(value = "Get User Wallet Commission Detail", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/commission-account/user/{accountNo}")
    public ResponseEntity<?> setDefaultWallet(@PathVariable String accountNo) {
        return userAccountService.makeDefaultWallet(accountNo);
    }
    
    @ApiOperation(value = "Get User Wallet Transaction Limit", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/user/account/{user_id}")
    public ResponseEntity<?> setDefaultWallet(@PathVariable Long user_id) {
        return userAccountService.UserWalletLimit(user_id);
    }
    
    @ApiOperation(value = "Create Cooperate account, this creates a default account and a commission account", notes = "Create Cooperate account, this creates a default account and a commission account", tags = { "USER-ACCOUNT-WALLET" })
	@PostMapping("/create/cooperate/user")
	public ResponseEntity<?> createCooperateAccount(@RequestBody WalletUserDTO createAccountPojo) {
    	return userAccountService.createUserAccount(createAccountPojo);
	}

    @ApiOperation(value = "Create Nuban account, this creates a default account / commission account / nuban account", notes = "Create Cooperate account, this creates a default account and a commission account", tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping("/create/nuban/user")
    public ResponseEntity<?> createNubanAccount(@RequestBody WalletUserDTO createAccountPojo) {
        return userAccountService.createUserAccount(createAccountPojo);
    }
    
    @ApiOperation(value = "List all Commission Accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/commission-wallets/all")
    public ResponseEntity<?> GetAllCommissionAccounts() {
        return userAccountService.getALLCommissionAccount();
    }
	
	@ApiOperation(value = "Get Wallet Account Info", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/commission/{accountNo}")
    public ResponseEntity<?> getAcctCommission(@PathVariable String accountNo) {
        return userAccountService.getAccountCommission(accountNo);
    }

    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet Account Info By Account Number", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/user-account/{accountNo}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String accountNo) throws Exception {
        return userAccountService.getAccountDetails(accountNo);
    }

	@ApiOperation(value = "Get Wallet Default Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/default/{user_id}")
    public ResponseEntity<?> getAcctDefault(@PathVariable Long user_id) {
        return userAccountService.getAccountDefault(user_id);
    }
	
	@ApiOperation(value = "To Search For Account(s) with Phone or Email or WayaID", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/account/search/{item}")
    public ResponseEntity<?> ListAllAccounts(@PathVariable String item) {
        return userAccountService.searchAccount(item);
    }
	
	@ApiOperation(value = "Generate Account Statement", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/admin/account/statement/{accountNo}")
    public ResponseEntity<?> GenerateAccountStatement(@PathVariable String accountNo) {
        ApiResponse<?> res = userAccountService.fetchTransaction(accountNo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
	
	@ApiOperation(value = "Generate Account Statement by tran Date", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/account/statement/{accountNo}")
    public ResponseEntity<?> FilterAccountStatement(@PathVariable String accountNo,
    		@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate, 
			@RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate) {
        ApiResponse<?> res = userAccountService.fetchFilterTransaction(accountNo,fromdate,todate);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
	
	@ApiOperation(value = "Recent Transaction Details for User Accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/recent/accounts/transaction/{user_id}")
    public ResponseEntity<?> GenerateRecentTransaction(@PathVariable Long user_id) {
        ApiResponse<?> res = userAccountService.fetchRecentTransaction(user_id);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
	
	@ApiOperation(value = "List all wallet accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/wallet/account")
    public ResponseEntity<?> ListAllWalletAccount() {
        return userAccountService.getListWalletAccount();
    }
	
	@ApiOperation(value = "Get simulated Account", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/simulated/{user_id}")
    public ResponseEntity<?> GetAcctSimulated(@PathVariable Long user_id) {
        return userAccountService.getAccountSimulated(user_id);
    }
	
	@ApiOperation(value = "List all simulated accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/simulated/account")
    public ResponseEntity<?> ListAllSimulatedAccount() {
        return userAccountService.getListSimulatedAccount();
    }
	
	@ApiOperation(value = "Create a Simulated User", hidden = false, tags = { "USER-ACCOUNT-WALLET" })
    @PostMapping(path = "simulated/account")
    public ResponseEntity<?> createSIMUser(@Valid @RequestBody AccountPojo2 user) {
		log.info("Request input: {}",user);
		return userAccountService.createAccount(user);
    }
	
	@ApiOperation(value = "Get Wallet User account count", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping(path = "/account/count/{userId}")
    public ResponseEntity<?> TotalUserAccountCount(@PathVariable Long userId) {
        return userAccountService.getUserAccountCount(userId);
    }

    @ApiOperation(value = "Total Active Accounts", notes = "Total Transaction", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping("/account/total-active-amount")
    public ResponseEntity<?> getTotalActiveAccount() {
        return userAccountService.getTotalActiveAccount();
    }

    @ApiOperation(value = "Total Active Accounts", notes = "Total Active Accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping("/account/total-active-count")
    public ResponseEntity<?> countActiveAccount() {
        return userAccountService.countActiveAccount();
    }

    @ApiOperation(value = "Total InActive Accounts", notes = "Total InActive Accounts", tags = { "USER-ACCOUNT-WALLET" })
    @GetMapping("/account/total-inactive-count")
    public ResponseEntity<?> countInActiveAccount() {
        return userAccountService.countInActiveAccount();
    }

}
