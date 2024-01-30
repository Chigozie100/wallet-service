package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.CoreBankingProcessService;
import com.wayapaychat.temporalwallet.service.CoreBankingService;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "ADMIN", description = "Transaction Wallet Service API")
@Validated
@Slf4j
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public class AdminController {

    private final TransAccountService transAccountService;
    private final CoreBankingService coreBankingService;
    private final UserAccountService userAccountService;
    private final CoreBankingProcessService coreBankingProcessService;

    @Autowired
    public AdminController(TransAccountService transAccountService, CoreBankingService coreBankingService,
        UserAccountService userAccountService, CoreBankingProcessService coreBankingProcessService) {
        this.transAccountService = transAccountService;
        this.coreBankingService = coreBankingService;
        this.userAccountService = userAccountService;
        this.coreBankingProcessService = coreBankingProcessService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Run CBA Accounting Process", notes = "Run CBA Accounting Process", tags = { "ADMIN" })
    @PostMapping("/cba/run-process/{proccessName}")
    public ResponseEntity<?> coreBankingRunProcess(HttpServletRequest request,
            @PathVariable String proccessName) {
        log.info("Endpoint to call core banking run process called!!! ---->> {}", proccessName);
        return coreBankingProcessService.runProcess(proccessName);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Fix CBA Accounting Entries", notes = "Fix CBA Accounting Entries", tags = { "ADMIN" })
    @PostMapping("/cba/fix-entries/{transactionId}")
    public ResponseEntity<?> coreBankingFixEntry(HttpServletRequest request,
            @PathVariable String transactionId) {
        log.info("Endpoint to call core banking fix entry called!!! ---->> {}", transactionId);
        return coreBankingProcessService.fixTransactionEntries(request, transactionId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create Account on CBA", notes = "Create CBA Account", tags = { "ADMIN" })
    @PostMapping("/cba/create-account/{accountNumber}")
    public ResponseEntity<?> coreBankingCreateAccount(HttpServletRequest request,
            @PathVariable String accountNumber) {
        log.info("Endpoint to call core banking create account called!!! ---->> {}", accountNumber);
        return userAccountService.setupAccountOnExternalCBA(accountNumber);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = { "ADMIN" })
    @PostMapping("/sendmoney/wallet-simulated-users")
    public ResponseEntity<?> sendMoneyForSimulatedUsers(HttpServletRequest request,
            @Valid @RequestBody List<TransferSimulationDTO> transfer) {
        // implement fraud or kyc check and other || or reverse transaction
        log.info("Endpoint to send Money For Simulated Users called!!! ---->> {}", transfer);
        return transAccountService.sendMoneyToSimulatedUser(request, transfer);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To transfer money from one waya official account to another", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/official/transfer")
    public ResponseEntity<?> OfficialSendMoney(HttpServletRequest request,
            @Valid @RequestBody OfficeTransferDTO transfer) {
        log.info("Endpoint to Official Send Money called!!! ---->> {}", transfer);
        TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
        BeanUtils.copyProperties(transfer, transactionDTO);
        transactionDTO.setBenefAccountNumber(transfer.getOfficeCreditAccount());
        transactionDTO.setDebitAccountNumber(transfer.getOfficeDebitAccount());
        transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
        transactionDTO.setBeneficiaryName(transfer.getReceiverName());
        transactionDTO.setSenderName(transfer.getSenderName());

        try {
            log.info("Send Money: {}", transfer);
            return coreBankingService.processTransaction(transactionDTO, "WAYAOFFTOOFF", request);

        } catch (CustomException ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To transfer money from one waya official account to user wallet", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/official/user/transfer")
    public ResponseEntity<?> OfficialUserMoneyEventID(HttpServletRequest request,
            @Valid @RequestBody OfficeUserTransferDTO transfer) {
        log.info("Endpoint to Official User Send Money Event ID called!!! ---->> {}", transfer);
        TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
        BeanUtils.copyProperties(transfer, transactionDTO);
        transactionDTO.setBenefAccountNumber(transfer.getCustomerCreditAccount());
        transactionDTO.setDebitAccountNumber(transfer.getOfficeDebitAccount());
        transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());
        transactionDTO.setBeneficiaryName(transfer.getReceiverName());
        transactionDTO.setSenderName(transfer.getSenderName());

        try {
            log.info("Send Money: {}", transfer);
            return coreBankingService.processTransaction(transactionDTO, "WAYAOFFTOCUS", request);

        } catch (CustomException ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To transfer money from one waya official account to multiple user wallets", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/official/user/transfer-multiple")
    public ResponseEntity<?> OfficialUserMoneyMultiple(HttpServletRequest request,
            @Valid @RequestBody List<OfficeUserTransferDTO> transfer) {
        log.info("Endpoint to Official User Send Money Multiple called!!! ---->> {}", transfer);
        ResponseEntity<?> res = transAccountService.OfficialUserTransferMultiple(request, transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money From Official Account to commercial bank", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/Official/fund/bank/account")
    public ResponseEntity<?> officialFundBank(HttpServletRequest request,
            @Valid @RequestBody BankPaymentOfficialDTO transfer) {
        log.info("Endpoint to Official fund bank called!!! ---->> {}", transfer);
        return transAccountService.BankTransferPaymentOfficial(request, transfer);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money From Official Account to commercial bank", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/Official/fund/bank/mutilple-account")
    public ResponseEntity<?> officialFundBankMultiple(HttpServletRequest request,
            @Valid @RequestBody List<BankPaymentOfficialDTO> transfer) {
        log.info("Endpoint to Official fund bank multiple called!!! ---->> {}", transfer);
        return transAccountService.BankTransferPaymentOfficialMultiple(request, transfer);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money to Wallet", notes = "Post Money", tags = { "ADMIN" })
    @PostMapping("/admin/sendmoney")
    public ResponseEntity<?> AdminsendMoney(HttpServletRequest request,
            @Valid @RequestBody AdminLocalTransferDTO transfer) {
        log.info("Endpoint Admin send money called!!! ---->> {}", transfer);
        ApiResponse<?> res = transAccountService.AdminsendMoney(request, transfer);
        log.info("Endpoint Response Admin send money ---->> {}", res);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money to Wallet: Multiple Transaction", notes = "Post Money", tags = { "ADMIN" })
    @PostMapping("/admin/sendmoney-multiple")
    public ResponseEntity<?> AdminSendMoneyMultiple(HttpServletRequest request,
            @Valid @RequestBody List<AdminLocalTransferDTO> transfer) {
        log.info("Endpoint Admin send money multiple called!!! ---->> {}", transfer);
        ApiResponse<?> res = transAccountService.AdminSendMoneyMultiple(request, transfer);
        log.info("Send Money: {}", transfer);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/admin/commission/transfer")
    public ResponseEntity<?> AdminCommissionMoney(HttpServletRequest request,
            @Valid @RequestBody CommissionTransferDTO transfer) {
        log.info("Endpoint Admin commission money called!!! ---->> {}", transfer);
        ResponseEntity<?> res = transAccountService.AdminCommissionMoney(request, transfer);
        log.info("Send Money: {}", transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money from Commission to Default Wallet", notes = "Post Money", tags = {
            "TRANSACTION-WALLET" })
    @PostMapping("/client/commission/transfer")
    public ResponseEntity<?> CommissionMoney(HttpServletRequest request,
            @Valid @RequestBody ClientComTransferDTO transfer) {
        log.info("Endpoint commission money called!!! ---->> {}", transfer);
        ResponseEntity<?> res = transAccountService.ClientCommissionMoney(request, transfer);
        log.info("commission Money response : {}", transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money to Wallet", notes = "Admin Post Money", tags = { "ADMIN" })
    @PostMapping("/admin/sendmoney/customer")
    public ResponseEntity<?> AdminSendMoney(HttpServletRequest request,
            @Valid @RequestBody AdminWalletTransactionDTO transfer) {
        log.info("Endpoint Admin send money called!!! ---->> {}", transfer);
        ApiResponse<?> res = transAccountService.AdminSendMoneyCustomer(request, transfer);
        log.info("Admin send money response: {}", res);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "Report Account Transaction Statement", tags = { "ADMIN" })
    @GetMapping(path = "/official/account/statement/{accountNo}")
    public ResponseEntity<?> GetAccountStatement(@PathVariable String accountNo) {
        log.info("Endpoint Get account statement called!!! ---->> {}", accountNo);
        ApiResponse<?> res = transAccountService.ReportTransaction(accountNo);
        log.info("Endpoint Get account statement response !!! ---->> {}", accountNo);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/admin/wallet/funding")
    public ResponseEntity<?> AdminTransferForUser(HttpServletRequest request,
            @RequestBody() AdminUserTransferDTO walletDto, @RequestParam("command") String command) {
        log.info("Endpoint Admin Transfer For User called!!! ---->> {}", walletDto);
        return transAccountService.adminTransferForUser(request, command, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/admin/wallet/payment")
    public ResponseEntity<?> AdminPaymentService(HttpServletRequest request,
            @RequestBody() WalletAdminTransferDTO walletDto, @RequestParam("command") String command) {
        log.info("Endpoint Admin Payment Service called!!! ---->> {}", walletDto);
        ResponseEntity<?> res = transAccountService.cashTransferByAdmin(request, command, walletDto);
        log.info("Endpoint Response Admin Payment Service!!! ---->> {}", res);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    // Wallet call by other service
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Office Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/event/office/payment")
    public ResponseEntity<?> EventOfficePayment(HttpServletRequest request,
            @RequestBody() EventOfficePaymentDTO walletDto) {
        log.info("Endpoint Event Office Payment called!!! ---->> {}", walletDto);
        return transAccountService.EventOfficePayment(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Office Event: Temporal - Official Transfer Multiple", notes = "Transfer amount from Temporal wallet to Official wallet mutiliple transaction", tags = {
            "ADMIN" })
    @PostMapping("/event/office/temporal-to-official-multiple")
    public ResponseEntity<?> TemporalToOfficialWalletDTO(HttpServletRequest request,
            @RequestBody() List<TemporalToOfficialWalletDTO> walletDto) {
        log.info("Endpoint Temporal To Official Wallet multiple called!!! ---->> {}", walletDto);
        return transAccountService.TemporalWalletToOfficialWalletMutiple(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Office Event: Temporal - Official Transfer Multiple", notes = "Transfer amount from Temporal wallet to Official wallet mutiliple transaction", tags = {
            "ADMIN" })
    @PostMapping("/event/office/temporal-to-official")
    public ResponseEntity<?> TemporalToOfficialWallet(HttpServletRequest request,
            @RequestBody() TemporalToOfficialWalletDTO walletDto) {
        log.info("Endpoint Temporal To Official Wallet called!!! ---->> {}", walletDto);
        return transAccountService.TemporalWalletToOfficialWallet(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin send Non-Waya Payment with excel upload on behalf of users", notes = "Admin send Non-Waya Payment with excel upload on behalf of users", tags = {
            "ADMIN" })
    @PostMapping(path = "/non-waya/payment/new-multiple-excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> NonWayaPaymentMultipleUpload(HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {
        log.info("Endpoint Non Waya Payment Multiple Upload via multipart file called!!! ");
        return transAccountService.TransferNonPaymentMultipleUpload(request, file);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Non-Waya Payment for Single transaction by waya official", notes = "Transfer amount from user wallet to Non-waya for single transaction by waya  official", tags = {
            "ADMIN" })
    @PostMapping("/non-waya/payment/new-single-waya-official")
    public ResponseEntity<?> NonWayaPaymentSingleWayaOfficial(HttpServletRequest request,
            @Valid @RequestBody() NonWayaPaymentMultipleOfficialDTO walletDto) {
        log.info("Endpoint Non Waya Payment single waya official called!!! ---->>> {}", walletDto);
        return transAccountService.TransferNonPaymentSingleWayaOfficial(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Non-Waya Payment for multiple transaction by waya official", notes = "Transfer amount from user wallet to Non-waya for multiple transaction by waya  official", tags = {
            "ADMIN" })
    @PostMapping("/non-waya/payment/new-multiple-waya-official")
    public ResponseEntity<?> NonWayaPaymentMultipleWayaOfficial(HttpServletRequest request,
            @Valid @RequestBody() List<NonWayaPaymentMultipleOfficialDTO> walletDto) {
        log.info("Endpoint Non Waya Payment multiple waya official called!!! ---->>> {}", walletDto);
        return transAccountService.TransferNonPaymentMultipleWayaOfficial(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping(path = "/non-waya/payment/new-multiple-official-excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> TransferNonPaymentWayaOfficialExcel(HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {
        log.info("Endpoint Transfer Non Payment Waya Official Excel called!!!");
        return new ResponseEntity<>(transAccountService.TransferNonPaymentWayaOfficialExcel(request, file),
                HttpStatus.OK);
    }

    @ApiOperation(value = "Download Template for Bulk User Creation ", tags = { "ADMIN" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "Response Headers") })
    @GetMapping("/download/bulk-none-waya-excel")
    public ResponseEntity<Resource> getFile(@RequestParam("isNoneWaya") String isNoneWaya) {
        log.info("Endpoint to get file called!!!");
        String filename = "bulk-none-waya-excel.xlsx";
        InputStreamResource file = new InputStreamResource(transAccountService.createExcelSheet(isNoneWaya));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    // Wallet call by other service
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/admin/commission/payment")
    public ResponseEntity<?> CommissionPaymentAdmin(HttpServletRequest request,
            @RequestBody() EventPaymentDTO walletDto) {
        log.info("Endpoint to Commission Payment Admin called!!! --->> {}", walletDto);
        return transAccountService.EventCommissionPayment(request, walletDto);
    }

    @ApiOperation(value = "Commission History", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @GetMapping("/admin/commission/history")
    public ResponseEntity<?> CommissiomPaymentList() {
        log.info("Endpoint to Commission Payment list called!!!");
        ApiResponse<?> res = transAccountService.CommissionPaymentHistory();
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "To Fetch Official Transaction activities", notes = "To Fetch Official Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/official/transaction/{wayaNo}")
    public ResponseEntity<?> PaymentWayaReport(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
            @PathVariable("wayaNo") String wayaNo) {
        log.info("Endpoint for payment waya report called --->>> {}", wayaNo);
        ApiResponse<?> res;
        try {
            res = transAccountService.PaymentAccountTrans(fromdate, todate, wayaNo);
            log.info("Endpoint Response for payment waya report --->>> {}", res);
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

    @ApiOperation(value = "To Fetch Transaction activities per user", notes = "To Fetch Transaction activities per user", tags = {
            "ADMIN" })
    @GetMapping("/official/transaction/{wayaNo}/user")
    public ResponseEntity<?> PaymentWayaReportPerUser(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
            @PathVariable("wayaNo") String wayaNo) {
        log.info("Endpoint for payment waya report per user called --->>> {}", wayaNo);
        ApiResponse<?> res;
        try {
            res = transAccountService.PaymentAccountTrans(fromdate, todate, wayaNo);
            log.info("Endpoint Response for payment waya report per user --->>> {}", res);
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
    @ApiOperation(value = "To List Official Transaction activities", notes = "To List Official Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/official/transaction")
    public ResponseEntity<?> PaymentOffWaya(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter) {
        log.info("Endpoint for payment off waya ");
        ApiResponse<?> res;
        try {
            res = transAccountService.PaymentOffTrans(page, size, filter);
            log.info("Endpoint Response for payment off waya --->>> {}", res);
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
    @ApiOperation(value = "To List All Transaction activities", notes = "To List all Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/all/transaction")
    public ResponseEntity<?> allTransactions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate) {
        log.info("Endpoint get all transactions");
        ApiResponse<?> res;
        try {
            res = transAccountService.getAllTransactions(page, size, filter, fromdate, todate);
            log.info("Endpoint response get all transactions");
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
    @ApiOperation(value = "To List All Transaction activities", notes = "To List all Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/all/transaction/{accountNo}")
    public ResponseEntity<?> allTransactionsByAcctNo(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate,
            @PathVariable("accountNo") String accountNo) {
        log.info("Endpoint get transactions by acct number ---->> {}", accountNo);
        ApiResponse<?> res;
        try {
            res = transAccountService.getAllTransactionsByAccountNo(page, size, filter, fromdate, todate, accountNo);
            log.info("Endpoint response get transactions by acct number ---->> {}", res);
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
    @ApiOperation(value = "Official Account Reports", notes = "Official Account Reports", tags = {
            "ADMIN" })
    @GetMapping("/offical-account/reports")
    public ResponseEntity<?> OfficialAccountReports(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate) {
        log.info("Endpoint get official account reports called !!!!");
        ApiResponse<?> res;
        try {
            res = transAccountService.OfficialAccountReports(page, size, fromdate, todate, filter);
            log.info("Endpoint response get official account reports ====>>> {}", res);
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

    // ApiResponse<?> OfficialAccountReports(int page, int size, String fillter,
    // Date fromdate, Date todate)

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin to Fetch all Reversal", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @GetMapping("/all/reverse/report")
    public ResponseEntity<?> PaymentAllReverse() {
        log.info("Endpoint for payment reverse called !!!!!");
        ApiResponse<?> res;
        try {
            res = transAccountService.TranALLReverseReport();
            log.info("Endpoint response for payment reverse ---->> {}", res);
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
            "ADMIN" })
    @PostMapping("/credit/bulk-transaction")
    public ResponseEntity<?> createCreditBulkTrans(HttpServletRequest request,
            @Valid @RequestBody BulkTransactionCreationDTO userList) {
        log.info("Endpoint to create credit bulk trans called ---->>> {}", userList);
        ResponseEntity<?> res = transAccountService.createBulkTransaction(request, userList);
        log.info("Endpoint response to create credit bulk trans ---->> {}", res);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", userList);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping(path = "/credit/bulk-transaction-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCreditBulkTransExcel(HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {
        log.info("Endpoint to create credit bulk trans excel called ");
        ResponseEntity<?> res = transAccountService.createBulkExcelTrans(request, file);
        log.info("Endpoint response to create credit bulk trans excel ---->> {}", res);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", file);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true),
            @ApiImplicitParam(name = "pin", value = "pin", paramType = "header", required = true) })
    @ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/debit/bulk-transaction")
    public ResponseEntity<?> createDebitBulkTrans(HttpServletRequest request,
            @Valid @RequestBody BulkTransactionCreationDTO userList) {
        log.info("Endpoint to create debit bulk trans called ---->> {}", userList);
        ResponseEntity<?> res = transAccountService.createBulkDebitTransaction(request, userList);
        log.info("Endpoint response to create debit bulk trans ---->> {}", res);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", userList);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true),
            @ApiImplicitParam(name = "pin", value = "pin", paramType = "header", required = true)
    })
    @ApiOperation(value = "Waya Admin to create multiple debit transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping(path = "/debit/bulk-transaction-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createDebitBulkTransExcel(HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {
        log.info("Endpoint to create debit bulk trans excel called ");
        ResponseEntity<?> res = transAccountService.createBulkDebitExcelTrans(request, file);
        log.info("Endpoint response to create debit bulk trans excel ---->> {}", res);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", file);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "For Admin to view all waya transaction", notes = "To view all transaction for wallet/waya", tags = {
            "ADMIN" })
    @GetMapping("/admin/statement/{acctNo}")
    public ResponseEntity<?> StatementReport(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
            @PathVariable("acctNo") String acctNo) {
        log.info("Endpoint for statement report called!!!! ---->> {}", acctNo);
        ApiResponse<?> res;
        try {
            res = transAccountService.statementReport(fromdate, todate, acctNo);
            log.info("Endpoint response statement report ---->> {}", res);
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

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = {
            "ADMIN" })
    @GetMapping("/transaction/get-official-debit-credit-count")
    public ResponseEntity<?> getUserCount() {
        log.info("Endpoint to get user count called !!!");
        return transAccountService.debitAndCreditTransactionAmountOfficial();
    }

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = {
            "ADMIN" })
    @GetMapping("/transaction/get-official-credit-count")
    public ResponseEntity<?> getCreditTransactionAmountOfficial() {
        log.info("Endpoint to get Credit Transaction Amount Official called !!!");
        return transAccountService.creditTransactionAmountOffical();
    }

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = {
            "ADMIN" })
    @GetMapping("/transaction/get-official-debit-transaction-count")
    public ResponseEntity<?> getDebitTransactionAmountOfficial() {
        log.info("Endpoint to get debit Transaction Amount Official called !!!");
        return transAccountService.debitTransactionAmountOffical();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Transaction Reversal", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/transaction/reverse")
    public ResponseEntity<?> PaymentReversal(HttpServletRequest request,
            @RequestBody() ReverseTransactionDTO reverseDto) throws ParseException {
        log.info("Endpoint Payment Reversal called !!! ----->>> {}", reverseDto);
        return coreBankingService.processTransactionReversal(reverseDto, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "get official account by event id  ", notes = "get official account by event id", tags = {
            "ADMIN" })
    @GetMapping("/offical-account/{eventID}")
    public ResponseEntity<?> getSingleAccountByEventID(@PathVariable("eventID") String eventId) {
        log.info("Endpoint get Single Account By EventID called !!! ----->>> {}", eventId);
        return transAccountService.getSingleAccountByEventID(eventId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "List all wallet accounts", tags = { "ADMIN" })
    @GetMapping(path = "/wallet/account")
    public ResponseEntity<?> ListAllWalletAccount() {
        return userAccountService.getListWalletAccount();
    }

    @ApiOperation(value = "Get simulated Account", tags = { "ADMIN" })
    @GetMapping(path = "/simulated/{user_id}")
    public ResponseEntity<?> GetAcctSimulated(@PathVariable Long user_id) {
        log.info("Endpoint Get Acct Simulated called !!! ----->>> {}", user_id);
        return userAccountService.getAccountSimulated(user_id);
    }

    @ApiOperation(value = "List all simulated accounts", tags = { "ADMIN" })
    @GetMapping(path = "/simulated/account")
    public ResponseEntity<?> ListAllSimulatedAccount() {
        log.info("Endpoint list of Acct Simulated called !!!");
        return userAccountService.getListSimulatedAccount();
    }

    @ApiOperation(value = "Create a Simulated User", tags = { "ADMIN" })
    @PostMapping(path = "simulated/account")
    public ResponseEntity<?> createSIMUser(HttpServletRequest request,@Valid @RequestBody AccountPojo2 user,
            @RequestHeader("Authorization") String token) {
        log.info("create SIM user input: {}", user);
        return userAccountService.createAccount(request,user, token);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a wallet account", tags = { "ADMIN" })
    @PostMapping(path = "/official/user/account")
    public ResponseEntity<?> createUserAccount(HttpServletRequest request,@Valid @RequestBody AccountPojo2 accountPojo,
            @RequestHeader("Authorization") String token) {
        log.info("Endpoint create user account called !!! --->> {}", accountPojo);
        return userAccountService.createAccount(request,accountPojo, token);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a waya official account", tags = { "ADMIN" })
    @PostMapping(path = "/official/waya/account")
    public ResponseEntity<?> createOfficialAccount(@Valid @RequestBody OfficialAccountDTO account) {
        log.info("Endpoint create official account called !!! --->> {}", account);
        return userAccountService.createOfficialAccount(account);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a waya official account", tags = { "ADMIN" })
    @PostMapping(path = "/official/waya/account-multiple")
    public ArrayList<Object> createMultipleOfficialAccount(@Valid @RequestBody List<OfficialAccountDTO> account) {
        log.info("Endpoint create multiple official account called !!! --->> {}", account);
        return userAccountService.createOfficialAccount(account);
    }

    @ApiOperation(value = "List all waya official accounts", tags = { "ADMIN" })
    @GetMapping(path = "/waya/official/account")
    public ResponseEntity<?> ListAllWayaAccount() {
        log.info("Endpoint to list all account called !!!");
        return userAccountService.getListWayaAccount();
    }

    @ApiOperation(value = "Delete,Pause and Block User Account", tags = { "ADMIN" })
    @PostMapping(path = "/user/account/access")
    public ResponseEntity<?> postAccountRestriction(@Valid @RequestBody AdminAccountRestrictionDTO user) {
        log.info("Endpoint to post Account Restriction called!!!: {}", user);
        return userAccountService.UserAccountAccess(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Delete User Account", tags = { "ADMIN" })
    @PostMapping(path = "/user/account/delete")
    public ResponseEntity<?> postAccountUser(@Valid @RequestBody UserAccountDelete user) {
        log.info("Endpoint to post Account user called!!!: {}", user);
        return userAccountService.AccountAccessDelete(user);
    }

    @ApiOperation(value = "Pause Account / Freeze Account", tags = { "ADMIN" })
    @PostMapping(path = "/account/pause")
    public ResponseEntity<?> postAccountPause(@Valid @RequestBody AccountFreezeDTO user) {
        log.info("Endpoint to post Account pause called!!!: {}", user);
        return userAccountService.AccountAccessPause(user);
    }

    @ApiOperation(value = " Block / UnBlock", tags = { "ADMIN" })
    @PostMapping(path = "/account/block")
    public ResponseEntity<?> postAccountBlock(@Valid @RequestBody AccountBlockDTO user, HttpServletRequest request) {
        log.info("Endpoint to post Account block called!!!: {}", user);
        return userAccountService.AccountAccessBlockAndUnblock(user, request);
    }

    @ApiOperation(value = "Delete Account / Block / UnBlock", tags = { "ADMIN" })
    @PostMapping(path = "/account/closure")
    public ResponseEntity<?> postAccountClosure(@Valid @RequestBody AccountCloseDTO user) {
        log.info("Endpoint to post Account closure called!!!: {}", user);
        return userAccountService.AccountAccessClosure(user);
    }

    @ApiOperation(value = "Delete Multiple Account", tags = { "ADMIN" })
    @PostMapping(path = "/account/closure-multiple")
    public ResponseEntity<?> postAccountClosureMultiple(@Valid @RequestBody List<AccountCloseDTO> user) {
        log.info("Endpoint to post Account closure multiple called!!!: {}", user);
        return userAccountService.AccountAccessClosureMultiple(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Transaction account block / unblock", tags = { "ADMIN" })
    @PostMapping(path = "/account/lien/transaction")
    public ResponseEntity<?> postAccountLien(@Valid @RequestBody AccountLienDTO user) {
        log.info("Endpoint to post Account lien called!!!: {}", user);
        return userAccountService.AccountAccessLien(user);
    }

    @ApiOperation(value = "Create Admin Cash Wallet - (Admin COnsumption Only)", tags = { "ADMIN" })
    @PostMapping(path = "/cash/account")
    public ResponseEntity<?> createCashAccounts(HttpServletRequest request,@Valid @RequestBody WalletCashAccountDTO user) {
        log.info("Endpoint to create cash accounts called!!!: {}", user);
        return userAccountService.createCashAccount(request,user);
        // return userAccountService.createCashAccount(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create Event Wallet Account - (Admin COnsumption Only)", tags = { "ADMIN" })
    @PostMapping(path = "/event/account")
    public ResponseEntity<?> createEventAccounts(@Valid @RequestBody WalletEventAccountDTO user) {
        log.info("Endpoint to create event accounts called!!! : {}", user);
        return userAccountService.createEventAccount(user);
        // return userAccountService.createEventAccount(user);
    }

    @ApiOperation(value = "Generate Account Statement", tags = { "ADMIN" })
    @GetMapping(path = "/admin/account/statement/{accountNo}")
    public ResponseEntity<?> GenerateAccountStatement(@PathVariable String accountNo) {
        log.info("Endpoint to generate account statement called!!! ---->>> {}", accountNo);
        ApiResponse<?> res = userAccountService.fetchTransaction(accountNo);
        log.info("Endpoint response to generate account statement ---->>> {}", res);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "List User wallets", tags = { "ADMIN" })
    @GetMapping(path = "/admin/user/accounts/{user_id}")
    public ResponseEntity<?> GetListAccount(HttpServletRequest request,@PathVariable long user_id) {
        log.info("Endpoint to get list of accounts called!!! ---->>> {}", user_id);
        return userAccountService.ListUserAccount(request,user_id);
    }

    @ApiOperation(value = "Get All Wallets - (Admin Consumption Only)", tags = { "ADMIN" })
    @GetMapping(path = "/all-wallets")
    public ResponseEntity<?> getAllAccounts() {
        log.info("Endpoint to get all accounts called!!!");
        return userAccountService.getAllAccount();
    }

    @ApiOperation(value = "List all Commission Accounts", tags = { "ADMIN" })
    @GetMapping(path = "/commission-wallets/all")
    public ResponseEntity<?> GetAllCommissionAccounts() {
        log.info("Endpoint to get all commission accounts called!!!");
        return userAccountService.getALLCommissionAccount();
    }

    @ApiOperation(value = "Get List of Commission Accounts", tags = { "ADMIN" })
    @GetMapping(path = "/commission-wallets")
    public ResponseEntity<?> ListAllCommissionAccounts(@RequestBody List<Integer> ids) {
        log.info("Endpoint to get a list of commission accounts called!!!---->> {}", ids);
        return userAccountService.getListCommissionAccount(ids);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet Selected Account Detail", tags = { "ADMIN" })
    @GetMapping(path = "admin/account/{accountNo}")
    public ResponseEntity<?> GetAcctDetail(@PathVariable String accountNo) {
        log.info("Endpoint to fetch account details called!!! ---->> {}", accountNo);
        return userAccountService.fetchAccountDetail(accountNo, true);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet Account Info By Account Number", tags = { "ADMIN" })
    @GetMapping(path = "admin/user-account/{accountNo}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String accountNo) throws Exception {
        log.info("Endpoint to get account details called!!! ---->> {}", accountNo);
        return userAccountService.getAccountDetails(accountNo, true);
    }

    @ApiOperation(value = "Admin Get User Wallet Commission Account", tags = { "ADMIN" })
    @GetMapping(path = "/admin/commission-accounts/{user_id}")
    public ResponseEntity<?> getCommissionAccounts(@PathVariable long user_id,
                                                   @RequestParam(name = "profileId",required = false) String profileId) {
        log.info("Endpoint to get commission accounts called!!!---->> {}", profileId);
        return userAccountService.getUserCommissionList(user_id, true,profileId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To List All Accounts", notes = "To List all Accounts", tags = {
            "ADMIN" })
    @GetMapping("/all/accounts")
    public ResponseEntity<?> allAccounts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate) {

        log.info("Endpoint to get all accounts called!!! ");
        ApiResponse<?> res;
        try {
            res = userAccountService.getAllAccounts(page, size, filter, fromdate, todate);
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
    @ApiOperation(value = "To List All Transaction activities", notes = "To List all Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/transaction-list")
    public ResponseEntity<?> allTransactionsNoPagiination(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate) {
        log.info("Endpoint to get all transactions without pagination called!!! ");
        ApiResponse<?> res;
        try {
            res = transAccountService.getAllTransactionsNoPagination(fromdate, todate);
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
    @ApiOperation(value = "To List All Transaction activities no pagination", notes = "To List all Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/transaction-list/{accountNo}")
    public ResponseEntity<?> allTransactionsNoPagination(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate,
            @PathVariable("accountNo") String accountNo) {
        log.info("Endpoint to get all transactions with account number without pagination called!!! ---->> {}", accountNo);
        ApiResponse<?> res;
        try {
            res = transAccountService.getAllTransactionsByAccountNo(fromdate, todate, accountNo);
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
    @ApiOperation(value = "Generate All users transaction analysis", notes = "To Users transaction analysis", tags = {
            "ADMIN" })
    @GetMapping("/all/users/analysis")
    public ResponseEntity<ApiResponse<?>> fetchAllUserAccountStats() {
        log.info("Endpoint to Fetch All User Account Stats called!!! ");
        ApiResponse<?> response = userAccountService.fetchAllUsersTransactionAnalysis();
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Fetch single user transactions analysis for referral commission", notes = "customer transactions analysis for referral commission", tags = {
            "ADMIN" })
    @GetMapping("/user/trns/analysis/{userId}")
    public ResponseEntity<ApiResponse<?>> fetchUserTransactionStatForReferral(@PathVariable String userId,
                                                                              @RequestParam String accountNumber,
                                                                              @RequestParam String profileId){
        log.info("Endpoint to fetch User Transaction Stat For Referral called!!! --->> {}", profileId);
        ApiResponse<?> response = userAccountService.fetchUserTransactionStatForReferral(userId,accountNumber,profileId);
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Fetch user transaction by reference number", notes = "get user transaction by ref number", tags = {
            "ADMIN" })
    @GetMapping(path = "/fetchTransByReferenceNumber/{referenceNumber}")
    public ResponseEntity<?> fetchTransactionByRefNumber(@PathVariable String referenceNumber) {
        log.info("Endpoint to fetch User Transaction by ref number called!!! --->> {}", referenceNumber);
        ApiResponse<?> response = transAccountService.fetchUserTransactionsByReferenceNumber(referenceNumber);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Fetch user account transaction by reference number", notes = "get user account transaction by ref number", tags = {
            "ADMIN" })
    @GetMapping(path = "/fetchTransByReferenceNumber/{referenceNumber}/accountNumber/{accountNumber}")
    public ResponseEntity<?> fetchAccountTransactionByRefNumberAndAcctNumber(@PathVariable String referenceNumber,
            @PathVariable String accountNumber) {
        log.info("Endpoint to fetch User Transaction by ref number {} and account number {} called!!!", referenceNumber, accountNumber);
        ApiResponse<?> response = transAccountService.fetchTransactionsByReferenceNumberAndAccountNumber(accountNumber,
                referenceNumber);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update account description", notes = "update account description", tags = {
            "ADMIN" })
    @PostMapping("/update/account-description/{accountNumber}")
    public ResponseEntity<?> updateAccountDescription(@PathVariable String accountNumber,
            @RequestParam String description,
            @RequestHeader("Authorization") String token) {
        log.info("Endpoint to update Account Description called!!!--->> {}", description);
        ResponseEntity<?> response = userAccountService.updateAccountDescription(accountNumber, token, description);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCodeValue()));
    }
    
    
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update account description", notes = "update account description", tags = {
            "ADMIN" })
     @PostMapping("/update/account-name/{accountNumber}")
    public ResponseEntity<?> updateAccountName(@PathVariable String accountNumber, @RequestParam String name,
            @RequestHeader("Authorization") String token){
        log.info("Endpoint to update Account name called!!!--->> {}", name);
        ApiResponse<?> response = userAccountService.updateAccountName(accountNumber, token, name);
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update debit limit", notes = "update debit limit", tags = {
            "ADMIN" })
    @PostMapping("/update/debit-limit/{accountNumber}")
    public ResponseEntity<?> updateDebitLimit(@PathVariable String accountNumber, @RequestParam double limit,
                                               @RequestHeader("Authorization") String token){
        log.info("Endpoint to update debit limit called!!! --->> {}", limit);
        ApiResponse<?> response = userAccountService.updateDebitLimit(accountNumber, token, limit);
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }

}
