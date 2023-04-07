package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.response.ApiResponse;
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

    @Autowired
    public AdminController(TransAccountService transAccountService, CoreBankingService coreBankingService,
            UserAccountService userAccountService) {
        this.transAccountService = transAccountService;
        this.coreBankingService = coreBankingService;
        this.userAccountService = userAccountService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Send Money to Wallet", notes = "Post Money", tags = { "ADMIN" })
    @PostMapping("/sendmoney/wallet-simulated-users")
    public ResponseEntity<?> sendMoneyForSimulatedUsers(HttpServletRequest request,
            @Valid @RequestBody List<TransferSimulationDTO> transfer) {
        // implement fraud or kyc check and other || or reverse transaction

        return transAccountService.sendMoneyToSimulatedUser(request, transfer);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To transfer money from one waya official account to another", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/official/transfer")
    public ResponseEntity<?> OfficialSendMoney(HttpServletRequest request,
            @Valid @RequestBody OfficeTransferDTO transfer) {

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

        return transAccountService.BankTransferPaymentOfficial(request, transfer);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money From Official Account to commercial bank", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/Official/fund/bank/mutilple-account")
    public ResponseEntity<?> officialFundBankMultiple(HttpServletRequest request,
            @Valid @RequestBody List<BankPaymentOfficialDTO> transfer) {

        return transAccountService.BankTransferPaymentOfficialMultiple(request, transfer);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money to Wallet", notes = "Post Money", tags = { "ADMIN" })
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
    @ApiOperation(value = "Admin Send Money to Wallet: Multiple Transaction", notes = "Post Money", tags = { "ADMIN" })
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
            "ADMIN" })
    @PostMapping("/admin/commission/transfer")
    public ResponseEntity<?> AdminCommissionMoney(HttpServletRequest request,
            @Valid @RequestBody CommissionTransferDTO transfer) {
        ResponseEntity<?> res = transAccountService.AdminCommissionMoney(request, transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
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

        ResponseEntity<?> res = transAccountService.ClientCommissionMoney(request, transfer);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money to Wallet", notes = "Admin Post Money", tags = { "ADMIN" })
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

    @ApiOperation(value = "Report Account Transaction Statement", tags = { "ADMIN" })
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
            "ADMIN" })
    @PostMapping("/admin/wallet/funding")
    public ResponseEntity<?> AdminTransferForUser(HttpServletRequest request,
            @RequestBody() AdminUserTransferDTO walletDto, @RequestParam("command") String command) {
        return transAccountService.adminTransferForUser(request, command, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Transfer from Waya to another wallet", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/admin/wallet/payment")
    public ResponseEntity<?> AdminPaymentService(HttpServletRequest request,
            @RequestBody() WalletAdminTransferDTO walletDto, @RequestParam("command") String command) {
        ResponseEntity<?> res = transAccountService.cashTransferByAdmin(request, command, walletDto);
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

        return transAccountService.EventOfficePayment(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Office Event: Temporal - Official Transfer Multiple", notes = "Transfer amount from Temporal wallet to Official wallet mutiliple transaction", tags = {
            "ADMIN" })
    @PostMapping("/event/office/temporal-to-official-multiple")
    public ResponseEntity<?> TemporalToOfficialWalletDTO(HttpServletRequest request,
            @RequestBody() List<TemporalToOfficialWalletDTO> walletDto) {
        return transAccountService.TemporalWalletToOfficialWalletMutiple(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Office Event: Temporal - Official Transfer Multiple", notes = "Transfer amount from Temporal wallet to Official wallet mutiliple transaction", tags = {
            "ADMIN" })
    @PostMapping("/event/office/temporal-to-official")
    public ResponseEntity<?> TemporalToOfficialWallet(HttpServletRequest request,
            @RequestBody() TemporalToOfficialWalletDTO walletDto) {
        return transAccountService.TemporalWalletToOfficialWallet(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin send Non-Waya Payment with excel upload on behalf of users", notes = "Admin send Non-Waya Payment with excel upload on behalf of users", tags = {
            "ADMIN" })
    @PostMapping(path = "/non-waya/payment/new-multiple-excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> NonWayaPaymentMultipleUpload(HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {
        return transAccountService.TransferNonPaymentMultipleUpload(request, file);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Non-Waya Payment for Single transaction by waya official", notes = "Transfer amount from user wallet to Non-waya for single transaction by waya  official", tags = {
            "ADMIN" })
    @PostMapping("/non-waya/payment/new-single-waya-official")
    public ResponseEntity<?> NonWayaPaymentSingleWayaOfficial(HttpServletRequest request,
            @Valid @RequestBody() NonWayaPaymentMultipleOfficialDTO walletDto) {
        return transAccountService.TransferNonPaymentSingleWayaOfficial(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Non-Waya Payment for multiple transaction by waya official", notes = "Transfer amount from user wallet to Non-waya for multiple transaction by waya  official", tags = {
            "ADMIN" })
    @PostMapping("/non-waya/payment/new-multiple-waya-official")
    public ResponseEntity<?> NonWayaPaymentMultipleWayaOfficial(HttpServletRequest request,
            @Valid @RequestBody() List<NonWayaPaymentMultipleOfficialDTO> walletDto) {
        return transAccountService.TransferNonPaymentMultipleWayaOfficial(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Waya Admin to create multiple transaction", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping(path = "/non-waya/payment/new-multiple-official-excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> TransferNonPaymentWayaOfficialExcel(HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {

        return new ResponseEntity<>(transAccountService.TransferNonPaymentWayaOfficialExcel(request, file),
                HttpStatus.OK);
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

    // Wallet call by other service
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Event and Service Payment", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/admin/commission/payment")
    public ResponseEntity<?> CommissiomPaymentAdmin(HttpServletRequest request,
            @RequestBody() EventPaymentDTO walletDto) {

        return transAccountService.EventCommissionPayment(request, walletDto);
    }

    @ApiOperation(value = "Commission History", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @GetMapping("/admin/commission/history")
    public ResponseEntity<?> CommissiomPaymentList() {
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

    @ApiOperation(value = "To Fetch Transaction activities per user", notes = "To Fetch Transaction activities per user", tags = {
            "ADMIN" })
    @GetMapping("/official/transaction/{wayaNo}/user")
    public ResponseEntity<?> PaymentWayaReportPerUser(
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
    @ApiOperation(value = "To List Official Transaction activities", notes = "To List Official Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/official/transaction")
    public ResponseEntity<?> PaymentOffWaya(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter) {
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
    @ApiOperation(value = "To List All Transaction activities", notes = "To List all Transaction activities", tags = {
            "ADMIN" })
    @GetMapping("/all/transaction")
    public ResponseEntity<?> allTransactions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate) {
        ApiResponse<?> res;
        try {
            res = transAccountService.getAllTransactions(page, size, filter, fromdate, todate);
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
    public ResponseEntity<?> allTransactions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate,
            @PathVariable("accountNo") String accountNo) {
        ApiResponse<?> res;
        try {
            res = transAccountService.getAllTransactionsByAccountNo(page, size, filter, fromdate, todate, accountNo);
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
        ApiResponse<?> res;
        try {
            res = transAccountService.OfficialAccountReports(page, size, fromdate, todate, filter);
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
            "ADMIN" })
    @PostMapping("/transfer/bulk-transaction")
    public ResponseEntity<?> createBulkTrans(HttpServletRequest request,
            @Valid @RequestBody BulkTransactionCreationDTO userList) {
        ResponseEntity<?> res = transAccountService.createBulkTransaction(request, userList);
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
    @PostMapping(path = "/transfer/bulk-transaction-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBulkTransExcel(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
        ResponseEntity<?> res = transAccountService.createBulkExcelTrans(request, file);
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

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = {
            "ADMIN" })
    @GetMapping("/transaction/get-official-debit-credit-count")
    public ResponseEntity<?> getUserCount() {
        return transAccountService.debitAndCreditTransactionAmountOfficial();
    }

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = {
            "ADMIN" })
    @GetMapping("/transaction/get-official-credit-count")
    public ResponseEntity<?> getCreditTransactionAmountOfficial() {
        return transAccountService.creditTransactionAmountOffical();
    }

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = {
            "ADMIN" })
    @GetMapping("/transaction/get-official-debit-transaction-count")
    public ResponseEntity<?> getDebitTransactionAmountOfficial() {
        return transAccountService.debitTransactionAmountOffical();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Transaction Reversal", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
    @PostMapping("/transaction/reverse")
    public ResponseEntity<?> PaymentReversal(HttpServletRequest request,
            @RequestBody() ReverseTransactionDTO reverseDto) throws ParseException {
        return coreBankingService.processTransactionReversal(reverseDto, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "get official account by event id  ", notes = "get official account by event id", tags = {
            "ADMIN" })
    @GetMapping("/offical-account/{eventID}")
    public ResponseEntity<?> getSingleAccountByEventID(@PathVariable("eventID") String eventId) {
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
        return userAccountService.getAccountSimulated(user_id);
    }

    @ApiOperation(value = "List all simulated accounts", tags = { "ADMIN" })
    @GetMapping(path = "/simulated/account")
    public ResponseEntity<?> ListAllSimulatedAccount() {
        return userAccountService.getListSimulatedAccount();
    }

    @ApiOperation(value = "Create a Simulated User", tags = { "ADMIN" })
    @PostMapping(path = "simulated/account")
    public ResponseEntity<?> createSIMUser(@Valid @RequestBody AccountPojo2 user) {
        log.info("Request input: {}", user);
        return userAccountService.createAccount(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a wallet account", tags = { "ADMIN" })
    @PostMapping(path = "/official/user/account")
    public ResponseEntity<?> createUserAccount(@Valid @RequestBody AccountPojo2 accountPojo) {
        return userAccountService.createAccount(accountPojo);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a waya official account", tags = { "ADMIN" })
    @PostMapping(path = "/official/waya/account")
    public ResponseEntity<?> createOfficialAccount(@Valid @RequestBody OfficialAccountDTO account) {
        return userAccountService.createOfficialAccount(account);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a waya official account", tags = { "ADMIN" })
    @PostMapping(path = "/official/waya/account-multiple")
    public ArrayList<Object> createOfficialAccount(@Valid @RequestBody List<OfficialAccountDTO> account) {
        return userAccountService.createOfficialAccount(account);
    }

    @ApiOperation(value = "List all waya official accounts", tags = { "ADMIN" })
    @GetMapping(path = "/waya/official/account")
    public ResponseEntity<?> ListAllWayaAccount() {
        return userAccountService.getListWayaAccount();
    }

    @ApiOperation(value = "Delete,Pause and Block User Account", tags = { "ADMIN" })
    @PostMapping(path = "/user/account/access")
    public ResponseEntity<?> postAccountRestriction(@Valid @RequestBody AdminAccountRestrictionDTO user) {
        log.info("Request input: {}", user);
        return userAccountService.UserAccountAccess(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Delete User Account", tags = { "ADMIN" })
    @PostMapping(path = "/user/account/delete")
    public ResponseEntity<?> postAccountUser(@Valid @RequestBody UserAccountDelete user) {
        log.info("Request input: {}", user);
        return userAccountService.AccountAccessDelete(user);
    }

    @ApiOperation(value = "Pause Account / Freeze Account", tags = { "ADMIN" })
    @PostMapping(path = "/account/pause")
    public ResponseEntity<?> postAccountPause(@Valid @RequestBody AccountFreezeDTO user) {
        log.info("Request input: {}", user);
        return userAccountService.AccountAccessPause(user);
    }

    @ApiOperation(value = " Block / UnBlock", tags = { "ADMIN" })
    @PostMapping(path = "/account/block")
    public ResponseEntity<?> postAccountBlock(@Valid @RequestBody AccountBlockDTO user, HttpServletRequest request) {
        log.info("Request input: {}", user);
        return userAccountService.AccountAccessBlockAndUnblock(user, request);
    }

    @ApiOperation(value = "Delete Account / Block / UnBlock", tags = { "ADMIN" })
    @PostMapping(path = "/account/closure")
    public ResponseEntity<?> postAccountClosure(@Valid @RequestBody AccountCloseDTO user) {
        log.info("Request input: {}", user);
        return userAccountService.AccountAccessClosure(user);
    }

    @ApiOperation(value = "Delete Multiple Account", tags = { "ADMIN" })
    @PostMapping(path = "/account/closure-multiple")
    public ResponseEntity<?> postAccountClosureMultiple(@Valid @RequestBody List<AccountCloseDTO> user) {
        log.info("Request input: {}", user);
        return userAccountService.AccountAccessClosureMultiple(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Transaction account block / unblock", tags = { "ADMIN" })
    @PostMapping(path = "/account/lien/transaction")
    public ResponseEntity<?> postAccountLien(@Valid @RequestBody AccountLienDTO user) {
        log.info("Request input: {}", user);
        return userAccountService.AccountAccessLien(user);
    }

    @ApiOperation(value = "Create Admin Cash Wallet - (Admin COnsumption Only)", tags = { "ADMIN" })
    @PostMapping(path = "/cash/account")
    public ResponseEntity<?> createCashAccounts(@Valid @RequestBody WalletCashAccountDTO user) {
        return userAccountService.createCashAccount(user);
        // return userAccountService.createCashAccount(user);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create Event Wallet Account - (Admin COnsumption Only)", tags = { "ADMIN" })
    @PostMapping(path = "/event/account")
    public ResponseEntity<?> createEventAccounts(@Valid @RequestBody WalletEventAccountDTO user) {
        return userAccountService.createEventAccount(user);
        // return userAccountService.createEventAccount(user);
    }

    @ApiOperation(value = "Generate Account Statement", tags = { "ADMIN" })
    @GetMapping(path = "/admin/account/statement/{accountNo}")
    public ResponseEntity<?> GenerateAccountStatement(@PathVariable String accountNo) {
        // check if the accountNo passed is same with User token
        ApiResponse<?> res = userAccountService.fetchTransaction(accountNo);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "List User wallets", tags = { "ADMIN" })
    @GetMapping(path = "/admin/user/accounts/{user_id}")
    public ResponseEntity<?> GetListAccount(@PathVariable long user_id) {
        return userAccountService.ListUserAccount(user_id);
    }

    @ApiOperation(value = "Get All Wallets - (Admin Consumption Only)", tags = { "ADMIN" })
    @GetMapping(path = "/all-wallets")
    public ResponseEntity<?> getAllAccounts() {
        return userAccountService.getAllAccount();
    }

    @ApiOperation(value = "List all Commission Accounts", tags = { "ADMIN" })
    @GetMapping(path = "/commission-wallets/all")
    public ResponseEntity<?> GetAllCommissionAccounts() {
        return userAccountService.getALLCommissionAccount();
    }

    @ApiOperation(value = "Get List of Commission Accounts", tags = { "ADMIN" })
    @GetMapping(path = "/commission-wallets")
    public ResponseEntity<?> ListAllCommissionAccounts(@RequestBody List<Integer> ids) {
        return userAccountService.getListCommissionAccount(ids);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet Selected Account Detail", tags = { "ADMIN" })
    @GetMapping(path = "admin/account/{accountNo}")
    public ResponseEntity<?> GetAcctDetail(@PathVariable String accountNo) {
        return userAccountService.fetchAccountDetail(accountNo, true);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet Account Info By Account Number", tags = { "ADMIN" })
    @GetMapping(path = "admin/user-account/{accountNo}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String accountNo) throws Exception {
        return userAccountService.getAccountDetails(accountNo, true);
    }

    @ApiOperation(value = "Admin Get User Wallet Commission Account", tags = { "ADMIN" })
    @GetMapping(path = "/admin/commission-accounts/{user_id}")
    public ResponseEntity<?> getCommissionAccounts(@PathVariable long user_id) {
        return userAccountService.getUserCommissionList(user_id, true);
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

}
