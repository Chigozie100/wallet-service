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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/wallet")
@Slf4j
public class WalletTransactionController {
	
	@Autowired
	TransAccountService transAccountService;
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money")
	@PostMapping("/sendmoney/wallet")
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
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Transfer from one User Wallet to another wallet", notes = "Transfer from one Wallet to another wallet for a user this takes customer wallet id and the Beneficiary wallet id, effective from 06/24/2021")
	@PostMapping("/fund/transfer/wallet")
	public ResponseEntity<?> handleTransactions(@RequestBody TransferTransactionDTO transactionPojo) {
		ApiResponse<?> res = transAccountService.makeWalletTransaction("",transactionPojo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find Transaction By Account Number pagable", notes = "find transaction by Account Number pagable")
	@GetMapping("/find/by/account/number")
	public ResponseEntity<?> findByAccountNumber(@RequestParam("accountNumber") String accountNumber, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size){
		ApiResponse<?> res = transAccountService.findByAccountNumber(page, size, accountNumber);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find Transaction By Wallet Id pagable", notes = "find transaction by Wallet Id pagable")
	@GetMapping("/find/by/wallet/id")
	public ResponseEntity<?> findWalletId(@RequestParam("walletId") Long walletId, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse<?> res = transAccountService.getTransactionByWalletId(page, size, walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find All Transaction pagable", notes = "find all transaction pagable")
	@GetMapping("/find/all")
	public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse<?> res = transAccountService.findAllTransaction(page, size);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/admin/wallet/funding")
	public ResponseEntity<?> AdminTransferForUser(@RequestBody() AdminUserTransferDTO walletDto, @RequestParam("command") String command) {
		ApiResponse<?> res = transAccountService.adminTransferForUser(command, walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}

}
