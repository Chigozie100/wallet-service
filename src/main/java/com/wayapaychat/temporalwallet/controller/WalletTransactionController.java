package com.wayapaychat.temporalwallet.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminWalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.dto.BulkTransactionCreationDTO;
import com.wayapaychat.temporalwallet.dto.ClientComTransferDTO;
import com.wayapaychat.temporalwallet.dto.ClientWalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.CommissionTransferDTO;
import com.wayapaychat.temporalwallet.dto.DirectTransactionDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaPaymentDTO;
import com.wayapaychat.temporalwallet.dto.OfficeTransferDTO;
import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.ReversePaymentDTO;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WayaTradeDTO;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;

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
	@ApiOperation(value = "External Wallet Payment", notes = "Post Money")
	@PostMapping("/external/payment/{userId}")
	public ResponseEntity<?> ExternalSendMoney(HttpServletRequest request, @Valid @RequestBody CardRequestPojo transfer, @PathVariable("userId") Long userId) {
		return transAccountService.PostExternalMoney(request, transfer, userId);
        
	}
	
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
	
	@ApiOperation(value = "Notify Transaction", notes = "Post Money")
	@PostMapping("/notify/transaction")
	public ResponseEntity<?> VirtuPaymentMoney(@Valid @RequestBody DirectTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.VirtuPaymentMoney(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Notify Transaction Reverse", notes = "Reverse Post Money")
	@PostMapping("/notify/transaction/reverse")
	public ResponseEntity<?> VirtuPaymentReverse(@RequestBody() ReversePaymentDTO reverseDto) {
		ApiResponse<?> res;
		try {
			res = transAccountService.VirtuPaymentReverse(reverseDto);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To transfer money from one waya official account to another", notes = "Post Money")
	@PostMapping("/official/transfer")
	public ResponseEntity<?> OfficialSendMoney(@Valid @RequestBody OfficeTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.OfficialMoneyTransfer(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To transfer money from one waya official account to user wallet", notes = "Post Money")
	@PostMapping("/official/user/transfer")
	public ResponseEntity<?> OfficialUserMoney(@Valid @RequestBody OfficeUserTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.OfficialUserTransfer(transfer);
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
	@ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money")
	@PostMapping("/admin/commission/transfer")
	public ResponseEntity<?> AdminCommissionMoney(@Valid @RequestBody CommissionTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminCommissionMoney(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money")
	@PostMapping("/client/commission/transfer")
	public ResponseEntity<?> CommissionMoney(@Valid @RequestBody ClientComTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.ClientCommissionMoney(transfer);
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
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money to Wallet", notes = "Admin Post Money")
	@PostMapping("/admin/sendmoney/customer")
	public ResponseEntity<?> AdminSendMoney(@Valid @RequestBody AdminWalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminSendMoneyCustomer(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Client Send Money to Wallet", notes = "Client Post Money")
	@PostMapping("/client/sendmoney/customer")
	public ResponseEntity<?> ClientSendMoney(@Valid @RequestBody ClientWalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.ClientSendMoneyCustomer(transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Wallet Account Statement", notes = "Statement of Account")
	@GetMapping("/statement/{accountNo}")
	public ResponseEntity<?> getStatement(@PathVariable("accountNo") String accountNo) {
		ApiResponse<?> res = transAccountService.getStatement(accountNo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Statement of account: {}", accountNo);
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
	public ResponseEntity<?> findTransactionAccountNo(@PathVariable("accountNo") String accountNo, @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size){
		ApiResponse<?> res = transAccountService.findByAccountNumber(page, size, accountNo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Fetch Transaction By Wallet Id", notes = "find transaction by Wallet Id pagable")
	@GetMapping("/get/transactions/{walletId}")
	public ResponseEntity<?> findWalletTransaction(@PathVariable("walletId") Long walletId, @RequestParam(defaultValue = "0") int page,
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
	@ApiOperation(value = "Find Transaction by tranId", notes = "find client transaction")
	@GetMapping("/account/transactions/{tranId}")
	public ResponseEntity<?> findClientTransaction(@PathVariable("tranId") String tranId) {
		ApiResponse<?> res = transAccountService.findClientTransaction(tranId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Report Account Transaction Statement")
    @GetMapping(path = "/official/account/statement/{accountNo}")
    public ResponseEntity<?> GetAccountStatement(@PathVariable String accountNo) {
        ApiResponse<?> res = transAccountService.ReportTransaction(accountNo);
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
	@ApiOperation(value = "Trade and Service Payment", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/event/trade/payment")
	public ResponseEntity<?> BuySellPayment(@RequestBody() WayaTradeDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventBuySellPayment(walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya")
	@PostMapping("/non-waya/transaction/payment")
	public ResponseEntity<?> NonWayaPayment(@RequestBody() NonWayaPaymentDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventNonPayment(walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya")
	@PostMapping("/non-waya/transaction/redeem")
	public ResponseEntity<?> NonWayaRedeem(@RequestBody() NonWayaPaymentDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventNonRedeem(walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/admin/commission/payment")
	public ResponseEntity<?> CommissiomPaymentAdmin(@RequestBody() EventPaymentDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventCommissionPayment(walletDto);
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
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To Fetch client Reverse", notes = "Transfer amount from one wallet to another wallet")
	@GetMapping("/transaction/reverse/{accountNo}")
	public ResponseEntity<?> PaymentTransReport(@RequestParam("fromdate") 
	   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate, 
			@RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
			@PathVariable("accountNo") String accountNo) {
		ApiResponse<?> res;
		try {
			res = transAccountService.PaymentTransAccountReport(fromdate, todate, accountNo);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	
	@ApiOperation(value = "To Fetch Official Transaction activities", notes = "Transfer amount from one wallet to another wallet")
	@GetMapping("/official/transaction/{wayaNo}")
	public ResponseEntity<?> PaymentWayaReport(@RequestParam("fromdate") 
	   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate, 
			@RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
			@PathVariable("wayaNo") String wayaNo) {
		ApiResponse<?> res;
		try {
			res = transAccountService.PaymentAccountTrans(fromdate, todate, wayaNo);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiOperation(value = "To List Official Transaction activities", notes = "Transfer amount from one wallet to another wallet")
	@GetMapping("/official/transaction")
	public ResponseEntity<?> PaymentOffWaya() {
		ApiResponse<?> res;
		try {
			res = transAccountService.PaymentOffTrans();
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet")
	@GetMapping("/all/reverse/report")
	public ResponseEntity<?> PaymentAllReverse() {
		ApiResponse<?> res;
		try {
			res = transAccountService.TranALLReverseReport();
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet")
	@PostMapping("/transfer/bulk-transaction")
	public ResponseEntity<?> createBulkTrans(@Valid @RequestBody BulkTransactionCreationDTO userList) {
		ApiResponse<?> res = transAccountService.createBulkTransaction(userList);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", userList);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet")
    @PostMapping(path = "/transfer/bulk-transaction-excel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBulkTransExcel(@RequestPart("file") MultipartFile file) {
		ApiResponse<?> res = transAccountService.createBulkExcelTrans(file);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", file);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
	
	
	@ApiOperation(value = "For Admin to view all waya transaction", notes = "To view all transaction for wallet/waya")
	@GetMapping("/admin/statement/{acctNo}")
	public ResponseEntity<?> StatementReport(@RequestParam("fromdate") 
	   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate, 
			@RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
			@PathVariable("acctNo") String acctNo) {
		ApiResponse<?> res;
		try {
			res = transAccountService.statementReport(fromdate, todate, acctNo);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiOperation(value = "For Client to view all waya transaction", notes = "To view all transaction for wallet/waya")
	@GetMapping("/client/statement/{acctNo}")
	public ResponseEntity<?> StatementClient(@RequestParam("fromdate") 
	   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate, 
			@RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
			@PathVariable("acctNo") String acctNo) {
		ApiResponse<?> res;
		try {
			res = transAccountService.statementReport(fromdate, todate, acctNo);
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transaction Charge Report", notes = "Charge Report")
	@GetMapping("/transaction/charge/report")
	public ResponseEntity<?> PaymentChargeReport() {
		ApiResponse<?> res;
		try {
			res = transAccountService.TranChargeReport();
			if (!res.getStatus()) {
	            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	        }
	        return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		
	}

}
