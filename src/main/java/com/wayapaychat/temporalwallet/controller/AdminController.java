package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.CoreBankingService;
import com.wayapaychat.temporalwallet.service.TransAccountService;
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

    @Autowired
    public AdminController(TransAccountService transAccountService, CoreBankingService coreBankingService) {
        this.transAccountService = transAccountService;
        this.coreBankingService = coreBankingService;
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
    public ResponseEntity<?> OfficialSendMoney(@Valid @RequestBody OfficeTransferDTO transfer) {

        TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
        BeanUtils.copyProperties(transfer, transactionDTO);
        transactionDTO.setBenefAccountNumber(transfer.getOfficeCreditAccount());
        transactionDTO.setDebitAccountNumber(transfer.getOfficeDebitAccount());
        transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());

        try{
            log.info("Send Money: {}", transfer);
            return coreBankingService.transfer(transactionDTO, "WAYAOFFTOOFF");

        }catch (CustomException ex){
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To transfer money from one waya official account to user wallet", notes = "Post Money", tags = {
            "ADMIN" })
    @PostMapping("/official/user/transfer")
    public ResponseEntity<?> OfficialUserMoneyEventID(@Valid @RequestBody OfficeUserTransferDTO transfer) {

        TransferTransactionDTO transactionDTO = new TransferTransactionDTO();
        BeanUtils.copyProperties(transfer, transactionDTO);  
        transactionDTO.setBenefAccountNumber(transfer.getCustomerCreditAccount());
        transactionDTO.setDebitAccountNumber(transfer.getOfficeDebitAccount());
        transactionDTO.setTransactionCategory(TransactionTypeEnum.TRANSFER.name());

        try{
            log.info("Send Money: {}", transfer);
            return coreBankingService.transfer(transactionDTO, "WAYAOFFTOCUS");

        }catch (CustomException ex){
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
    @ApiOperation(value = "Admin Send Money From Official Account to commercial bank", notes = "Post Money", tags = { "ADMIN" })
    @PostMapping("/Official/fund/bank/account")
    public ResponseEntity<?> officialFundBank(HttpServletRequest request, @Valid @RequestBody BankPaymentOfficialDTO transfer) {

        return transAccountService.BankTransferPaymentOfficial(request, transfer);

    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin Send Money From Official Account to commercial bank", notes = "Post Money", tags = { "ADMIN" })
    @PostMapping("/Official/fund/bank/mutilple-account")
    public ResponseEntity<?> officialFundBankMultiple(HttpServletRequest request, @Valid @RequestBody List<BankPaymentOfficialDTO> transfer) {

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
    public ResponseEntity<?> EventOfficePayment(HttpServletRequest request, @RequestBody() EventOfficePaymentDTO walletDto) {

        return transAccountService.EventOfficePayment(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Office Event: Temporal - Official Transfer Multiple", notes = "Transfer amount from Temporal wallet to Official wallet mutiliple transaction", tags = {
            "ADMIN" })
    @PostMapping("/event/office/temporal-to-official-multiple")
    public ResponseEntity<?> TemporalToOfficialWalletDTO(HttpServletRequest request, @RequestBody() List<TemporalToOfficialWalletDTO> walletDto) {
        return transAccountService.TemporalWalletToOfficialWalletMutiple(request, walletDto);
    }

    
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Office Event: Temporal - Official Transfer Multiple", notes = "Transfer amount from Temporal wallet to Official wallet mutiliple transaction", tags = {
            "ADMIN" })
    @PostMapping("/event/office/temporal-to-official")
    public ResponseEntity<?> TemporalToOfficialWallet(HttpServletRequest request, @RequestBody() TemporalToOfficialWalletDTO walletDto) {
        return transAccountService.TemporalWalletToOfficialWallet(request, walletDto);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Admin send Non-Waya Payment with excel upload on behalf of users", notes = "Admin send Non-Waya Payment with excel upload on behalf of users", tags = {
            "ADMIN" })
    @PostMapping(path = "/non-waya/payment/new-multiple-excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> NonWayaPaymentMultipleUpload(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
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

    @ApiOperation(value = "To Fetch Official Transaction activities", notes = "Transfer amount from one wallet to another wallet", tags = {
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

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To List Official Transaction activities", notes = "Transfer amount from one wallet to another wallet", tags = {
            "ADMIN" })
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

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = { "ADMIN" })
    @GetMapping("/transaction/get-official-debit-credit-count")
    public ResponseEntity<?> getUserCount() {
        return transAccountService.debitAndCreditTransactionAmountOfficial();
    }

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = { "ADMIN" })
    @GetMapping("/transaction/get-official-credit-count")
    public ResponseEntity<?> getCreditTransactionAmountOfficial() {
        return transAccountService.creditTransactionAmountOffical();
    }

    @ApiOperation(value = "All Official Transaction Count ", notes = "All Official Transaction Count ", tags = { "ADMIN" })
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


}
