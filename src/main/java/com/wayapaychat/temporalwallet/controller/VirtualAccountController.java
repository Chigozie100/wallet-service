package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.dto.AccountDetailDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a Virtual Account", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @PostMapping(
            value = "/createVirtualAccount",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_USER_OWNER', 'ROLE_USER_MERCHANT')")
    public ResponseEntity<SuccessResponse> createVirtualAccount(@RequestBody VirtualAccountRequest accountRequest){
        log.info("Endpoint to create Virtual Account called !!! ---->> {}", accountRequest);
        return virtualService.createVirtualAccountVersion2(accountRequest);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a Virtual Account", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @GetMapping(
            value = "/nameEnquiry/{accountNo}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_USER_OWNER', 'ROLE_USER_MERCHANT')")
    public ResponseEntity<?> nameEnquiry(@PathVariable("accountNo") String accountNo){
        log.info("Endpoint is for name enquiry !!! ---->> {}", accountNo);
        AccountDetailDTO response = virtualService.nameEnquiry(accountNo);
        if(response == null){
            return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Virtual Account Transaction Search", hidden = false, tags = { "BANK-VIRTUAL-ACCOUNT" })
    @GetMapping(
            value = "/transactions",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_USER_OWNER', 'ROLE_USER_MERCHANT')")
    public CompletableFuture<ResponseEntity<SuccessResponse>> transactions(
            @RequestParam("fromdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromdate,
            @RequestParam("todate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime todate,
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_USER_MERCHANT')")
    public CompletableFuture<ResponseEntity<SuccessResponse>> registerWebhookUrl(@RequestBody VirtualAccountHookRequest accountRequest){
        log.info("Endpoint to get register web hook URL called !!! ---->> {}", accountRequest);
        return CompletableFuture.completedFuture(virtualService.registerWebhookUrl(accountRequest));
    }

}