package com.wayapaychat.temporalwallet.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.pojo.TransWallet;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.util.PDFExporter;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
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

import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;
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

	@Autowired
	TemporalWalletDAO temporalWalletDAO;

	@Autowired
	TransactionCountService transactionCountService;


	// @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value =
	// "token", paramType = "header", required = true) })
	@ApiOperation(value = "Generate OTP for Payment", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/otp/generate/{emailOrPhoneNumber}")
	public ResponseEntity<?> OtpGenerate(HttpServletRequest request,
			@PathVariable("emailOrPhoneNumber") String emailOrPhoneNumber) {
		return transAccountService.PostOTPGenerate(request, emailOrPhoneNumber);
	}

	// @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value =
	// "token", paramType = "header", required = true) })
	@ApiOperation(value = "Verify Wallet OTP", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/otp/payment/verify")
	public ResponseEntity<?> otpVerify(HttpServletRequest request, @Valid @RequestBody WalletRequestOTP otp) {
		return transAccountService.PostOTPVerify(request, otp);
	}



	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "External Wallet Payment", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/external/payment/{userId}")
	public ResponseEntity<?> ExternalSendMoney(HttpServletRequest request, @Valid @RequestBody CardRequestPojo transfer,
			@PathVariable("userId") Long userId) {
		return transAccountService.PostExternalMoney(request, transfer, userId);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/sendmoney/wallet")
	public ResponseEntity<?> sendMoney(HttpServletRequest request,
			@Valid @RequestBody TransferTransactionDTO transfer) {
		return transAccountService.sendMoney(request, transfer);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/sendmoney/wallet-simulated-users")
	public ResponseEntity<?> sendMoneyForSimulatedUsers(HttpServletRequest request,
									   @Valid @RequestBody List<TransferSimulationDTO> transfer) {
		return transAccountService.sendMoneyToSimulatedUser(request, transfer);
	}

	@ApiOperation(value = "Notify Transaction", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/notify/transaction")
	public ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request,
			@Valid @RequestBody DirectTransactionDTO transfer) {
		return transAccountService.VirtuPaymentMoney(request, transfer);
	}

	@ApiOperation(value = "Notify Transaction Reverse", notes = "Reverse Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/notify/transaction/reverse")
	public ResponseEntity<?> VirtuPaymentReverse(HttpServletRequest request,
			@RequestBody() ReversePaymentDTO reverseDto) {
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To transfer money from one waya official account to another", notes = "Post Money", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/official/transfer")
	public ResponseEntity<?> OfficialSendMoney(HttpServletRequest request,
			@Valid @RequestBody OfficeTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.OfficialMoneyTransfer(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To transfer money from one waya official account to user wallet", notes = "Post Money", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/official/user/transfer")
	public ResponseEntity<?> OfficialUserMoneyEventID(HttpServletRequest request,
											   @Valid @RequestBody OfficeUserTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.OfficialUserTransfer(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
//	@ApiOperation(value = "To transfer money from one waya official account to user wallet", notes = "Post Money", tags = {
//			"TRANSACTION-WALLET" })
//	@PostMapping("/official/user/transfer")
//	public ResponseEntity<?> OfficialUserMoney(HttpServletRequest request,
//			@Valid @RequestBody OfficeUserTransferDTO transfer) {
//		ApiResponse<?> res = transAccountService.OfficialUserTransfer(request, transfer);
//		if (!res.getStatus()) {
//			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
//		}
//		log.info("Send Money: {}", transfer);
//		return new ResponseEntity<>(res, HttpStatus.OK);
//	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To transfer money from one waya official account to multiple user wallets", notes = "Post Money", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/official/user/transfer-multiple")
	public ResponseEntity<?> OfficialUserMoneyMultiple(HttpServletRequest request,
											   @Valid @RequestBody List<OfficeUserTransferDTO> transfer) {
		ApiResponse<?> res = transAccountService.OfficialUserTransfer(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to commercial bank", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/fund/bank/account")
	public ResponseEntity<?> fundBank(HttpServletRequest request, @Valid @RequestBody BankPaymentDTO transfer) {
		System.out.println("transfer : {} " + transfer);
		return transAccountService.BankTransferPayment(request, transfer);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money From Official Account to commercial bank", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/Official/fund/bank/account")
	public ResponseEntity<?> officialFundBank(HttpServletRequest request, @Valid @RequestBody BankPaymentOfficialDTO transfer) {
		System.out.println("transfer : {} " + transfer);
		return transAccountService.BankTransferPaymentOfficial(request, transfer);

	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money to Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/sendmoney")
	public ResponseEntity<?> AdminsendMoney(HttpServletRequest request,
			@Valid @RequestBody AdminLocalTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminsendMoney(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money to Wallet: Multiple Transaction", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/sendmoney-multiple")
	public ResponseEntity<?> AdminSendMoneyMultiple(HttpServletRequest request,
											@Valid @RequestBody List<AdminLocalTransferDTO> transfer) {
		ApiResponse<?> res = transAccountService.AdminSendMoneyMultiple(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}


	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/admin/commission/transfer")
	public ResponseEntity<?> AdminCommissionMoney(HttpServletRequest request,
			@Valid @RequestBody CommissionTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminCommissionMoney(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/client/commission/transfer")
	public ResponseEntity<?> CommissionMoney(HttpServletRequest request,
			@Valid @RequestBody ClientComTransferDTO transfer) {
		ApiResponse<?> res = transAccountService.ClientCommissionMoney(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet with Charge", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/sendmoney/wallet/charge")
	public ResponseEntity<?> PushsendMoney(HttpServletRequest request,
			@Valid @RequestBody WalletTransactionChargeDTO transfer) {
		ApiResponse<?> res = transAccountService.sendMoneyCharge(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/sendmoney/wallet/customer")
	public ResponseEntity<?> sendMoneyCustomer(HttpServletRequest request,
			@Valid @RequestBody WalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.sendMoneyCustomer(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Send Money to Wallet", notes = "Admin Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/admin/sendmoney/customer")
	public ResponseEntity<?> AdminSendMoney(HttpServletRequest request,
			@Valid @RequestBody AdminWalletTransactionDTO transfer) {
		ApiResponse<?> res = transAccountService.AdminSendMoneyCustomer(request, transfer);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		log.info("Send Money: {}", transfer);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Client Send Money to Wallet", notes = "Client Post Money", tags = { "TRANSACTION-WALLET" })
	@PostMapping("/client/sendmoney/customer")
	public ResponseEntity<?> ClientSendMoney(HttpServletRequest request,
			@Valid @RequestBody ClientWalletTransactionDTO transfer) {
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Transfer from one User Wallet to another wallet", notes = "Transfer from one Wallet to another wallet for a user this takes customer wallet id and the Beneficiary wallet id, effective from 06/24/2021", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/fund/transfer/wallet")
	public ResponseEntity<?> handleTransactions(HttpServletRequest request,
			@RequestBody TransferTransactionDTO transactionPojo) {
		return transAccountService.makeWalletTransaction(request, "", transactionPojo);

	}

	// Stopped
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To Fetch Transactions By Account Number", notes = "find transaction by Account Number pagable", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/find/transactions/{accountNo}")
	public ResponseEntity<?> findTransactionAccountNo(@PathVariable("accountNo") String accountNo,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		ApiResponse<?> res = transAccountService.findByAccountNumber(page, size, accountNo);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Fetch Transaction By Wallet Id", notes = "find transaction by Wallet Id pagable", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/get/transactions/{walletId}")
	public ResponseEntity<?> findWalletTransaction(@PathVariable("walletId") Long walletId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		ApiResponse<?> res = transAccountService.getTransactionByWalletId(page, size, walletId);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find All Transaction pagable", notes = "find all transaction pagable", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/find/all/transactions")
	public ResponseEntity<?> findAllTransaction(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		ApiResponse<?> res = transAccountService.findAllTransaction(page, size);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Find Transaction by tranId", notes = "find client transaction", tags = {
			"TRANSACTION-WALLET" })
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/admin/wallet/funding")
	public ResponseEntity<?> AdminTransferForUser(HttpServletRequest request,
			@RequestBody() AdminUserTransferDTO walletDto, @RequestParam("command") String command) {
		return transAccountService.adminTransferForUser(request, command, walletDto);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/admin/wallet/payment")
	public ResponseEntity<?> AdminPaymentService(HttpServletRequest request,
			@RequestBody() WalletAdminTransferDTO walletDto, @RequestParam("command") String command) {
		ApiResponse<?> res = transAccountService.cashTransferByAdmin(request, command, walletDto);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	// Wallet call by other service

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment: Reverse Payment request", notes = "Reverse Payment request", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/event/charge/reverse-payment-request")
	public ResponseEntity<?> EventReversPayment(HttpServletRequest request, @RequestBody() EventPaymentRequestReversal walletDto) {
		return transAccountService.EventReversePaymentRequest(request, walletDto);

	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/event/charge/payment")
	public ResponseEntity<?> EventPayment(HttpServletRequest request, @RequestBody() EventPaymentDTO walletDto) {
		return transAccountService.EventTransferPayment(request, walletDto);

	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment for Merchant Settlement", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/event/charge/payment-merchant-settlement")
	public ResponseEntity<?> EventPaymentSettlement(HttpServletRequest request, @RequestBody() EventPaymentSettlementDTO walletDto) {
		return transAccountService.EventPaymentSettlement(request, walletDto);

	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Office Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/event/office/payment")
	public ResponseEntity<?> EventOfficePayment(HttpServletRequest request, @RequestBody() EventOfficePaymentDTO walletDto) {
		return transAccountService.EventOfficePayment(request, walletDto);

	}
//ability to transfer money from the temporal wallet back to waya official account in single or in mass with excel upload
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Office Event: Temporal - Official Transfer", notes = "Transfer amount from Temporal wallet to Official wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/event/office/temporal-to-official")
	public ResponseEntity<?> TemporalToOfficialWalletDTO(HttpServletRequest request, @RequestBody() TemporalToOfficialWalletDTO walletDto) {
		return transAccountService.TemporalWalletToOfficialWallet(request, walletDto);

	}
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Office Event: Temporal - Official Transfer Multiple", notes = "Transfer amount from Temporal wallet to Official wallet mutiliple transaction", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/event/office/temporal-to-official-multiple")
	public ResponseEntity<?> TemporalToOfficialWalletDTO(HttpServletRequest request, @RequestBody() List<TemporalToOfficialWalletDTO> walletDto) {
		return transAccountService.TemporalWalletToOfficialWalletMutiple(request, walletDto);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Trade and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/event/trade/payment")
	public ResponseEntity<?> BuySellPayment(HttpServletRequest request, @RequestBody() WayaTradeDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventBuySellPayment(request, walletDto);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/non-waya/transaction/payment")
	public ResponseEntity<?> NonWayaPaymentX(HttpServletRequest request, @RequestBody() NonWayaPaymentDTO walletDto) {
		System.out.println("HERE  IS THE ENTRY POINT");
		ApiResponse<?> res = transAccountService.EventNonPayment(request, walletDto);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
//	@ApiOperation(value = "Non-Waya Payment Multiple ", notes = "Transfer amount from user wallet to Non-waya mutiple transaction", tags = {
//			"TRANSACTION-WALLET" })
//	@PostMapping("/non-waya/transaction/payment-multiple")
//	public ResponseEntity<?> NonWayaPaymentXMultiple(HttpServletRequest request, @RequestBody() List<NonWayaPaymentDTO> walletDto) {
//		ApiResponse<?> res = transAccountService.EventNonPaymentMultiple(request, walletDto);
//		if (!res.getStatus()) {
//			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
//		}
//		return new ResponseEntity<>(res, HttpStatus.OK);
//	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/non-waya/transaction/redeem")
	public ResponseEntity<?> NonWayaRedeem(HttpServletRequest request, @RequestBody() NonWayaPaymentDTO walletDto) {
		ApiResponse<?> res = transAccountService.EventNonRedeem(request, walletDto);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem Multiple Tranc", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/non-waya/transaction/redeem-multiple")
	public ResponseEntity<?> NonWayaRedeemMultiple(HttpServletRequest request, @RequestBody() List<NonWayaPaymentDTO> walletDto) {
		ApiResponse<?> res = transAccountService.EventNonRedeemMultiple(request, walletDto);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Transaction Count", notes = "Total Transaction", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-transactions/{userId}")
	public ResponseEntity<?> totalNonePaymentRequest(@PathVariable String userId) {
		return transAccountService.getTotalNoneWayaPaymentRequest(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Pending Count", notes = "Total Pending Count", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-pending/{userId}")
	public ResponseEntity<?> pendingNonePaymentRequest(@PathVariable String userId) {
		return transAccountService.getPendingNoneWayaPaymentRequest(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Expired Count", notes = "Total Expired", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-expired/{userId}")
	public ResponseEntity<?> expiredNonePaymentRequest(@PathVariable String userId) {
		return transAccountService.getExpierdNoneWayaPaymentRequest(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Reserved Count", notes = "Total Reserved", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-reserved/{userId}")
	public ResponseEntity<?> ReservedNonePaymentRequest(@PathVariable String userId) {
		return transAccountService.getReservedNoneWayaPaymentRequest(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Payout Count", notes = "Total Payout", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-payout/{userId}")
	public ResponseEntity<?> PayoutNonePaymentRequest(@PathVariable String userId) {
		return transAccountService.getPayoutNoneWayaPaymentRequest(userId);
	}
	//v


	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Expired Amount", notes = "Total Expired Amount", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-expired-amount/{userId}")
	public ResponseEntity<?> expiredNonePaymentRequestAmount(@PathVariable String userId) {
		return transAccountService.getExpierdNoneWayaPaymentRequestAmount(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Payout Amount", notes = "Total Payout Amount", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-payout-amount/{userId}")
	public ResponseEntity<?> payoutNonePaymentRequestAmount(@PathVariable String userId) {
		return transAccountService.getPayoutNoneWayaPaymentRequestAmount(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Reserved Amount", notes = "Total Reserved Amount", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-reserved-amount/{userId}")
	public ResponseEntity<?> ReservedNonePaymentRequestAmount(@PathVariable String userId) {
		return transAccountService.getReservedNoneWayaPaymentRequestAmount(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Transaction Count", notes = "Total Transaction", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-transactions-amount/{userId}")
	public ResponseEntity<?> totalNonePaymentRequestAmount(@PathVariable String userId) {
		return transAccountService.getTotalNoneWayaPaymentRequestAmount(userId);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non Waya Total Pending Amount", notes = "Total Pending Amount", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/total-pending-amount/{userId}")
	public ResponseEntity<?> pendingNonePaymentRequestAmount(@PathVariable String userId) {
		return transAccountService.getPendingNoneWayaPaymentRequestAmount(userId);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/non-waya/payment/new")
	public ResponseEntity<?> NonWayaPayment(HttpServletRequest request,
			@Valid @RequestBody() NonWayaPaymentDTO walletDto) {
		return transAccountService.TransferNonPayment(request, walletDto);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment for multiple transaction ", notes = "Transfer amount from user wallet to Non-waya for multiple transaction", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/non-waya/payment/new-multiple")
	public ResponseEntity<?> NonWayaPaymentMultiple(HttpServletRequest request,
											@Valid @RequestBody() List<NonWayaPaymentDTO> walletDto) {
		return transAccountService.TransferNonPaymentMultiple(request, walletDto);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin send Non-Waya Payment with excel upload on behalf of users", notes = "Admin send Non-Waya Payment with excel upload on behalf of users", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping(path = "/non-waya/payment/new-multiple-excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> NonWayaPaymentMultipleUpload(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
		return transAccountService.TransferNonPaymentMultipleUpload(request, file);
	}


	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment for Single transaction by waya official", notes = "Transfer amount from user wallet to Non-waya for single transaction by waya  official", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/non-waya/payment/new-single-waya-official")
	public ResponseEntity<?> NonWayaPaymentSingleWayaOfficial(HttpServletRequest request,
																@Valid @RequestBody() NonWayaPaymentMultipleOfficialDTO walletDto) {
		return transAccountService.TransferNonPaymentSingleWayaOfficial(request, walletDto);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment for multiple transaction by waya official", notes = "Transfer amount from user wallet to Non-waya for multiple transaction by waya  official", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/non-waya/payment/new-multiple-waya-official")
	public ResponseEntity<?> NonWayaPaymentMultipleWayaOfficial(HttpServletRequest request,
													@Valid @RequestBody() List<NonWayaPaymentMultipleOfficialDTO> walletDto) {
		return transAccountService.TransferNonPaymentMultipleWayaOfficial(request, walletDto);
	}


	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping(path = "/non-waya/payment/new-multiple-official-excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> TransferNonPaymentWayaOfficialExcel(HttpServletRequest request, @RequestPart("file") MultipartFile file) {

		return new ResponseEntity<>(transAccountService.TransferNonPaymentWayaOfficialExcel(request, file), HttpStatus.OK);
	}

	@ApiOperation(value = "Download Template for Bulk User Creation ", tags = { "ADMIN" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/download/bulk-none-waya-excel")
	public ResponseEntity<Resource> getFile(@RequestParam("isNoneWaya") String isNoneWaya) {
		String filename = "bulk-none-waya-excel.xlsx";
		InputStreamResource file = new InputStreamResource(transAccountService.createExcelSheet(isNoneWaya));
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}


	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/list-transactions/{userId}")
	public ResponseEntity<?> getListOfNonWayaTransfers(HttpServletRequest request,
													   @RequestParam(defaultValue = "0") int page,
													   @RequestParam(defaultValue = "10") int size,
													   @PathVariable String userId) {
		return transAccountService.getListOfNonWayaTransfers(request, userId, page, size);
	}


	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Payment list", notes = "Non-Waya Payment list", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/non-waya/payment/list-transactions")
	public ResponseEntity<?> listOfNonWayaTransfers(HttpServletRequest request,
													   @RequestParam(defaultValue = "0") int page,
													   @RequestParam(defaultValue = "10") int size) {
		return transAccountService.listOfNonWayaTransfers(request, page, size);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PutMapping("/non-waya/transaction/redeem/new")
	public ResponseEntity<?> NonWayaRedeem(HttpServletRequest request,
			@Valid @RequestBody() NonWayaRedeemDTO walletDto) {
		return transAccountService.NonWayaPaymentRedeem(request, walletDto);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PutMapping("/non-waya/transaction/redeem/PIN")
	public ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request,
			@Valid @RequestBody() NonWayaPayPIN walletDto) {
		return transAccountService.NonWayaRedeemPIN(request, walletDto);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "QR Code Payment generation", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/qr-code/transactionpayment")
	public ResponseEntity<?> WayaQRCodeGen(HttpServletRequest request,
			@Valid @RequestBody() WayaPaymentQRCode walletDto) {
		return transAccountService.WayaQRCodePayment(request, walletDto);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "QR Code Payment redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
			"TRANSACTION-WALLET" })
	@PutMapping("/qr-code/transaction/redeem")
	public ResponseEntity<?> WayaQRCodeRedeem(HttpServletRequest request,
			@Valid @RequestBody() WayaRedeemQRCode walletDto) {
		return transAccountService.WayaQRCodePaymentRedeem(request, walletDto);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Payment request", notes = "Transfer amount from user to User in waya", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/payment/request/transaction")
	public ResponseEntity<?> transerPaymentUserToUser(HttpServletRequest request, @Valid @RequestBody WayaPaymentRequest transfer) {

		return transAccountService.WayaPaymentRequestUsertoUser(request, transfer);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/admin/commission/payment")
	public ResponseEntity<?> CommissiomPaymentAdmin(HttpServletRequest request,
			@RequestBody() EventPaymentDTO walletDto) {
		return transAccountService.EventCommissionPayment(request, walletDto);
	}

	@ApiOperation(value = "Commission History", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/admin/commission/history")
	public ResponseEntity<?> CommissiomPaymentList() {
		ApiResponse<?> res = transAccountService.CommissionPaymentHistory();
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	// Wallet call by other service
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transaction Reversal", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/transaction/reverse")
	public ResponseEntity<?> PaymentReversal(HttpServletRequest request,
			@RequestBody() ReverseTransactionDTO reverseDto) throws ParseException {
		return transAccountService.TranReversePayment(request, reverseDto);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin Transaction Reversal for faild transactions", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/transaction/reverse-failed-transaction")
	public ResponseEntity<?> PaymentReversalRevised(HttpServletRequest request,
											 @RequestBody() ReverseTransactionDTO reverseDto) throws ParseException {
		return transAccountService.TranReversePaymentRevised(request, reverseDto);
	}



	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/reverse/report")
	public ResponseEntity<?> PaymentRevReReport(
			@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To Fetch client Reverse", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/transaction/reverse/{accountNo}")
	public ResponseEntity<?> PaymentTransReport(
			@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
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

	@ApiOperation(value = "To Fetch Official Transaction activities", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/official/transaction/{wayaNo}")
	public ResponseEntity<?> PaymentWayaReport(
			@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To List Official Transaction activities", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/official/transaction")
	public ResponseEntity<?> PaymentOffWaya(@RequestParam( defaultValue = "0") int page,
											@RequestParam( defaultValue = "10") int size,
											@RequestParam( defaultValue = "D") String filter) {
		ApiResponse<?> res;
		try {
			res = transAccountService.PaymentOffTrans(page, size, filter);
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping("/transfer/bulk-transaction")
	public ResponseEntity<?> createBulkTrans(HttpServletRequest request,
			@Valid @RequestBody BulkTransactionCreationDTO userList) {
		ApiResponse<?> res = transAccountService.createBulkTransaction(request, userList);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", userList);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
			"TRANSACTION-WALLET" })
	@PostMapping(path = "/transfer/bulk-transaction-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createBulkTransExcel(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
		ApiResponse<?> res = transAccountService.createBulkExcelTrans(request, file);
		if (!res.getStatus()) {
			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
		}
		log.info("Send Money: {}", file);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@ApiOperation(value = "For Admin to view all waya transaction", notes = "To view all transaction for wallet/waya", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/admin/statement/{acctNo}")
	public ResponseEntity<?> StatementReport(
			@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
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

//
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
//	@ApiOperation(value = "For Admin to view all waya transaction", notes = "To view all transaction for wallet/waya", tags = {
//			"TRANSACTION-WALLET" })
//	@GetMapping("/client/statement-format/{acctNo}")
//	public ResponseEntity<?> StatementReportFormat(
//			@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
//			@RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
//			@PathVariable("acctNo") String acctNo) {
//		ApiResponse<?> res;
//		try {
//			res = transAccountService.statementReport2(fromdate, todate, acctNo);
//			if (!res.getStatus()) {
//				return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
//			}
//			return new ResponseEntity<>(res, HttpStatus.OK);
//		} catch (Exception e) {
//			e.printStackTrace();
//			res = new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, e.getMessage(), null);
//			return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
//		}
//
//	}

	@ApiOperation(value = "For Client to view all waya transaction", notes = "To view all transaction for wallet/waya", tags = {
			"TRANSACTION-WALLET" })
	@GetMapping("/client/statement/{acctNo}")
	public ResponseEntity<?> StatementClient(
			@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "To Export Account Transaction ", notes = "Account Statement", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/export/pdf/{accountNo}")
	public ResponseEntity<?> exportToPDF(HttpServletResponse response,
							  @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
							  @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
							  @PathVariable String accountNo) throws IOException, com.lowagie.text.DocumentException {
		response.setContentType("application/pdf");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=receipt_" + currentDateTime + ".pdf";
		response.setHeader(headerKey, headerValue);
		List<TransWallet> res = transAccountService.statementReport2(fromdate, todate, accountNo);

		PDFExporter exporter = new PDFExporter(res,accountNo,fromdate,todate);
		exporter.export(response);
		return new ResponseEntity<>(headerValue, HttpStatus.BAD_REQUEST);

	}


	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Total Credit Transactions Amount", notes = "Total Credit Transactions", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/total-credit")
	public ResponseEntity<?> totalCreditTransaction() {
		return transAccountService.creditTransactionAmount();
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Total Debit Transactions Amount", notes = "Total Debit Transactions", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/total-debit")
	public ResponseEntity<?> totalDebitTransaction() {
		return transAccountService.debitTransactionAmount();
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Total Credit And Debit Transactions Amount", notes = "Total Credit And Debit Transactions", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/total-credit-debit")
	public ResponseEntity<?> totalCreditAndDebitTransaction() {
		return transAccountService.debitAndCreditTransactionAmount();
	}

	@ApiOperation(value = "User Transaction Count ", notes = "User Transaction Count", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/get-user-transaction-count")
	public ResponseEntity<?> userTransactionCount() {
		return transactionCountService.getAllUserCount();
	}

	@ApiOperation(value = "User Transaction Count by User Id ", notes = "User Transaction Count by User Id", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/get-user-transaction-count/{userId}")
	public ResponseEntity<?> getUserCount(@PathVariable String userId) {
		return transactionCountService.getUserCount(userId);
	}


	@ApiOperation(value = "All Offical Transaction Count ", notes = "All Offical Transaction Count ", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/get-official-debit-credit-count")
	public ResponseEntity<?> getUserCount() {
		return transAccountService.debitAndCreditTransactionAmountOfficial();
	}

	@ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/get-official-credit-count")
	public ResponseEntity<?> getCreditTransactionAmountOfficial() {
		return transAccountService.creditTransactionAmountOffical();
	}

	@ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = { "TRANSACTION-WALLET" })
	@GetMapping("/transaction/get-official-debit-transaction-count")
	public ResponseEntity<?> getDebitTransactionAmountOfficial() {
		return transAccountService.debitTransactionAmountOffical();
	}

}
