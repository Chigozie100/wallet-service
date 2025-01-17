package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.enumm.EventCharge;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.response.TransactionsResponse;
import com.wayapaychat.temporalwallet.service.CoreBankingService;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.ExportPdf;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "TRANSACTION-WALLET", description = "Transaction Wallet Service API")
@Validated
@Slf4j
public class WalletTransactionController {

    private final TransAccountService transAccountService;
    private final TransactionCountService transactionCountService;
    private final CoreBankingService coreBankingService;

    @Autowired
    public WalletTransactionController(TransAccountService transAccountService, TransactionCountService transactionCountService, CoreBankingService coreBankingService) {
        this.transAccountService = transAccountService;
        this.transactionCountService = transactionCountService;
        this.coreBankingService = coreBankingService;
    }

    // @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value =
    // "token", paramType = "header", required = true) })
    @ApiOperation(value = "Generate OTP for Payment", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/otp/generate/{emailOrPhoneNumber}")
    public ResponseEntity<?> OtpGenerate(HttpServletRequest request,
            @PathVariable("emailOrPhoneNumber") String emailOrPhoneNumber,
                                         @RequestParam(name = "businessId",required = false)String businessId) {
        log.info("Endpoint to generate OTP called !!! --->> {}", emailOrPhoneNumber);
        return transAccountService.PostOTPGenerate(request, emailOrPhoneNumber,businessId);
    }

    // @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value =
    // "token", paramType = "header", required = true) })
    @ApiOperation(value = "Verify Wallet OTP", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/otp/payment/verify")
    public ResponseEntity<?> otpVerify(HttpServletRequest request, @Valid @RequestBody WalletRequestOTP otp) {
        log.info("Endpoint to verify OTP called !!! --->> {}", otp);
        return transAccountService.PostOTPVerify(request, otp);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "External Wallet Payment", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/external/payment/{userId}")
    public ResponseEntity<?> ExternalSendMoney(HttpServletRequest request, @Valid @RequestBody CardRequestPojo transfer,
            @PathVariable("userId") Long userId) {
        log.info("Endpoint External Send Money called !!! --->> {}", userId);
        return transAccountService.PostExternalMoney(request, transfer, userId);
    }

    // Wallet call by other serviceƒ
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/sendmoney/wallet")
    public ResponseEntity<?> sendMoney(HttpServletRequest request, @Valid @RequestBody TransferTransactionDTO transfer) {
        log.info("Endpoint  Send Money called !!! --->> {}", transfer);

        try {
            return coreBankingService.processTransaction(transfer, "WAYATRAN", request);
        } catch (CustomException ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send Money to Account to Account", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/sendmoney/account")
    public ResponseEntity<?> sendMoneyCBA(HttpServletRequest request,
            @Valid @RequestBody TransferTransactionDTO transfer) {
        log.info("Endpoint  Send Money CBA called !!! --->> {}", transfer);
        try {
            return coreBankingService.processTransaction(transfer, "WAYATRAN", request);
        } catch (CustomException ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send Money to Contact: Email or Phone or ID ", notes = "Send Money to Contact: Email or Phone of ID", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/sendmoney/to-contact")
    public ResponseEntity<?> sendMoneyToEmailOrPhone(HttpServletRequest request, @Valid @RequestBody SendMoneyToEmailOrPhone transfer) {
        log.info("Endpoint  Send Money to email or phone called !!! --->> {}", transfer);

        try {

            // get user by email or phone
            String beneficiaryAcctNum;
            if(transfer.isAccountNumber()){
                beneficiaryAcctNum = transfer.getBeneficiaryAccountNumber();
            }else {
                WalletAccount account = transAccountService.findUserAccount(Long.valueOf(transfer.getBeneficiaryUserId()),transfer.getBeneficiaryProfileId());
                if(account == null)
                    return new ResponseEntity<>(new ErrorResponse("BENEFICIARY ACCOUNT NOT FOUND, CAN NOT PROCESS TRANSFER"), HttpStatus.NOT_FOUND);

                beneficiaryAcctNum = account.getAccountNo();
            }
//            WalletAccount account = transAccountService.findByEmailOrPhoneNumberOrId(
//                    transfer.getIsAccountNumber(),
//                    transfer.getEmailOrPhone(),
//                    transfer.getSenderUserId(),
//                    transfer.getSenderAccountNumber(),transfer.getSenderProfileId());
            TransferTransactionDTO data = new TransferTransactionDTO(
                    transfer.getSenderAccountNumber(),
                    beneficiaryAcctNum,
                    transfer.getAmount(),
                    "LOCAL",
                    "NGN",
                    transfer.getTranNarration(),
                    transfer.getPaymentReference(),
                    "TRANSFER", transfer.getReceiverName(), transfer.getSenderName());

            return coreBankingService.processTransaction(data, EventCharge.WAYATRAN.name(), request);
        } catch (CustomException ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @ApiOperation(value = "Notify Transaction", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/notify/transaction")
    public ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request,
            @Valid @RequestBody DirectTransactionDTO transfer) {
        log.info("Endpoint Virtu Payment Money called !!! --->> {}", transfer);

        // implement fraud or kyc check and other || or reverse transaction
        return transAccountService.VirtuPaymentMoney(request, transfer);
    }

    @ApiOperation(value = "Notify Transaction Reverse", notes = "Reverse Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/notify/transaction/reverse")
    public ResponseEntity<?> VirtuPaymentReverse(HttpServletRequest request,
            @RequestBody() ReversePaymentDTO reverseDto) {
        log.info("Endpoint Virtu Payment reverse called !!! --->> {}", reverseDto);
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

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send Money to commercial bank", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/fund/bank/account")
    public ResponseEntity<?> fundBank(HttpServletRequest request, @Valid @RequestBody BankPaymentDTO transfer) {
        log.info("Endpoint to fund bank called !!! --->> {}", transfer);
        return transAccountService.BankTransferPayment(request, transfer);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send Money to Wallet with Charge", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/sendmoney/wallet/charge")
    public ResponseEntity<?> PushsendMoney(HttpServletRequest request,
            @Valid @RequestBody WalletTransactionChargeDTO transfer) {
        log.info("Endpoint to push send money called !!! --->> {}", transfer);
        ResponseEntity<?> res = transAccountService.sendMoneyCharge(request, transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        log.info("Endpoint response to push send money: {}", res);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/sendmoney/wallet/customer")
    public ResponseEntity<?> sendMoneyCustomer(HttpServletRequest request,
            @Valid @RequestBody WalletTransactionDTO transfer) {
        log.info("Endpoint to send Money Customer called !!! --->> {}", transfer);

        ResponseEntity<?> res = transAccountService.sendMoneyCustomer(request, transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Client Send Money to Wallet", notes = "Client Post Money", tags = {"TRANSACTION-WALLET"})
    @PostMapping("/client/sendmoney/customer")
    public ResponseEntity<?> ClientSendMoney(HttpServletRequest request,
            @Valid @RequestBody ClientWalletTransactionDTO transfer) {
        log.info("Endpoint Client Send Money called !!! --->> {}", transfer);
        ResponseEntity<?> res = transAccountService.ClientSendMoneyCustomer(request, transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "Wallet Account Statement", notes = "Statement of Account", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/statement/{accountNo}")
    public ResponseEntity<?> getStatement(@PathVariable("accountNo") String accountNo) {
        log.info("Endpoint to get statement called !!! --->> {}", accountNo);
        ApiResponse<?> res = transAccountService.getStatement(accountNo);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        log.info("Statement of account: {}", accountNo);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Transfer from one User Wallet to another wallet", notes = "Transfer from one Wallet to another wallet for a user this takes customer wallet id and the Beneficiary wallet id, effective from 06/24/2021", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/fund/transfer/wallet")
    public ResponseEntity<?> handleTransactions(HttpServletRequest request,
            @RequestBody TransferTransactionDTO transactionPojo) {

        log.info("Endpoint to handle transactions called !!! --->> {}", transactionPojo);
        return transAccountService.makeWalletTransaction(request, "", transactionPojo);

    }

    // Stopped
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "To Fetch Transactions By Account Number", notes = "find transaction by Account Number pagable", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/find/transactions/{accountNo}")
    public ResponseEntity<?> findTransactionAccountNo(@PathVariable("accountNo") String accountNo,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        log.info("Endpoint to find Transaction Account No called !!! --->> {}", accountNo);
        ApiResponse<?> res = transAccountService.findByAccountNumber(page, size, accountNo);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Fetch Transaction By Wallet Id", notes = "find transaction by Wallet Id pagable", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/get/transactions/{walletId}")
    public ResponseEntity<?> findWalletTransaction(@PathVariable("walletId") Long walletId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        log.info("Endpoint to find wallet transaction called !!! --->> {}", walletId);
        ApiResponse<?> res = transAccountService.getTransactionByWalletId(page, size, walletId);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Find All Transaction pagable", notes = "find all transaction pagable", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/find/all/transactions")
    public ResponseEntity<?> findAllTransaction(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Endpoint to find all transaction called !!!");
        ApiResponse<?> res = transAccountService.findAllTransaction(page, size);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Find Transaction by tranId", notes = "find client transaction", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/account/transactions/{tranId}")
    public ResponseEntity<?> findClientTransaction(@PathVariable("tranId") String tranId) {
        log.info("Endpoint to find client transaction called !!! ---->>> {}", tranId);
        TransactionsResponse res = transAccountService.findClientTransaction(tranId);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Find Transaction by tranId", notes = "find client transaction", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/account/transactions/entries/{tranId}")
    public ResponseEntity<?> findAllTransactionEntries(@PathVariable("tranId") String tranId) {
        log.info("Endpoint to find all transaction entries called !!! ---->>> {}", tranId);
        TransactionsResponse res = transAccountService.findAllTransactionEntries(tranId);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Event and Service Payment: Reverse Payment request", notes = "Reverse Payment request", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/event/charge/reverse-payment-request")
    public ResponseEntity<?> EventReversPayment(HttpServletRequest request, @RequestBody() EventPaymentRequestReversal walletDto) {
        log.info("Endpoint Event Revers Payment called !!! ---->>> {}", walletDto);
        return transAccountService.EventReversePaymentRequest(request, walletDto);

    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/event/charge/payment")
    public ResponseEntity<?> EventPayment(HttpServletRequest request, @RequestBody() EventPaymentDTO walletDto) {
        log.info("Endpoint Event Payment called !!! ---->>> {}", walletDto);
        return transAccountService.EventTransferPayment(request, walletDto, false);

    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/non-waya/transaction/payment")
    public ResponseEntity<?> NonWayaPaymentX(HttpServletRequest request, @RequestBody() NonWayaPaymentDTO walletDto) {
        log.info("Endpoint Non Waya Payment X called !!! ---->>> {}", walletDto);
        ResponseEntity<?> res = transAccountService.EventNonPayment(request, walletDto);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/non-waya/transaction/redeem")
    public ResponseEntity<?> NonWayaRedeem(HttpServletRequest request, @RequestBody() NonWayaPaymentDTO walletDto) {
        log.info("Endpoint Non Waya redeem called !!! ---->>> {}", walletDto);
        ResponseEntity<?> res = transAccountService.EventNonRedeem(request, walletDto);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Redeem Multiple Tranc", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/non-waya/transaction/redeem-multiple")
    public ResponseEntity<?> NonWayaRedeemMultiple(HttpServletRequest request, @RequestBody() List<NonWayaPaymentDTO> walletDto) {
        log.info("Endpoint Non Waya redeem multiple called !!! ---->>> {}", walletDto);
        ResponseEntity<?> res = transAccountService.EventNonRedeemMultiple(request, walletDto);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Transaction Count", notes = "Total Transaction", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-transactions/{userId}")
    public ResponseEntity<?> totalNonePaymentRequest(@PathVariable String userId) {
        log.info("Endpoint total None Payment Request called !!! ---->>> {}", userId);
        return transAccountService.getTotalNoneWayaPaymentRequest(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Pending Count", notes = "Total Pending Count", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-pending/{userId}")
    public ResponseEntity<?> pendingNonePaymentRequest(@PathVariable String userId) {
        log.info("Endpoint to get pending None Payment Request called !!! ---->>> {}", userId);
        return transAccountService.getPendingNoneWayaPaymentRequest(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Expired Count", notes = "Total Expired", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-expired/{userId}")
    public ResponseEntity<?> expiredNonePaymentRequest(@PathVariable String userId) {
        log.info("Endpoint to get expired None Payment Request called !!! ---->>> {}", userId);
        return transAccountService.getExpierdNoneWayaPaymentRequest(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Reserved Count", notes = "Total Reserved", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-reserved/{userId}")
    public ResponseEntity<?> ReservedNonePaymentRequest(@PathVariable String userId) {
        log.info("Endpoint to get reversed None Payment Request called !!! ---->>> {}", userId);
        return transAccountService.getReservedNoneWayaPaymentRequest(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Payout Count", notes = "Total Payout", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-payout/{userId}")
    public ResponseEntity<?> PayoutNonePaymentRequest(@PathVariable String userId) {
        log.info("Endpoint to get payout None Payment Request called !!! ---->>> {}", userId);
        return transAccountService.getPayoutNoneWayaPaymentRequest(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Expired Amount", notes = "Total Expired Amount", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-expired-amount/{userId}")
    public ResponseEntity<?> expiredNonePaymentRequestAmount(@PathVariable String userId) {
        log.info("Endpoint to get expired None Payment Request amount called !!! ---->>> {}", userId);
        return transAccountService.getExpierdNoneWayaPaymentRequestAmount(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Payout Amount", notes = "Total Payout Amount", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-payout-amount/{userId}")
    public ResponseEntity<?> payoutNonePaymentRequestAmount(@PathVariable String userId) {
        log.info("Endpoint to get payout None Payment Request amount called !!! ---->>> {}", userId);
        return transAccountService.getPayoutNoneWayaPaymentRequestAmount(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Reserved Amount", notes = "Total Reserved Amount", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-reserved-amount/{userId}")
    public ResponseEntity<?> ReservedNonePaymentRequestAmount(@PathVariable String userId) {
        log.info("Endpoint to get reversed None Payment Request amount called !!! ---->>> {}", userId);
        return transAccountService.getReservedNoneWayaPaymentRequestAmount(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Transaction Count", notes = "Total Transaction", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-transactions-amount/{userId}")
    public ResponseEntity<?> totalNonePaymentRequestAmount(@PathVariable String userId) {
        log.info("Endpoint to get total None Payment Request amount called !!! ---->>> {}", userId);
        return transAccountService.getTotalNoneWayaPaymentRequestAmount(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non Waya Total Pending Amount", notes = "Total Pending Amount", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/total-pending-amount/{userId}")
    public ResponseEntity<?> pendingNonePaymentRequestAmount(@PathVariable String userId) {
        log.info("Endpoint to get pending None Payment Request amount called !!! ---->>> {}", userId);
        return transAccountService.getPendingNoneWayaPaymentRequestAmount(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/non-waya/payment/new")
    public ResponseEntity<?> nonWayaPayment(HttpServletRequest request,
            @Valid @RequestBody() NonWayaPaymentDTO walletDto) {
        log.info("Endpoint to get Non Payment called !!! ---->>> {}", walletDto);
        return transAccountService.transferToNonPayment(request, walletDto);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Payment for multiple transaction ", notes = "Transfer amount from user wallet to Non-waya for multiple transaction", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/non-waya/payment/new-multiple")
    public ResponseEntity<?> NonWayaPaymentMultiple(HttpServletRequest request,
            @Valid @RequestBody() List<NonWayaPaymentDTO> walletDto) {
        log.info("Endpoint to get Non waya Payment multiple called !!! ---->>> {}", walletDto);
        return transAccountService.TransferNonPaymentMultiple(request, walletDto);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Payment", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/list-transactions/{userId}")
    public ResponseEntity<?> getListOfNonWayaTransfers(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String userId) {
        log.info("Endpoint to get List Of Non Waya Transfers called !!! ---->>> {}", userId);
        return transAccountService.getListOfNonWayaTransfers(request, userId, page, size);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Payment list", notes = "Non-Waya Payment list", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/non-waya/payment/list-transactions")
    public ResponseEntity<?> listOfNonWayaTransfers(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Endpoint to get list Of Non Waya Transfers called !!!");
        return transAccountService.listOfNonWayaTransfers(request, page, size);
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PutMapping("/non-waya/transaction/redeem/new")
    public ResponseEntity<?> NonWayaRedeem(HttpServletRequest request,
            @Valid @RequestBody() NonWayaRedeemDTO walletDto) {
        log.info("Endpoint to get Non Waya Redeem called !!! ----->> {}", walletDto);
        return transAccountService.NonWayaPaymentRedeem(request, walletDto);
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Non-Waya Redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PutMapping("/non-waya/transaction/redeem/PIN")
    public ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request,
            @Valid @RequestBody() NonWayaPayPIN walletDto) {
        log.info("Endpoint to get Non Waya Redeem PIN called !!! ----->> {}", walletDto);
        return transAccountService.NonWayaRedeemPIN(request, walletDto);
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "QR Code Payment generation", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/qr-code/transactionpayment")
    public ResponseEntity<?> WayaQRCodeGen(HttpServletRequest request,
            @Valid @RequestBody() WayaPaymentQRCode walletDto) {
        log.info("Endpoint to get Waya QR Code Gen called !!! ----->> {}", walletDto);
        return transAccountService.WayaQRCodePayment(request, walletDto);
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "QR Code Payment redeem", notes = "Transfer amount from user wallet to Non-waya", tags = {
        "TRANSACTION-WALLET"})
    @PutMapping("/qr-code/transaction/redeem")
    public ResponseEntity<?> WayaQRCodeRedeem(HttpServletRequest request,
            @Valid @RequestBody() WayaRedeemQRCode walletDto) {
        log.info("Endpoint to get Waya QR Code Redeem called !!! ----->> {}", walletDto);
        return transAccountService.WayaQRCodePaymentRedeem(request, walletDto);
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Payment request", notes = "Transfer amount from user to User in waya", tags = {
        "TRANSACTION-WALLET"})
    @PostMapping("/payment/request/transaction")
    public ResponseEntity<?> transferPaymentUserToUser(HttpServletRequest request, @Valid @RequestBody WayaPaymentRequest transfer) {
        log.info("Endpoint to get transfer Payment User To User called !!! ----->> {}", transfer);
        return transAccountService.WayaPaymentRequestUsertoUser(request, transfer);
    }

    // Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/reverse/report")
    public ResponseEntity<?> PaymentRevReReport(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate) {
        log.info("Endpoint to get all payment rev report !!!");
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
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "To Fetch client Reverse", notes = "Transfer amount from one wallet to another wallet", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/transaction/reverse/{accountNo}")
    public ResponseEntity<?> PaymentTransReport(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
            @PathVariable("accountNo") String accountNo) {
        log.info("Endpoint to get payment rev report with acct no !!! ----->>> {}", accountNo);

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

    @ApiOperation(value = "For Client to view all waya transaction", notes = "To view all transaction for wallet/waya", tags = {
        "TRANSACTION-WALLET"})
    @GetMapping("/client/statement/{acctNo}")
    public ResponseEntity<?> StatementClient(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
            @PathVariable("acctNo") String acctNo) {
        log.info("Endpoint to get statement client with acct no !!! ----->>> {}", acctNo);
        return getResponseEntity(fromdate, todate, acctNo);
    }

    private ResponseEntity<?> getResponseEntity(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("fromdate") Date fromdate, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("todate") Date todate, @PathVariable("acctNo") String acctNo) {
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
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Admin Transaction Charge Report", notes = "Charge Report", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/charge/report")
    public ResponseEntity<?> PaymentChargeReport() {
        log.info("Endpoint to get payment charge report called !!! ");
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

    @ApiOperation(value = "To Filter Transaction Type", notes = "Filter Transaction", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/filter/{accountNo}")
    public ResponseEntity<?> PaymentTransFilter(@PathVariable("accountNo") String accountNo) {
        log.info("Endpoint to get payment trans filter called !!! ---->> {}", accountNo);
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
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "To Export Account Transaction ", notes = "Account Statement", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/export/pdf/{accountNo}")
    public ResponseEntity<?> exportToPDF(HttpServletResponse response,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
            @PathVariable String accountNo) throws IOException, com.lowagie.text.DocumentException {
        log.info("Endpoint to get export account statement to PDF called !!! ---->> {}", accountNo);
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=receipt_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);
        ApiResponse<CustomerStatement> res = transAccountService.accountstatementReport2(fromdate, todate, accountNo);       
        
        ExportPdf exporter = new ExportPdf(res.getData().getTransaction(), accountNo, fromdate, todate,
                res.getData().getAccountName(), res.getData().getOpeningBal().toString(), res.getData().getClosingBal().toString(),
                res.getData().getClearedal().toString(), res.getData().getClosingBal().toString(), res.getData().getBlockedAmount());

//                ExportPdf2 exporter = new ExportPdf2(res.getData().getTransaction(), accountNo, fromdate, todate,
//                res.getData().getAccountName(),  res.getData().getBlockedAmount());
        exporter.export(response);
        return new ResponseEntity<>(headerValue, HttpStatus.OK);

    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Total Credit Transactions Amount", notes = "Total Credit Transactions", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/total-credit")
    public ResponseEntity<?> totalCreditTransaction() {
        log.info("Endpoint to get total Credit Transaction called !!!");
        return transAccountService.creditTransactionAmount();
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Total Debit Transactions Amount", notes = "Total Debit Transactions", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/total-debit")
    public ResponseEntity<?> totalDebitTransaction() {
        log.info("Endpoint to get total debit Transaction called !!!");
        return transAccountService.debitTransactionAmount();
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Total Credit And Debit Transactions Amount", notes = "Total Credit And Debit Transactions", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/total-credit-debit")
    public ResponseEntity<?> totalCreditAndDebitTransaction() {
        log.info("Endpoint to get total Credit and debit Transaction called !!!");
        return transAccountService.debitAndCreditTransactionAmount();
    }

    @ApiOperation(value = "User Transaction Count ", notes = "User Transaction Count", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/get-user-transaction-count")
    public ResponseEntity<?> userTransactionCount() {
        log.info("Endpoint to get user transaction count called !!!");
        return transactionCountService.getAllUserCount();
    }

    @ApiOperation(value = "User Transaction Count by User Id ", notes = "User Transaction Count by User Id", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/get-user-transaction-count/{userId}/{profileId}")
    public ResponseEntity<?> getUserCount(@PathVariable String userId,@PathVariable String profileId) {
        log.info("Endpoint to get user count called !!! ---->> {}", profileId);
        return transactionCountService.getUserCount(userId,profileId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "User Transaction Fee ", notes = "User Transaction Fee", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/get-user-transaction-fee/{accountNo}/{amount}/{eventId}")
    public BigDecimal getUserTransactionFee(@PathVariable String accountNo, @PathVariable BigDecimal amount, @PathVariable String eventId) {
        log.info("Endpoint to get user transaction fee called !!! ---->> {}", accountNo);
        return transAccountService.computeTransFee(accountNo, amount, eventId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Total Overall and category Based Transactions", notes = "Total Overall and category Based Transactions", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/overall-based-analysis")
    public ResponseEntity<?> overallBasedAnalysis(HttpServletRequest request) {
        log.info("Endpoint to get overall Based Analysis called !!!}");
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            log.info(xfHeader.split(",")[0]);
        }
        log.info(request.getRemoteAddr());
        return transAccountService.transactionAnalysis();
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Total Overall Based Transactions", notes = "Total Overall Based Transactions", tags = {"TRANSACTION-WALLET"})
    @GetMapping("/transaction/analysis")
    public ResponseEntity<?> transactionFilterDate(@RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate) {
        log.info("Endpoint to get transaction analysis Filter Date called !!! ");
        return transAccountService.transactionAnalysisFilterDate(fromdate, todate);
    }

//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Fetch user transaction by reference number", notes = "get user transaction by ref number", tags = {"TRANSACTION-WALLET"})
    @GetMapping(path = "/fetchByReferenceNumber/{referenceNumber}")
    public ResponseEntity<?> fetchUserTransactionByRefNumber(@PathVariable String referenceNumber) {
        log.info("Endpoint to fetch User Transaction By Ref Number called !!! ------>>> {}", referenceNumber);
        ApiResponse<?> response = transAccountService.fetchUserTransactionsByReferenceNumber(referenceNumber);
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Fetch user transaction TSQ by reference number and account number", notes = "get TSQ reference number and account number", tags = {"TRANSACTION-WALLET"})
    @GetMapping(path = "/fetchByReferenceNumberTsq/{accountNumber}/{referenceNumber}")
    public ResponseEntity<?> doMerchantTsqByReferenceAndAcctNo(HttpServletRequest request,@RequestHeader("Authorization") String token,
                                                               @PathVariable String accountNumber,
                                                               @PathVariable String referenceNumber) {
        log.info("Endpoint to do Merchant Tsq By Reference: {} And Acct No: {} called !!!", referenceNumber, accountNumber);
        ApiResponse<?> response = transAccountService.fetchMerchantTransactionTqs(request,token,accountNumber,referenceNumber);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


}
