package com.wayapaychat.temporalwallet.controller;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
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
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to commercial bank", notes = "Post Money")
	@PostMapping("/fund/bank/account")
	public ResponseEntity<?> fundBank(@Valid @RequestBody BankPaymentDTO transfer) {
		ApiResponse<?> res = transAccountService.BankTransferPayment(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money to Wallet", notes = "Post Money")
	@PostMapping("/admin/sendmoney")
	public ResponseEntity<?> AdminsendMoney(@Valid @RequestBody AdminLocalTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminsendMoney(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet with Charge", notes = "Post Money")
	@PostMapping("/sendmoney/wallet/charge")
	public ResponseEntity<?> PushsendMoney(@Valid @RequestBody WalletTransactionChargeDTO transfer) {
		ApiResponse<?> res = transAccountService.sendMoneyCharge(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money")
	@PostMapping("/sendmoney/wallet/customer")
	public ResponseEntity<?> sendMoneyCustomer(@Valid @RequestBody WalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.sendMoneyCustomer(transfer);
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
	@ApiOperation(value = "To Fetch Transactions By Account Number", notes = "find transaction by Account Number pagable")
	@GetMapping("/find/transactions/{accountNo}")
	public ResponseEntity<?> findTransactionAccountNo(@RequestParam("accountNo") String accountNo, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size){
		ApiResponse<?> res = transAccountService.findByAccountNumber(page, size, accountNo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Fetch Transaction By Wallet Id", notes = "find transaction by Wallet Id pagable")
	@GetMapping("/find/transactions/{walletId}")
	public ResponseEntity<?> findWalletTransaction(@RequestParam("walletId") Long walletId, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		ApiResponse<?> res = transAccountService.getTransactionByWalletId(page, size, walletId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find All Transaction pagable", notes = "find all transaction pagable")
	@GetMapping("/find/all/transactions")
	public ResponseEntity<?> findAllTransaction(@RequestParam(defaultValue = "0") int page,
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
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/admin/wallet/payment")
	public ResponseEntity<?> AdminPaymentService(@RequestBody() WalletAdminTransferDTO walletDto, @RequestParam("command") String command) {
		ApiResponse<?> res = transAccountService.cashTransferByAdmin(command, walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/event/charge/payment")
	public ResponseEntity<?> EventPayment(@RequestBody() EventPaymentDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventTransferPayment(walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transaction Reversal", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/transaction/reverse")
	public ResponseEntity<?> PaymentReversal(@RequestBody() ReverseTransactionDTO reverseDto) {
		ApiResponse<?> res;
		try {
			res = transAccountService.TranReversePayment(reverseDto);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet")
	@GetMapping("/reverse/report")
	public ResponseEntity<?> PaymentRevReReport(@RequestParam("fromdate") 
	   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate, 
			@RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate) {
		ApiResponse<?> res;
		try {
			res = transAccountService.TranRevALLReport(fromdate, todate);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}

}
