package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.entity.UserPricing;
import com.wayapaychat.temporalwallet.enumm.PriceCategory;
import com.wayapaychat.temporalwallet.service.UserPricingService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> syncUsersWithProducts(@PathVariable String apiKey) {
        return userPricingService.syncWalletUser(apiKey);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update Custom Price", notes = "Update Custom Pricing", tags = { "USER-PRICING" })
    @PutMapping(value = "/custom-pricing", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> updateCustomPrice(@RequestParam("userId") Long  userId, @RequestParam("discountAmount") BigDecimal discountAmount, @RequestParam("customAmount") BigDecimal customAmount, @RequestParam("capAmount") BigDecimal capAmount , @RequestParam("product") String product, @RequestParam(defaultValue = "FIXED") PriceCategory priceType) {

        return CompletableFuture.completedFuture(userPricingService.update(userId, discountAmount, customAmount, capAmount, product, priceType));
    }


//     @ApiImplicitParams({
//             @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
//     @ApiOperation(value = "Update Custom Price", notes = "Update Custom Pricing", tags = { "USER-PRICING" })
//     @PutMapping(value = "/custom-pricing-product", produces =
//             MediaType.APPLICATION_JSON_VALUE)
//     @Async
//     @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
//     public CompletableFuture<ResponseEntity<?>> updateCustomProductPrice(@RequestParam("capAmount") BigDecimal  capAmount, @RequestParam("discountAmount") BigDecimal discountAmount, @RequestParam("customAmount") BigDecimal customAmount, @RequestParam("product") String product, @RequestParam(defaultValue = "FIXED") PriceCategory priceType) {

//         return CompletableFuture.completedFuture(userPricingService.updateCustomProduct(capAmount, discountAmount,customAmount, product, priceType));
//     }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get All Users with Products Pricing", notes = "Products Pricing List", tags = { "USER-PRICING" })
    @GetMapping(value = "/user-products", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> getUsersWithProductsListByUserId(@PathVariable String userId, @PathVariable String product) {
        return CompletableFuture.completedFuture(userPricingService.getAllUserPricingUserId(userId,product));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "apply-discountPricing", notes = "apply-discount List", tags = { "USER-PRICING" })
    @PostMapping(value = "/apply-discount", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> applyDiscountToAll(@RequestParam("discountAmount") BigDecimal discountAmount) {

        return CompletableFuture.completedFuture(userPricingService.applyDiscountToAll(discountAmount));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "apply-cap-pricePricing", notes = "apply-cap-price List", tags = { "USER-PRICING" })
    @PostMapping(value = "/apply-cap-price", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> applyCapToAll(@RequestParam("capAmount") BigDecimal capAmount) {

        return CompletableFuture.completedFuture(userPricingService.applyCapToAll(capAmount));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "apply-cap-pricePricing", notes = "apply-cap-price List", tags = { "USER-PRICING" })
    @PostMapping(value = "/apply-general-price", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> applyGeneralToAll(@RequestParam("generalPrice") BigDecimal generalPrice, @RequestParam("productType") String productType, @RequestParam("capAmount") BigDecimal capAmount, @RequestParam(defaultValue = "FIXED") PriceCategory priceType) {

        return CompletableFuture.completedFuture(userPricingService.applyGeneralToAll(generalPrice,productType, capAmount, priceType));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "delete-all", notes = "delete-all", tags = { "USER-PRICING" })
    @DeleteMapping(value = "/delete-all/{apiKey}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> deleteUserPricing(@PathVariable String apiKey) {
        return CompletableFuture.completedFuture(userPricingService.deleteAll(apiKey));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "creat-user-pricing", notes = "creat-user-pricing", tags = { "USER-PRICING" })
    @PostMapping(value = "/creat-user-pricing", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Async
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public CompletableFuture<ResponseEntity<?>> create(@PathVariable String userId) {
        return CompletableFuture.completedFuture(userPricingService.createUserPricing(userId));
    }


// @ApiImplicitParams({
//         @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
// @ApiOperation(value = "distinctSearch", notes = "distinctSearch", tags = { "USER-PRICING" })
// @GetMapping(value = "/distinctSearch", produces =
//         MediaType.APPLICATION_JSON_VALUE)
// @Async
// @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
// public CompletableFuture<ResponseEntity<List<UserPricing>>> distinctSearch() {
//     return CompletableFuture.completedFuture(userPricingService.distinctSearch());
// }

@ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
@ApiOperation(value = "create-products", notes = "delete-all", tags = { "USER-PRICING" })
@PostMapping(value = "/products", produces =
        MediaType.APPLICATION_JSON_VALUE)
@Async
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public CompletableFuture<ResponseEntity<?>> createProducts(@RequestParam("name") String name, @RequestParam("description") String description,@RequestParam("eventId") String eventId) {
    return CompletableFuture.completedFuture(userPricingService.createProducts(name,description,eventId));
}

@ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
@ApiOperation(value = "update-products", notes = "update-products", tags = { "USER-PRICING" })
@PutMapping(value = "/products/{id}", produces =
        MediaType.APPLICATION_JSON_VALUE)
@Async
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public CompletableFuture<ResponseEntity<?>> editProducts(@PathVariable("id") Long id, @RequestParam("name") String name, @RequestParam("description") String description,@RequestParam("eventId") String eventId) {
    return CompletableFuture.completedFuture(userPricingService.editProducts(id,name,description,eventId));
}

@ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
@ApiOperation(value = "get-products", notes = "get-products", tags = { "USER-PRICING" })
@GetMapping(value = "/products", produces =
        MediaType.APPLICATION_JSON_VALUE)
@Async
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public CompletableFuture<ResponseEntity<?>> getAllProducts() {
    return CompletableFuture.completedFuture(userPricingService.getAllProducts());
}

@ApiImplicitParams({
        @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
@ApiOperation(value = "filter by user name", notes = "search", tags = { "USER-PRICING" })
@GetMapping(value = "/products/search/{value}", produces =
        MediaType.APPLICATION_JSON_VALUE)
@Async
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public CompletableFuture<ResponseEntity<?>> getAllProducts(@PathVariable("value") String value) {
    return CompletableFuture.completedFuture(userPricingService.search(value));
}
 

}
