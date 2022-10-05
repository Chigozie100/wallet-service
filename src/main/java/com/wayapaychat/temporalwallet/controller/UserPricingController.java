package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.service.UserPricingService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/v1/pricing")
@Tag(name = "USER-PRICING", description = "Pricing Wallet Service API")
@Validated
public class UserPricingController {

    private final UserPricingService userPricingService;

    @Autowired
    public UserPricingController(UserPricingService userPricingService) {
        this.userPricingService = userPricingService;
    }

    @ApiOperation(value = "Create User Custom Price", notes = "Custom Pricing", tags = { "USER-PRICING" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PostMapping("/custom-price")
    public ResponseEntity<?> userPricing(@RequestParam("userId") Long  userId, @RequestParam("amount") BigDecimal amount, @RequestParam("product") String product) {
        return userPricingService.create(userId,amount, product);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Sync User with Products", notes = "Custom Pricing", tags = { "USER-PRICING" })
    @GetMapping(value = "/sync-user-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> syncUsersWithProducts() {
        return userPricingService.syncWalletUser();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update Custom Price", notes = "Update Custom Pricing", tags = { "USER-PRICING" })
    @PutMapping(value = "/custom-pricing", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> updateCustomPrice(@RequestParam("userId") Long  userId, @RequestParam("discountAmount") BigDecimal discountAmount, @RequestParam("customAmount") BigDecimal customAmount, @RequestParam("capAmount") BigDecimal capAmount , @RequestParam("product") String product) {

        return CompletableFuture.completedFuture(userPricingService.update(userId, discountAmount, customAmount, capAmount, product));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get All Users with Products Pricing", notes = "Products Pricing List", tags = { "USER-PRICING" })
    @GetMapping(value = "/user-products", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> getUsersWithProductsList(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size) {

        return CompletableFuture.completedFuture(userPricingService.getAllUserPricing(page,size));
    }


}
