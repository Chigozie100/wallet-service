package com.wayapaychat.temporalwallet.controller;

import java.math.BigDecimal;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "USER-ACCOUNT-WALLET", description = "User Account Wallet Service API")
@Validated
@Slf4j
public class WalletUserAccountController {

    @Autowired
    UserAccountService userAccountService;

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Create a User", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping(path = "/create-user")
    public ResponseEntity<?> createUser(HttpServletRequest request,@Valid @RequestBody UserDTO user, @RequestHeader("Authorization") String token) {
        log.info("Request input: {}", user);
        return userAccountService.createUser(request,user, token);
    }

    //Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Create User Account", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping(path = "/user/account")
    public ResponseEntity<?> createUserAccount(HttpServletRequest request,@Valid @RequestBody WalletUserDTO user, @RequestHeader("Authorization") String token) {
        log.info("Request input: {}", user); //, String token
        return userAccountService.createUserAccount(request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE),user, token);
    }

    @ApiOperation(value = "Modify User Account", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping(path = "/user/account/modify")
    public ResponseEntity<?> createUserAccount(HttpServletRequest request,@Valid @RequestBody UserAccountDTO user) {
        log.info("Request input: {}", user);
        return userAccountService.modifyUserAccount(request,user);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "createExternal CBA Account e.g. MIFOS", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/user/externalcba-account/{accountNumber}")
    public ResponseEntity<?> createAccountOnMIFOS(@PathVariable("accountNumber") String accountNumber) {
        log.info("Request input: {}", accountNumber);
        return userAccountService.createExternalAccount(accountNumber);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "createAccountOnMIFOSAuto", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping(path = "/user/mifos-account/auto")
    public ResponseEntity<?> createNubbanAccountAuto() {
        return userAccountService.createNubbanAccountAuto();
    }

    //ResponseEntity<?> createNubbanAccountAuto()
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Account Toggle", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping(path = "/user/account/toggle")
    public ResponseEntity<?> createAccountToggle(HttpServletRequest request,@Valid @RequestBody AccountToggleDTO user) {
        log.info("Request input: {}", user);
        return userAccountService.ToggleAccount(request,user);
        //return userAccountService.modifyUserAccount(user);
    }

    //Wallet call by other service
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Create a Wallet | add additional wallet", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping(path = "/create-wallet")
    public ResponseEntity<?> createAccount(HttpServletRequest request, @Valid @RequestBody AccountPojo2 accountPojo, @RequestHeader("Authorization") String token) {
        return userAccountService.createAccount(request,accountPojo, token);
    }

    @ApiOperation(value = "Create a Wallet", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping(path = "/account/product")
    public ResponseEntity<?> createProductAccount(HttpServletRequest request,@Valid @RequestBody AccountProductDTO accountPojo) {
        return userAccountService.createAccountProduct(request,accountPojo);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Find wallet by walletId", notes = "Find user wallet by walletId", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping("/find/customer/{walletId}")
    public ResponseEntity<?> findCustomerById(@PathVariable("walletId") Long walletId) {
        ApiResponse<?> res = userAccountService.findCustWalletById(walletId);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Find wallet by walletId", notes = "Find user wallet by walletId", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping("/find/account/{walletId}")
    public ResponseEntity<?> findAccountById(@PathVariable("walletId") Long walletId) {
        ApiResponse<?> res = userAccountService.findAcctWalletById(walletId);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "Account LookUp", notes = "Find Virtual Account", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping("/account/lookup/{accountNo}")
    public ResponseEntity<?> AccountLook(@PathVariable("accountNo") String accountNo,
            @Valid @RequestBody SecureDTO key) {
        return userAccountService.AccountLookUp(accountNo, key);
    }

    @ApiOperation(value = "Get Wallet Account Info", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/info/{accountNo}")
    public ResponseEntity<?> getAcctInfo(@PathVariable String accountNo) {
        return userAccountService.getAccountInfo(accountNo);
    }

    @ApiOperation(value = "This method returns the User Object", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/info/user-details/{accountNo}")
    public ResponseEntity<?> getAcctInfoWithUserInfo(@PathVariable String accountNo) {
        return userAccountService.getAccountInfoWithUserInfo(accountNo);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Get Wallet Selected Account Detail", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/account/{accountNo}")
    public ResponseEntity<?> GetAcctDetail(@PathVariable String accountNo) {
        return userAccountService.fetchAccountDetail(accountNo, false);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Get User Info By Account Number", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/fetchUserByAccountNo/{accountNo}")
    public ResponseEntity<?> fetchUserByAccountNo(@PathVariable String accountNo) {
        return userAccountService.fetchUserByAccountNo(accountNo);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Do Name Enquiry", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/name-enquiry/{accountNo}")
    public ResponseEntity<?> nameEquiry(@PathVariable String accountNo) {
        return userAccountService.fetchAccountDetail(accountNo, false);
    }

    @ApiOperation(value = "Get Virtual Account Detail", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/account/virtual/{accountNo}")
    public ResponseEntity<?> GetVirtualAcctDetail(@PathVariable String accountNo) {
        return userAccountService.fetchVirtualAccountDetail(accountNo);
    }

    @ApiOperation(value = "Get User list of wallets", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/accounts/{user_id}/{profileId}")
    public ResponseEntity<?> getAccounts(HttpServletRequest request,@PathVariable long user_id,
                                         @PathVariable String profileId,
                                         @RequestHeader("Authorization") String token) {
        return userAccountService.getUserAccountList(request,user_id,profileId,token);
    }

    @ApiOperation(value = "Get User Wallet Commission Account", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/commission-accounts/{user_id}/{profileId}")
    public ResponseEntity<?> getCommissionAccounts(@PathVariable long user_id,@PathVariable String profileId) {
        return userAccountService.getUserCommissionList(user_id, false,profileId);
    }

    @ApiOperation(value = "Get User Wallet Commission Detail", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/commission-account/user/{accountNo}")
    public ResponseEntity<?> setDefaultWallet(@PathVariable String accountNo) {
        return userAccountService.makeDefaultWallet(accountNo);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Get User Wallet Transaction Limit", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/user/account/{user_id}/{profileId}")
    public ResponseEntity<?> setDefaultWallet(@PathVariable Long user_id,@PathVariable String profileId) {
        return userAccountService.UserWalletLimit(user_id,profileId);
    }

    @ApiOperation(value = "Create Cooperate account, this creates a default account and a commission account", notes = "Create Cooperate account, this creates a default account and a commission account", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping("/create/cooperate/user")
    public ResponseEntity<?> createCooperateAccount(HttpServletRequest request,@RequestBody WalletUserDTO createAccountPojo, @RequestHeader("Authorization") String token) {
        return userAccountService.createUserAccount(request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE),createAccountPojo, token);
    }

    @ApiOperation(value = "Create Nuban account, this creates a default account / commission account / nuban account", notes = "Create Cooperate account, this creates a default account and a commission account", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping("/create/nuban/user")
    public ResponseEntity<?> createNubanAccount(HttpServletRequest request,@RequestBody WalletUserDTO createAccountPojo, @RequestHeader("Authorization") String token) {
        return userAccountService.createUserAccount(request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE),createAccountPojo, token);
    }

    @ApiOperation(value = "Get Wallet Account Info", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/commission/{accountNo}")
    public ResponseEntity<?> getAcctCommission(@PathVariable String accountNo) {
        return userAccountService.getAccountCommission(accountNo);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Get Wallet Account Info By Account Number", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/user-account/{accountNo}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String accountNo) throws Exception {
        return userAccountService.getAccountDetails(accountNo, false);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Get Wallet Default Account", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/default/{user_id}/{profileId}")
    public ResponseEntity<?> getAcctDefault(@PathVariable Long user_id,@PathVariable String profileId) throws JsonProcessingException {
        return userAccountService.getAccountDefault(user_id,profileId);
    }

    @ApiOperation(value = "To Search For Account(s) with Phone or Email or WayaID", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/account/search/{item}")
    public ResponseEntity<?> ListAllAccounts(@PathVariable String item) {
        return userAccountService.searchAccount(item);
    }

    @ApiOperation(value = "Generate Account Statement by tran Date", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/account/statement/{accountNo}")
    public ResponseEntity<?> FilterAccountStatement(@PathVariable String accountNo,
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate) {

        // check if the accountNo passed is same with User token
        ApiResponse<?> res = userAccountService.fetchFilterTransaction(accountNo, fromdate, todate);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Recent Transaction Details for User Accounts", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/recent/accounts/transaction/{user_id}/{profileId}")
    public ResponseEntity<?> GenerateRecentTransaction(@PathVariable Long user_id,String profileId) {
        // check if the userI passed is same with token
        ApiResponse<?> res = userAccountService.fetchRecentTransaction(user_id,profileId);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Wallet User account count", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/account/count/{userId}/{profileId}")
    public ResponseEntity<?> TotalUserAccountCount(@PathVariable Long userId,@PathVariable String profileId) {
        return userAccountService.getUserAccountCount(userId,profileId);
    }

    @ApiOperation(value = "Total Active Accounts", notes = "Total Transaction", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping("/account/total-active-amount")
    public ResponseEntity<?> getTotalActiveAccount() {
        return userAccountService.getTotalActiveAccount();
    }

    @ApiOperation(value = "Total Active Accounts", notes = "Total Active Accounts", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping("/account/total-active-count")
    public ResponseEntity<?> countActiveAccount() {
        return userAccountService.countActiveAccount();
    }

    @ApiOperation(value = "Total InActive Accounts", notes = "Total InActive Accounts", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping("/account/total-inactive-count")
    public ResponseEntity<?> countInActiveAccount() {
        return userAccountService.countInActiveAccount();
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "updateCustomerDebitLimit", notes = "updateCustomerDebitLimit", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping("/updateCustomerDebitLimit")
    public ResponseEntity<?> updateCustomerDebitLimit(@RequestParam("userId") String userId,
                                                      @RequestParam("amount") BigDecimal amount,
                                                      @RequestParam String profileId) {
        ResponseEntity<?> res = userAccountService.updateCustomerDebitLimit(userId, amount,profileId);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "updateNotificationEmail", notes = "updateNotificationEmail", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping("/updateNotificationEmail")
    public ResponseEntity<?> updateNotificationEmail(@RequestParam("accountNumber") String accountNumber, @RequestParam("email") String email) {
        ResponseEntity<?> res = userAccountService.updateNotificationEmail(accountNumber, email);
        if (!res.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "Toggle transaction property per user")
    @PostMapping("/toggle/{userId}")
    public ApiResponse<?> toggleTransactionProperty(HttpServletRequest request,@PathVariable("userId") long userId,
            @RequestParam("type") String type, @RequestHeader("Authorization") String token) {
        return userAccountService.toggleTransactionType(request,userId, type, token);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Toggle transaction property per user")
    @GetMapping("/trans-type/status/{userId}")
    public ApiResponse<?> transactionPropertyStatus(@PathVariable("userId") long userId) {
        return userAccountService.transTypeStatus(userId);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Transaction Analysis for User", tags = {"USER-ACCOUNT-WALLET"})
    @GetMapping(path = "/analysis/transaction/{user_id}/{profileId}")
    public ResponseEntity<?> userTransactionAnalysis(@PathVariable Long user_id,
            @RequestParam(value = "filter", required = false) boolean filter,
            @RequestParam(value = "fromdate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromdate,
            @RequestParam(value = "todate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate todate,
                                                     @PathVariable String profileId) {
        // check if the userI passed is same with token
        ApiResponse<?> res = userAccountService.totalTransactionByUserId(user_id, filter, fromdate, todate,profileId);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation(value = "Createcommission account", notes = "Create Cooperate account, this creates a default account and a commission account", tags = {"USER-ACCOUNT-WALLET"})
    @PostMapping("/create/commission_account/{userId}/{profileId}")
    public ApiResponse<?> createCommissionAccount(@PathVariable("userId") long userId,
                                                  @RequestHeader("Authorization") String token,
                                                  @PathVariable String profileId) {
        return userAccountService.createCommisionAccount(userId, token,profileId);
    }

}
