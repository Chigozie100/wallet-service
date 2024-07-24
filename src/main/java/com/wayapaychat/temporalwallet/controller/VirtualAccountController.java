package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.pojo.VATransactionSearch;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountHookRequest;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import com.wayapaychat.temporalwallet.service.VirtualService;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/virtual-account")
@Tag(name = "BANK-VIRTUAL-ACCOUNT", description = "Virtual Account Service API")
@Validated
@Slf4j
public class VirtualAccountController {

    private final VirtualService virtualService;

    @Autowired
    public VirtualAccountController(VirtualService virtualService) {
        this.virtualService = virtualService;
    }

    //ROLE_API_KEY_USER
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Api-Key", value = "key", paramType = "header", required = true) })
    @ApiOperation(value = "Create a Virtual Account", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @PostMapping(
            value = "/createVirtualAccount",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_API_KEY_USER')")
    public CompletableFuture<ResponseEntity<SuccessResponse>> createVirtualAccount(@RequestBody VirtualAccountRequest accountRequest){
        log.info("Endpoint to create Virtual Account called !!! ---->> {}", accountRequest);
        return CompletableFuture.completedFuture(virtualService.createVirtualAccountVersion2(accountRequest));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Virtual Account Transaction Search", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @GetMapping(
            value = "/virtualaccount/transactions",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<SuccessResponse>> transactions(
       @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
       @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate,
       @RequestParam String  accountNo,
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size
    ){
        log.info("Endpoint to create Virtual Account called !!! ---->> {}", fromdate);
        return CompletableFuture.completedFuture(virtualService.searchVirtualTransactions(fromdate,todate, accountNo, page, size));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Register Aggregator WebHook", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @PostMapping(
            value = "/registerWebhookUrl",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> registerWebhookUrl(@RequestBody VirtualAccountHookRequest accountRequest){
        log.info("Endpoint to get register web hook URL called !!! ---->> {}", accountRequest);
        return CompletableFuture.completedFuture(virtualService.registerWebhookUrl(accountRequest));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a Virtual Account", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @GetMapping(
            value = "accountTransactionQuery",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public CompletableFuture<SuccessResponse> accountTransactionQuery(
            @RequestParam(defaultValue = "2020076821") String accountNumber,
            @RequestParam(defaultValue = "20200724") LocalDate startDate,
            @RequestParam(defaultValue = "20200724") LocalDate endDate) {
        log.info("Endpoint to get account Transaction Query called !!! ---->> {}", accountNumber);
        return CompletableFuture.completedFuture(virtualService.accountTransactionQuery(accountNumber,startDate,endDate));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Account Balance", tags = { "BANK-VIRTUAL-ACCOUNT" })
    @GetMapping(path = "/accountBalance/{accountNo}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public CompletableFuture<SuccessResponse> balanceEnquiry(@PathVariable String accountNo) {
        log.info("Endpoint balance Enquiry called !!! ---->> {}", accountNo);
        return CompletableFuture.completedFuture(virtualService.balanceEnquiry(accountNo));
    }


}
