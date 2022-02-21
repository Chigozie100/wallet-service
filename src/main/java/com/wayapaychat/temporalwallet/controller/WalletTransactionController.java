package com.wayapaychat.temporalwallet.controller;

import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.wayapaychat.temporalwallet.dto.NonWayaPayPIN;
import com.wayapaychat.temporalwallet.dto.NonWayaPaymentDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaRedeemDTO;
import com.wayapaychat.temporalwallet.dto.OfficeTransferDTO;
import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.ReversePaymentDTO;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WayaPaymentQRCode;
import com.wayapaychat.temporalwallet.dto.WayaPaymentRequest;
import com.wayapaychat.temporalwallet.dto.WayaRedeemQRCode;
import com.wayapaychat.temporalwallet.dto.WayaTradeDTO;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "TRANSACTION-WALLET", description = "Transaction Wallet Service API")
@Validated
@Slf4j
public class WalletTransactionController {
	
	@Autowired
	TransAccountService transAccountService;
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "External Wallet Payment", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/external/payment/{userId}")
	public ResponseEntity<?> ExternalSendMoney(HttpServletRequest request, @Valid @RequestBody CardRequestPojo transfer, @PathVariable("userId") Long userId) {
		    return transAccountService.PostExternalMoney(request, transfer, userId);   
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/sendmoney/wallet")
	public ResponseEntity<?> sendMoney(HttpServletRequest request, @Valid @RequestBody TransferTransactionDTO transfer) {
		return transAccountService.sendMoney(request, transfer);
	}
	
	@ApiOperation(value = "Notify Transaction", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/notify/transaction")
	public ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request, @Valid @RequestBody DirectTransactionDTO transfer) {
		return transAccountService.VirtuPaymentMoney(request, transfer);
	}
	
	@ApiOperation(value = "Notify Transaction Reverse", notes = "Reverse Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/notify/transaction/reverse")
	public ResponseEntity<?> VirtuPaymentReverse(HttpServletRequest request, @RequestBody() ReversePaymentDTO reverseDto) {
		ApiResponse<?> res;
		try {
			res = transAccountService.VirtuPaymentReverse(request, reverseDto);
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
	@ApiOperation(value = "To transfer money from one waya official account to another", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/official/transfer")
	public ResponseEntity<?> OfficialSendMoney(HttpServletRequest request, @Valid @RequestBody OfficeTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.OfficialMoneyTransfer(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To transfer money from one waya official account to user wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/official/user/transfer")
	public ResponseEntity<?> OfficialUserMoney(HttpServletRequest request, @Valid @RequestBody OfficeUserTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.OfficialUserTransfer(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to commercial bank", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/fund/bank/account")
	public ResponseEntity<?> fundBank(HttpServletRequest request, @Valid @RequestBody BankPaymentDTO transfer) {
		return transAccountService.BankTransferPayment(request, transfer);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money to Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/sendmoney")
	public ResponseEntity<?> AdminsendMoney(HttpServletRequest request, @Valid @RequestBody AdminLocalTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminsendMoney(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/commission/transfer")
	public ResponseEntity<?> AdminCommissionMoney(HttpServletRequest request, @Valid @RequestBody CommissionTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminCommissionMoney(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/client/commission/transfer")
	public ResponseEntity<?> CommissionMoney(HttpServletRequest request, @Valid @RequestBody ClientComTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.ClientCommissionMoney(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet with Charge", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/sendmoney/wallet/charge")
	public ResponseEntity<?> PushsendMoney(HttpServletRequest request, @Valid @RequestBody WalletTransactionChargeDTO transfer) {
		ApiResponse<?> res = transAccountService.sendMoneyCharge(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/sendmoney/wallet/customer")
	public ResponseEntity<?> sendMoneyCustomer(HttpServletRequest request, @Valid @RequestBody WalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.sendMoneyCustomer(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money to Wallet", notes = "Admin Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/sendmoney/customer")
	public ResponseEntity<?> AdminSendMoney(HttpServletRequest request, @Valid @RequestBody AdminWalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminSendMoneyCustomer(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Client Send Money to Wallet", notes = "Client Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/client/sendmoney/customer")
	public ResponseEntity<?> ClientSendMoney(HttpServletRequest request, @Valid @RequestBody ClientWalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.ClientSendMoneyCustomer(request, transfer);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
		log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Wallet Account Statement", notes = "Statement of Account", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "Transfer from one User Wallet to another wallet", notes = "Transfer from one Wallet to another wallet for a user this takes customer wallet id and the Beneficiary wallet id, effective from 06/24/2021", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/fund/transfer/wallet")
	public ResponseEntity<?> handleTransactions(HttpServletRequest request, @RequestBody TransferTransactionDTO transactionPojo) {
		ApiResponse<?> res = transAccountService.makeWalletTransaction(request,"",transactionPojo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	//Stopped
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To Fetch Transactions By Account Number", notes = "find transaction by Account Number pagable", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "Fetch Transaction By Wallet Id", notes = "find transaction by Wallet Id pagable", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "Find All Transaction pagable", notes = "find all transaction pagable", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "Find Transaction by tranId", notes = "find client transaction", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/account/transactions/{tranId}")
	public ResponseEntity<?> findClientTransaction(@PathVariable("tranId") String tranId) {
		ApiResponse<?> res = transAccountService.findClientTransaction(tranId);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Report Account Transaction Statement", tags = { "TRANSACTION-WALLET" })
    @GetMapping(path = "/official/account/statement/{accountNo}")
    public ResponseEntity<?> GetAccountStatement(@PathVariable String accountNo) {
        ApiResponse<?> res = transAccountService.ReportTransaction(accountNo);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/wallet/funding")
	public ResponseEntity<?> AdminTransferForUser(HttpServletRequest request, @RequestBody() AdminUserTransferDTO walletDto, @RequestParam("command") String command) {
		return transAccountService.adminTransferForUser(request, command, walletDto);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/wallet/payment")
	public ResponseEntity<?> AdminPaymentService(HttpServletRequest request, @RequestBody() WalletAdminTransferDTO walletDto, @RequestParam("command") String command) {
		ApiResponse<?> res = transAccountService.cashTransferByAdmin(request, command, walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/event/charge/payment")
	public ResponseEntity<?> EventPayment(HttpServletRequest request, @RequestBody() EventPaymentDTO walletDto) {
		return transAccountService.EventTransferPayment(request, walletDto);
		
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Trade and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/event/trade/payment")
	public ResponseEntity<?> BuySellPayment(HttpServletRequest request, @RequestBody() WayaTradeDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventBuySellPayment(request, walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/non-waya/transaction/payment")
	public ResponseEntity<?> NonWayaPaymentX(HttpServletRequest request, @RequestBody() NonWayaPaymentDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventNonPayment(request, walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/non-waya/transaction/redeem")
	public ResponseEntity<?> NonWayaRedeem(HttpServletRequest request, @RequestBody() NonWayaPaymentDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventNonRedeem(request, walletDto);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/non-waya/payment/new")
	public ResponseEntity<?> NonWayaPayment(HttpServletRequest request, @Valid @RequestBody() NonWayaPaymentDTO walletDto) {
		return transAccountService.TransferNonPayment(request, walletDto);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = { "TRANSACTION-WALLET" })
	@PutMapping("/non-waya/transaction/redeem/new")
	public ResponseEntity<?> NonWayaRedeem(HttpServletRequest request, @Valid @RequestBody() NonWayaRedeemDTO walletDto) {
		return transAccountService.NonWayaPaymentRedeem(request, walletDto);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = { "TRANSACTION-WALLET" })
	@PutMapping("/non-waya/transaction/redeem/PIN")
	public ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request, @Valid @RequestBody() NonWayaPayPIN walletDto) {
		return transAccountService.NonWayaRedeemPIN(request, walletDto);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "QR Code Payment generation", notes = "Transfer amount from user wallet to Non-waya", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/qr-code/transaction/payment")
	public ResponseEntity<?> WayaQRCodeGen(HttpServletRequest request, @Valid @RequestBody() WayaPaymentQRCode walletDto) {
		return transAccountService.WayaQRCodePayment(request, walletDto);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "QR Code Payment redeem", notes = "Transfer amount from user wallet to Non-waya", tags = { "TRANSACTION-WALLET" })
	@PutMapping("/qr-code/transaction/redeem")
	public ResponseEntity<?> WayaQRCodeRedeem(HttpServletRequest request, @Valid @RequestBody() WayaRedeemQRCode walletDto) {
		return transAccountService.WayaQRCodePaymentRedeem(request, walletDto);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Payment request", notes = "Transfer amount from user to User in waya", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/payment/request/transaction")
	public ResponseEntity<?> transerPaymentUserToUser(@RequestParam("command") String command, HttpServletRequest request, @Valid @RequestBody WayaPaymentRequest transfer){
		return transAccountService.WayaPaymentRequestUsertoUser(request, transfer);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/commission/payment")
	public ResponseEntity<?> CommissiomPaymentAdmin(HttpServletRequest request, @RequestBody() EventPaymentDTO walletDto) {
		return transAccountService.EventCommissionPayment(request, walletDto);	
	}
	
	@ApiOperation(value = "Commission History", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/admin/commission/history")
	public ResponseEntity<?> CommissiomPaymentList() {
		ApiResponse<?> res = transAccountService.CommissionPaymentHistory();
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	//Wallet call by other service
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transaction Reversal", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/transaction/reverse")
	public ResponseEntity<?> PaymentReversal(HttpServletRequest request, @RequestBody() ReverseTransactionDTO reverseDto) throws ParseException {
		return transAccountService.TranReversePayment(request, reverseDto);		
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "To Fetch client Reverse", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
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
	
	
	@ApiOperation(value = "To Fetch Official Transaction activities", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
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
	
	@ApiOperation(value = "To List Official Transaction activities", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/transfer/bulk-transaction")
	public ResponseEntity<?> createBulkTrans(HttpServletRequest request, @Valid @RequestBody BulkTransactionCreationDTO userList) {
		ApiResponse<?> res = transAccountService.createBulkTransaction(request, userList);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", userList);
        return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = { "TRANSACTION-WALLET" })
    @PostMapping(path = "/transfer/bulk-transaction-excel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBulkTransExcel(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
		ApiResponse<?> res = transAccountService.createBulkExcelTrans(request, file);
		if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
		log.info("Send Money: {}", file);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
	
	
	@ApiOperation(value = "For Admin to view all waya transaction", notes = "To view all transaction for wallet/waya", tags = { "TRANSACTION-WALLET" })
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
	
	@ApiOperation(value = "For Client to view all waya transaction", notes = "To view all transaction for wallet/waya", tags = { "TRANSACTION-WALLET" })
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
	@ApiOperation(value = "Admin Transaction Charge Report", notes = "Charge Report", tags = { "TRANSACTION-WALLET" })
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
	
	@ApiOperation(value = "To Filter Transaction Type", notes = "Filter Transaction", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/filter/{accountNo}")
	public ResponseEntity<?> PaymentTransFilter(@PathVariable("accountNo") String accountNo) {
		ApiResponse<?> res;
		try {
			res = transAccountService.PaymentTransFilter(accountNo);
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
