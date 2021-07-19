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

import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/wallet")
@Slf4j
public class WalletTransactionController {
	
	@Autowired
	TransAccountService transAccountService;
	
	//@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money")
	@PostMapping("/sendmoney")
	public ResponseEntity<?> sendMoney(@Valid @RequestBody TransferTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.sendMoney(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Wallet Account Statement", notes = "Statement of Account")
	@GetMapping("/statement/{accountNumber}")
	public ResponseEntity<?> getStatement(@PathVariable("accountNumber") String accountNumber) {
		ApiResponse<?> res = transAccountService.getStatement(accountNumber);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Statement of account: {}", accountNumber);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}

}
