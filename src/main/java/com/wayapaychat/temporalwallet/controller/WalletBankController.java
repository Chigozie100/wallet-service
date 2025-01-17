package com.wayapaychat.temporalwallet.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.wayapaychat.temporalwallet.service.ConfigService;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/bank")
@Tag(name = "BANK-WALLET", description = "Bank Wallet Service API")
@Validated
@Slf4j
public class WalletBankController {
	
	@Autowired
    ConfigService configService;

    @Autowired
    UserAccountService userAccountService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create a Wallet Default Special Code", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/code")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> creteDefaultCode(@Valid @RequestBody WalletConfigDTO configPojo) {
        log.info("Endpoint to crete Default Code called !!! ---->> {}", configPojo);
        return configService.createDefaultCode(configPojo);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get List of Wallet Default Special Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getDefaultCode() {
        log.info("Endpoint to get Default Code called !!!");
        return configService.getListDefaultCode();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet CodeValues using codeValueId", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codeValue/{codeValueId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getCodeValue(@PathVariable("codeValueId") Long codeValueId) {
        log.info("Endpoint to get Code value with code value name called !!! --->> {}", codeValueId);
        return configService.getListCodeValue(codeValueId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet CodeValues using codename", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codeValue/{codeName}/command")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> FetchCodeValue(@PathVariable("codeName") String codeName) {
        log.info("Endpoint to get Code value with code name called !!! --->> {}", codeName);
        return configService.getAllCodeValue(codeName);
    }
    
    @ApiOperation(value = "Get Wallet CodeValues using codeId", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codes/{codeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getCode(@PathVariable("codeId") Long codeId) {
        log.info("Endpoint to get Code called !!! --->> {}", codeId);
        return configService.getCode(codeId);
    }
    
    @ApiOperation(value = "Create a Wallet Product Code", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/product")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> creteProductCode(@Valid @RequestBody ProductCodeDTO product) {
        log.info("Endpoint to create product code called !!! --->> {}", product);
        return configService.createProduct(product);
    }
    
    @ApiOperation(value = "Get Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product/{productId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getProduct(@PathVariable("productId") Long productId) {
        log.info("Endpoint to get product called !!! --->> {}", productId);
        return configService.findProduct(productId);
    }
    
    @ApiOperation(value = "Get Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product/code/{productCode}/{glcode}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getProduct(@PathVariable("productCode") String productCode,
    		@PathVariable("glcode") String gl) {
        log.info("Endpoint to get product called !!! --->> {}", productCode);
        return configService.getProduct(productCode,gl);
    }
    
    @ApiOperation(value = "List Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getListProductCode() {
        log.info("Endpoint to get list of product code called !!!");
        return configService.ListProductCode();
    }
    
    @ApiOperation(value = "List Account Products", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product/account")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> ListProductAccount() {
        log.info("Endpoint to get list of product accounts called !!!");
        return configService.ListAccountProductCode();
    }
    
    @ApiOperation(value = "Create a Wallet Product Code", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/product/parameter")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> createProductParameter(@Valid @RequestBody ProductDTO product) {
        log.info("Endpoint to create product parameter called !!! --->> {}", product);
        return configService.createProductParameter(product);
    }
    
    @ApiOperation(value = "Create a Wallet Interest Slab", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/interest/parameter")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> createInterestParameter(@Valid @RequestBody InterestDTO interest) {
        log.info("Endpoint to create interest parameter called !!! --->> {}", interest);
        return configService.createInterestParameter(interest);
    }
    
    @ApiOperation(value = "Create a Wallet Account Chart", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/gl/coa")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> createCOA(@Valid @RequestBody AccountGLDTO chat) {
        log.info("Endpoint to create COA called !!! --->> {}", chat);
        return configService.createParamCOA(chat);
    }
    
    @ApiOperation(value = "Create a Wallet Teller", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/teller/till")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> createTeller(HttpServletRequest request, @Valid @RequestBody WalletTellerDTO tellerPojo) {
        log.info("Endpoint to create teller called !!! --->> {}", tellerPojo);
        return configService.createdTeller(request,tellerPojo);
    }
    
    @ApiOperation(value = "List Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/teller/till")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getListTellersTill() {
        log.info("Endpoint to get list tellers till called !!!");
        return configService.ListTellersTill();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a Wallet Event", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/event")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> createEventCharge(@Valid @RequestBody EventChargeDTO eventPojo) {
        log.info("Endpoint to create Event charge called !!! --->> {}", eventPojo);
        return configService.createdEvents(eventPojo);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update a Wallet Event", tags = { "BANK-WALLET" })
    @PutMapping(path = "/update/event/{eventId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> updateEventCharge(@Valid @RequestBody UpdateEventChargeDTO eventPojo, @PathVariable("eventId") Long eventId) {
        log.info("Endpoint to update Event charge called !!! --->> {}", eventPojo);
        return configService.updateEvents(eventPojo,eventId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update a Wallet Event", tags = { "BANK-WALLET" })
    @DeleteMapping(path = "/update/event/{eventId}/delete")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> deleteEventCharge(@PathVariable("eventId") Long eventId) {
        log.info("Endpoint to delete Event charge called !!! --->> {}", eventId);
        return configService.deleteEvent(eventId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "List Wallet Event", tags = { "BANK-WALLET" })
    @GetMapping(path = "/event/charges")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getListEventChrg() {
        log.info("Endpoint to list Event charge called !!!");
        return configService.ListEvents();
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Single Wallet Event", tags = { "BANK-WALLET" })
    @GetMapping(path = "/event/charges/{chargeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> getSingleEventCharge(@PathVariable("chargeId") Long chargeId) {
        log.info("Endpoint to get single Event charge called !!! --->> {}", chargeId);
        return configService.getSingleEvents(chargeId);
    }



    @ApiOperation(value = "Create a Transaction Charge", tags = { "BANK-WALLET" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PostMapping(path = "/create/transaction/charge")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> createTransactionCharge(@Valid @RequestBody ChargeDTO charge) {
        log.info("Endpoint to create transaction charge called !!! --->> {}", charge);
        return configService.createCharge(charge);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "AUTO CREATE ACCOUNT", tags = { "BANK-WALLET" })
    @PostMapping(path = "/auto-create/account")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
    public ResponseEntity<?> AutoCreateTransAccount(@RequestBody AutoCreateAccount request) {
        log.info("Endpoint to auto create transaction account called !!! --->> {}", request);
        ResponseEntity<?> responseEntity = configService.AutoCreateTransAccount(request);
        if (responseEntity.getStatusCode().is2xxSuccessful()){

            WalletEventAccountDTO walletEventAccountDTO = new WalletEventAccountDTO();
            walletEventAccountDTO.setAccountType("SAVINGS");
            walletEventAccountDTO.setAccountName(request.getTranNarration());
            walletEventAccountDTO.setCrncyCode(request.getCrncyCode());
            walletEventAccountDTO.setDescription(request.getTranNarration());
            walletEventAccountDTO.setEventId(request.getEventId());
            walletEventAccountDTO.setPlaceholderCode(request.getCodeValue());
            walletEventAccountDTO.setProductCode("OABAS");
            walletEventAccountDTO.setProductGL("11104");

            System.out.println(" Request Body::: " + walletEventAccountDTO);

            ResponseEntity<?> responseEntity1 = userAccountService.createEventAccount(walletEventAccountDTO);
            log.info(" ######### FINISH CREATING createEventAccount::: " + responseEntity1);
        }
        return responseEntity;
    }

}
