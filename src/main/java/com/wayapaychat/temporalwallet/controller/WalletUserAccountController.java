package com.wayapaychat.temporalwallet.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.dto.UserDTO;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.UserAccountService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/wallet")
@Slf4j
public class WalletUserAccountController {
	
	@Autowired
    UserAccountService userAccountService;
	
	@ApiOperation(value = "Create a User", hidden = false)
    @PostMapping(path = "/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO user) {
		log.info("Request input: {}",user);
        return userAccountService.createUser(user);
    }
	
	@ApiOperation(value = "Create User Account", hidden = false)
    @PostMapping(path = "/user/account")
    public ResponseEntity<?> createUserAccount(@Valid @RequestBody WalletUserDTO user) {
		log.info("Request input: {}",user);
        return userAccountService.createUserAccount(user);
    }
	
	@ApiOperation(value = "Create a Wallet")
    @PostMapping(path = "/create-wallet")
    public ResponseEntity<?> creteAccount(@Valid @RequestBody AccountPojo2 accountPojo) {
        return userAccountService.createAccount(accountPojo);
    }
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find wallet by walletId", notes = "Find user wallet by walletId")
	@GetMapping("/find/customer/{walletId}")
	public ResponseEntity<?> findCustomerById(@PathVariable("walletId") Long walletId) {
		ApiResponse<?> res = userAccountService.findCustWalletById(walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find wallet by walletId", notes = "Find user wallet by walletId")
	@GetMapping("/find/account/{walletId}")
	public ResponseEntity<?> findAccountById(@PathVariable("walletId") Long walletId) {
		ApiResponse<?> res = userAccountService.findAcctWalletById(walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}

}
