package com.wayapaychat.temporalwallet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.SwitchWallet;
import com.wayapaychat.temporalwallet.pojo.CreateAccountPojo;
import com.wayapaychat.temporalwallet.pojo.CreateAccountResponse;
import com.wayapaychat.temporalwallet.pojo.CreateWalletResponse;
import com.wayapaychat.temporalwallet.pojo.MainWalletResponse;
import com.wayapaychat.temporalwallet.pojo.ResponsePojo;
import com.wayapaychat.temporalwallet.proxy.MifosWalletProxy;
import com.wayapaychat.temporalwallet.repository.SwitchWalletRepository;
import com.wayapaychat.temporalwallet.service.WalletImplementation;
import com.wayapaychat.temporalwallet.util.ApiResponse;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/wallet")
public class WalletController {

	@Autowired
	private WalletImplementation walletImplementation;
	
	@Autowired
	private SwitchWalletRepository switchWalletRepo;
	
	@Autowired
	private MifosWalletProxy mifosWalletProxy;
	
	
	
	@ApiOperation(value = "Create User account/wallet", notes = "Create user account/wallet")
	@PostMapping("/create/account")
	public ResponseEntity<ApiResponse> createAccount(@RequestBody CreateAccountPojo createWallet) {
		
		List<SwitchWallet> switchList = switchWalletRepo.findAll();
		
		if(switchList.size() == 0) {
			ApiResponse res = walletImplementation.createAccount(createWallet);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		}
		String mm = "";
		return new ResponseEntity<ApiResponse>(HttpStatus.NOT_FOUND);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create User account/wallet", notes = "Create user account/wallet")
	@PostMapping("/create/wallet")
	public ResponseEntity<ApiResponse> createWallet(@RequestParam("productId") Integer productId) {
		ApiResponse res = walletImplementation.createWallet(productId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find user wallet by userId/externalId", notes = "Find user wallet by userId/externalId")
	@GetMapping("/find/by/userId/{userId}")
	public ResponseEntity<ApiResponse> findWalletByExternalId(@PathVariable("userId") Long externalId) {
		ApiResponse res = walletImplementation.findWalletByExternalId(externalId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find user Commission list", notes = "Find user commission list")
	@GetMapping("/get/commision/list/{userId}")
	public ResponseEntity<ApiResponse> getUserCommissionList(@PathVariable("userId") Long externalId) {
		ApiResponse res = walletImplementation.getUserCommissionList(externalId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "get account info using account number", notes = "Get account info using account number")
	@GetMapping("/get/account/info/{accountNum}")
	public ResponseEntity<ApiResponse> getAccountInfo(@PathVariable("accountNum") String accountNum){
		ApiResponse res = walletImplementation.getAccountInfo(accountNum);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Edit account name", notes = "Edit account name")
	@PutMapping("/edit/account/name")
	public ResponseEntity<ApiResponse> editAccountName(@RequestParam("accountName") String accountName, @RequestParam("accountNum") String accountNum) {
		ApiResponse res = walletImplementation.editAccountName(accountName, accountNum);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Get logged in user default wallet", notes = "Get logged in user defalult wallet")
	@GetMapping("/get/default/wallet")
	public ResponseEntity<ApiResponse> getDefaultWallet() {
		ApiResponse res = walletImplementation.getDefaultWallet();
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "Get logged in user default wallet", notes = "Get logged in user defalult wallet")
	@GetMapping("/get/default/wallet/open/{userId}")
	public ResponseEntity<ApiResponse> getDefaultWalletOpen(@PathVariable("userId") Long userId) {
		ApiResponse res = walletImplementation.getDefaultWalletOpen(userId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find all wallets", notes = "Find all wallets")
	@GetMapping("/find/all")
	public ResponseEntity<ApiResponse> findAll() {
		ApiResponse res = walletImplementation.findAll();
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create waya Account, this is for users with admin role", notes = "Create waya Account, this is for users with admin role")
	@PostMapping("/create/waya/account")
//	@PreAuthorize("Admin")
	public ResponseEntity<ApiResponse> createWayaAccount() {
		ApiResponse res = walletImplementation.createWayaWallet();
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "User can user this endpoint to set default wallet using the wallet id", notes = "User can user this endpoint to set default wallet using the wallet id")
	@PostMapping("/set/default/wallet")
	public ResponseEntity<ApiResponse> setDefaultWallet(@RequestParam("walletId") Long walletId) {
		ApiResponse res = walletImplementation.makeDefaultWallet(walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Get commission account list by user id list", notes = "Get commission account list by user id list")
	@PostMapping("/get/commission/list")
	public ResponseEntity<ApiResponse> getCommissionAccountListByIdList(@RequestBody List<Long> ids) {
		ApiResponse res = walletImplementation.getCommissionAccountListByArray(ids);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find all user wallet", notes = "Fetch all user wallet")
	@GetMapping("/all/user/wallet")
	public ResponseEntity<ApiResponse> findAllUserWallet() {
		ApiResponse res = walletImplementation.allUserWallet();
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Create Cooperate account, this creates a default account and a commission account", notes = "Create Cooperate account, this creates a default account and a commission account")
	@PostMapping("/create/cooperate/user")
	public ResponseEntity<ApiResponse> createCooperateAccount(@RequestBody CreateAccountPojo createAccountPojo) {
		ApiResponse res = walletImplementation.createCooperateUserAccount(createAccountPojo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Get waya commission wallet", notes = "Get Waya commission wallet")
	@GetMapping("/get/waya/commission/wallet")
	public ResponseEntity<ApiResponse> getWayaCommissionWallet() {
		ApiResponse res = walletImplementation.getWayaCommissionWallet();
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
}
