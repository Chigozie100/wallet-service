package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.pojo.VirtualAccountHookRequest;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import com.wayapaychat.temporalwallet.service.VirtualService;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/bank-account")
@Tag(name = "BANK-VIRTUAL-ACCOUNT", description = "Virtual Account Service API")
@Validated
@Slf4j
public class VirtualAccountController {

    private final VirtualService virtualService;
    private final UserAccountService userAccountService;

    @Autowired
    public VirtualAccountController(VirtualService virtualService, UserAccountService userAccountService) {
        this.virtualService = virtualService;
        this.userAccountService = userAccountService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a Virtual Account", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @PostMapping(
            value = "/createVirtualAccount",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<ResponseEntity<SuccessResponse>> createVirtualAccount(@RequestBody VirtualAccountRequest accountRequest){

        return CompletableFuture.completedFuture(virtualService.createVirtualAccount(accountRequest));
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
    public CompletableFuture<ResponseEntity<?>> registerWebhookUrl(@RequestBody VirtualAccountHookRequest accountRequest){

        return CompletableFuture.completedFuture(virtualService.registerWebhookUrl(accountRequest));
    }


    //ResponseEntity<?> registerWebhookUrl(VirtualAccountHookRequest request)

//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
//    @ApiOperation(value = "Create a Virtual Account", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
//    @GetMapping(
//            value = "accountTransactionQuery",
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            consumes = MediaType.APPLICATION_JSON_VALUE
//    )
//    @Async
//    public CompletableFuture<SuccessResponse> accountTransactionQuery(
//            @RequestParam(defaultValue = "2020076821") String accountNumber,
//            @RequestParam(defaultValue = "20200724") LocalDate startDate,
//            @RequestParam(defaultValue = "20200724") LocalDate endDate) {
//        return CompletableFuture.completedFuture(virtualService.accountTransactionQuery(accountNumber,startDate,endDate));
//    }

    @ApiOperation(value = "Generate Account Statement by tran Date", tags = { "BANK-VIRTUAL-ACCOUNT" })
    @GetMapping(path = "/account/statement/{accountNo}")
    public ResponseEntity<?> FilterAccountStatement(@PathVariable String accountNo,
                                                    @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromdate,
                                                    @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date todate) {
        ApiResponse<?> res = userAccountService.fetchFilterTransaction(accountNo,fromdate,todate);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }







}
