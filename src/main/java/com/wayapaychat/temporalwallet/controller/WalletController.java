package com.wayapaychat.temporalwallet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.wayapaychat.temporalwallet.pojo.CreateAccountPojo;
import com.wayapaychat.temporalwallet.pojo.CreateAccountResponse;
import com.wayapaychat.temporalwallet.pojo.CreateWalletResponse;
import com.wayapaychat.temporalwallet.pojo.MainWalletResponse;
import com.wayapaychat.temporalwallet.pojo.ResponsePojo;
import com.wayapaychat.temporalwallet.service.WalletImplementation;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/wallet")
public class WalletController {

	@Autowired
	private WalletImplementation walletImplementation;
	
	
	
	
	@ApiOperation(value = "Create User account/wallet", notes = "Create user account/wallet")
	@PostMapping("/create/account")
	public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountPojo createWallet) {
		return ResponseEntity.ok(walletImplementation.createAccount(createWallet));
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create User account/wallet", notes = "Create user account/wallet")
	@PostMapping("/create/wallet")
	public ResponseEntity<CreateWalletResponse> createWallet(@RequestParam("productId") Integer productId) {
		return ResponseEntity.ok(walletImplementation.createWallet(productId));
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find user wallet by userId/externalId", notes = "Find user wallet by userId/externalId")
	@GetMapping("/find/by/userId/{userId}")
	public ResponseEntity<List<MainWalletResponse>> findWalletByExternalId(@PathVariable("userId") Long externalId) {
		return ResponseEntity.ok(walletImplementation.findWalletByExternalId(externalId));
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find user Commission list", notes = "Find user commission list")
	@GetMapping("/get/commision/list/{userId}")
	public ResponseEntity<MainWalletResponse> getUserCommissionList(@PathVariable("userId") Long externalId) {
		return ResponseEntity.ok(walletImplementation.getUserCommissionList(externalId));
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "get account info using account number", notes = "Get account info using account number")
	@GetMapping("/get/account/info/{accountNum}")
	public ResponseEntity<MainWalletResponse> getAccountInfo(@PathVariable("accountNum") String accountNum){
		return ResponseEntity.ok(walletImplementation.getAccountInfo(accountNum));
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Edit account name", notes = "Edit account name")
	@PutMapping("/edit/account/name")
	public ResponseEntity<Accounts> editAccountName(@RequestParam("accountName") String accountName, @RequestParam("accountNum") String accountNum) {
		return ResponseEntity.ok(walletImplementation.editAccountName(accountName, accountNum));
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Get logged in user default wallet", notes = "Get logged in user defalult wallet")
	@GetMapping("/get/default/wallet")
	public ResponseEntity<MainWalletResponse> getDefaultWallet() {
		return ResponseEntity.ok(walletImplementation.getDefaultWallet());
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find all wallets", notes = "Find all wallets")
	@GetMapping("/find/all")
	public ResponseEntity<List<MainWalletResponse>> findAll() {
		return ResponseEntity.ok(walletImplementation.findAll());
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create waya Account, this is for users with admin role", notes = "Create waya Account, this is for users with admin role")
	@PostMapping("/create/waya/account")
//	@PreAuthorize("Admin")
	public ResponseEntity<ResponsePojo> createWayaAccount() {
		return ResponseEntity.ok(walletImplementation.createWayaWallet());
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "User can user this endpoint to set default wallet using the wallet id", notes = "User can user this endpoint to set default wallet using the wallet id")
	@PostMapping("/set/default/wallet")
	public ResponseEntity<ResponsePojo> setDefaultWallet(@RequestParam("walletId") Long walletId) {
		return ResponseEntity.ok(walletImplementation.makeDefaultWallet(walletId));
	}
	
	
//	public ResponseEntity<List<MainWalletResponse>> getCommissionAccountListByIdList(List<Long> ids) {
//		return ResponseEntity.ok(walletImplementation.getCommissionAccountListByArray(ids));
//	}
}
