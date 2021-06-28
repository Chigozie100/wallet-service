package com.wayapaychat.temporalwallet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.pojo.AdminUserTransferDto;
import com.wayapaychat.temporalwallet.pojo.MifosTransactionPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.WalletToWalletDto;
import com.wayapaychat.temporalwallet.service.TransactionNewService;
import com.wayapaychat.temporalwallet.util.ApiResponse;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/transaction/new")
public class TransactionControllerNew {
	
	@Autowired
	private TransactionNewService transactionService;
	
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Get wallet transactions pagable", notes = "Get wallet transaction pageable")
	@GetMapping("/get/wallet/transaction")
	public ResponseEntity<ApiResponse> getWalletTransaction(@RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse res = transactionService.getWalletTransaction(page, size);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Get wallet transactions By UserId pagable", notes = "Get wallet transaction by userId pageable")
	@GetMapping("/get/wallet/transaction/{userId}")
	public ResponseEntity<ApiResponse> getWalletTransactionByUser(@PathVariable("userId") Long userId, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse res = transactionService.getWalletTransactionByUser(userId, page, size);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Transfer from one user to another user wallet", notes = "Transfer amount from one user wallet to another user wallet")
	@PostMapping("/transfer/to/user")
	public ResponseEntity<ApiResponse> transerUserToUser(@RequestParam("command") String command, @RequestBody TransactionRequest request) {
		ApiResponse res = transactionService.transferUserToUser(command, request);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Transfer from one Wallet to another wallet", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/wallet/to/wallet")
	public ResponseEntity<ApiResponse> walletToWalletTransfer(@RequestBody() WalletToWalletDto walletDto, @RequestParam("command") String command) {
		ApiResponse res = transactionService.walletToWalletTransfer(walletDto, command);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Transfer from one Wallet to another wallet for a user ", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/admin/for/user/wallet")
	public ResponseEntity<ApiResponse> AdminTransferForUser(@RequestBody() AdminUserTransferDto walletDto, @RequestParam("command") String command) {
		ApiResponse res = transactionService.adminTransferForUser(command, walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find All Transaction pagable", notes = "find all transaction pagable")
	@GetMapping("/find/all")
	public ResponseEntity<ApiResponse> findAll(@RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse res = transactionService.findAllTransaction(page, size);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find Transaction By Wallet Id pagable", notes = "find transaction by Wallet Id pagable")
	@GetMapping("/find/by/wallet/id")
	public ResponseEntity<ApiResponse> findWalletId(@RequestParam("walletId") Long walletId, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse res = transactionService.getTransactionByWalletId(page, size, walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find Transaction By Transaction Type pagable", notes = "find transaction by Transaction Type pagable")
	@GetMapping("/find/by/transaction/type")
	public ResponseEntity<ApiResponse> findByTransactionType(@RequestParam("transactionType") String transactionType, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse res = transactionService.getTransactionByType(page, size, transactionType);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find Transaction By Account Number pagable", notes = "find transaction by Account Number pagable")
	@GetMapping("/find/by/account/number")
	public ResponseEntity<ApiResponse> findByAccountNumber(@RequestParam("accountNumber") String accountNumber, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size){
		ApiResponse res = transactionService.findByAccountNumber(page, size, accountNumber);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Transfer from one User Wallet to another User wallet, this takes customer wallet id and the Beneficiary wallet id, effective from 06/24/2021 ", notes = "Transfer from one Wallet to another wallet for a user this takes customer wallet id and the Beneficiary wallet id, effective from 06/24/2021")
	@PostMapping("/transfer/fund/to/wallet")
	public ResponseEntity<ApiResponse> handleTransactions(@RequestBody MifosTransactionPojo transactionPojo, @RequestParam("command") String command) {
		ApiResponse res = transactionService.makeWalletTransaction(transactionPojo, command);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
}
