package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.dto.BillerManagementResponse;
import com.wayapaychat.temporalwallet.exception.CustomException;
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
import java.util.List;
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

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Sync User with Products", notes = "Custom Pricing", tags = { "USER-PRICING" })
    @GetMapping(value = "/sync-user-products/{apiKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> syncUsersWithProducts(@PathVariable String apiKey) {
        return userPricingService.syncWalletUser(apiKey);
    }

//    @GetMapping("/sync-billers/{apiKey}")
//    @Async
//    public CompletableFuture<ResponseEntity<List<BillerManagementResponse>>> syncBillers(@PathVariable String apiKey) throws CustomException {
//        return CompletableFuture.completedFuture(userPricingService.syncBillers(apiKey));
//    }

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
    @ApiOperation(value = "Update Custom Price", notes = "Update Custom Pricing", tags = { "USER-PRICING" })
    @PutMapping(value = "/custom-pricing-product", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> updateCustomProductPrice(@RequestParam("capAmount") BigDecimal  capAmount, @RequestParam("discountAmount") BigDecimal discountAmount, @RequestParam("customAmount") BigDecimal customAmount, @RequestParam("product") String product) {

        return CompletableFuture.completedFuture(userPricingService.updateCustomProduct(capAmount, discountAmount,customAmount, product));
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

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get All Users with Products Pricing By User ID", notes = "Products Pricing List By User ID", tags = { "USER-PRICING" })
    @GetMapping(value = "/user-products/{userId}/{product}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> getUsersWithProductsListByUserId(@PathVariable String userId, @PathVariable String product) {
        return CompletableFuture.completedFuture(userPricingService.getAllUserPricingUserId(userId,product));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "apply-discountPricing", notes = "apply-discount List", tags = { "USER-PRICING" })
    @PostMapping(value = "/apply-discount", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> applyDiscountToAll(@RequestParam("discountAmount") BigDecimal discountAmount) {

        return CompletableFuture.completedFuture(userPricingService.applyDiscountToAll(discountAmount));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "apply-cap-pricePricing", notes = "apply-cap-price List", tags = { "USER-PRICING" })
    @PostMapping(value = "/apply-cap-price", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> applyCapToAll(@RequestParam("capAmount") BigDecimal capAmount) {

        return CompletableFuture.completedFuture(userPricingService.applyCapToAll(capAmount));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "apply-cap-pricePricing", notes = "apply-cap-price List", tags = { "USER-PRICING" })
    @PostMapping(value = "/apply-general-price", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> applyGeneralToAll(@RequestParam("capAmount") BigDecimal capAmount) {

        return CompletableFuture.completedFuture(userPricingService.applyGeneralToAll(capAmount));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "delete-all", notes = "delete-all", tags = { "USER-PRICING" })
    @DeleteMapping(value = "/delete-all/{apiKey}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    public CompletableFuture<ResponseEntity<?>> deleteUserPricing(@PathVariable String apiKey) {
        return CompletableFuture.completedFuture(userPricingService.deleteAll(apiKey));
    }


}
