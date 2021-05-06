package com.wayapaychat.temporalwallet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.service.TransactionNewService;

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
	public ResponseEntity<Page<Transactions>> getWalletTransaction(@RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(transactionService.getWalletTransaction(page, size));
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Get wallet transactions By UserId pagable", notes = "Get wallet transaction by userId pageable")
	@GetMapping("/get/wallet/transaction/{userId}")
	public ResponseEntity<Page<Transactions>> getWalletTransactionByUser(@PathVariable("userId") Long userId, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(transactionService.getWalletTransactionByUser(userId, page, size));
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Transfer from one user to another user wallet", notes = "Transfer amount from one user wallet to another user wallet")
	@PostMapping("/transfer/to/user")
	public ResponseEntity<TransactionRequest> transerUserToUser(@RequestParam("command") String command, @RequestBody TransactionRequest request) {
		return ResponseEntity.ok(transactionService.transferUserToUser(command, request));
	}
	
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find All Transaction pagable", notes = "find all transaction pagable")
	@GetMapping("/find/all")
	public ResponseEntity<Page<Transactions>> findAll(@RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(transactionService.findAllTransaction(page, size));
	}

}
