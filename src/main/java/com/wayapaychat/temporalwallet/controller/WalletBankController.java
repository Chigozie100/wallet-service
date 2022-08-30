package com.wayapaychat.temporalwallet.controller;

import javax.validation.Valid;


import com.wayapaychat.temporalwallet.dto.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create a Wallet Default Special Code", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/code")
    public ResponseEntity<?> creteDefaultCode(@Valid @RequestBody WalletConfigDTO configPojo) {
        return configService.createDefaultCode(configPojo);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get List of Wallet Default Special Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codes")
    public ResponseEntity<?> getDefaultCode() {
        return configService.getListDefaultCode();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet CodeValues using codeValueId", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codeValue/{codeValueId}")
    public ResponseEntity<?> getCodeValue(@PathVariable("codeValueId") Long codeValueId) {
        return configService.getListCodeValue(codeValueId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Wallet CodeValues using codename", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codeValue/{codeName}/command")
    public ResponseEntity<?> FetchCodeValue(@PathVariable("codeName") String codeName) {
        return configService.getAllCodeValue(codeName);
    }
    
    @ApiOperation(value = "Get Wallet CodeValues using codeId", tags = { "BANK-WALLET" })
    @GetMapping(path = "/codes/{codeId}")
    public ResponseEntity<?> getCode(@PathVariable("codeId") Long codeId) {
        return configService.getCode(codeId);
    }
    
    @ApiOperation(value = "Create a Wallet Product Code", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/product")
    public ResponseEntity<?> creteProductCode(@Valid @RequestBody ProductCodeDTO product) {
        return configService.createProduct(product);
    }
    
    @ApiOperation(value = "Get Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable("productId") Long productId) {
        return configService.findProduct(productId);
    }
    
    @ApiOperation(value = "Get Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product/code/{productCode}/{glcode}")
    public ResponseEntity<?> getProduct(@PathVariable("productCode") String productCode,
    		@PathVariable("glcode") String gl) {
        return configService.getProduct(productCode,gl);
    }
    
    @ApiOperation(value = "List Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product")
    public ResponseEntity<?> getListProductCode() {
        return configService.ListProductCode();
    }
    
    @ApiOperation(value = "List Account Products", tags = { "BANK-WALLET" })
    @GetMapping(path = "/product/account")
    public ResponseEntity<?> ListProductAccount() {
        return configService.ListAccountProductCode();
    }
    
    @ApiOperation(value = "Create a Wallet Product Code", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/product/parameter")
    public ResponseEntity<?> createProductParameter(@Valid @RequestBody ProductDTO product) {
        return configService.createProductParameter(product);
    }
    
    @ApiOperation(value = "Create a Wallet Interest Slab", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/interest/parameter")
    public ResponseEntity<?> createInterestParameter(@Valid @RequestBody InterestDTO interest) {
        return configService.createInterestParameter(interest);
    }
    
    @ApiOperation(value = "Create a Wallet Account Chart", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/gl/coa")
    public ResponseEntity<?> createCOA(@Valid @RequestBody AccountGLDTO chat) {
        return configService.createParamCOA(chat);
    }
    
    @ApiOperation(value = "Create a Wallet Teller", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/teller/till")
    public ResponseEntity<?> createTeller(@Valid @RequestBody WalletTellerDTO tellerPojo) {
        return configService.createdTeller(tellerPojo);
    }
    
    @ApiOperation(value = "List Wallet Product Code", tags = { "BANK-WALLET" })
    @GetMapping(path = "/teller/till")
    public ResponseEntity<?> getListTellersTill() {
        return configService.ListTellersTill();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create a Wallet Event", tags = { "BANK-WALLET" })
    @PostMapping(path = "/create/event")
    public ResponseEntity<?> createEventCharge(@Valid @RequestBody EventChargeDTO eventPojo) {
        return configService.createdEvents(eventPojo);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update a Wallet Event", tags = { "BANK-WALLET" })
    @PutMapping(path = "/update/event/{eventId}")
    public ResponseEntity<?> updateEventCharge(@Valid @RequestBody UpdateEventChargeDTO eventPojo, @PathVariable("eventId") Long eventId) {
        return configService.updateEvents(eventPojo,eventId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update a Wallet Event", tags = { "BANK-WALLET" })
    @DeleteMapping(path = "/update/event/{eventId}/delete")
    public ResponseEntity<?> deleteEventCharge(@PathVariable("eventId") Long eventId) {
        return configService.deleteEvent(eventId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "List Wallet Event", tags = { "BANK-WALLET" })
    @GetMapping(path = "/event/charges")
    public ResponseEntity<?> getListEventChrg() {
        return configService.ListEvents();
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Single Wallet Event", tags = { "BANK-WALLET" })
    @GetMapping(path = "/event/charges/{chargeId}")
    public ResponseEntity<?> getSingleEventCharge(@PathVariable("chargeId") Long chargeId) {
        return configService.getSingleEvents(chargeId);
    }



    @ApiOperation(value = "Create a Transaction Charge", tags = { "BANK-WALLET" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PostMapping(path = "/create/transaction/charge")
    public ResponseEntity<?> createTransactionCharge(@Valid @RequestBody ChargeDTO charge) {
        return configService.createCharge(charge);
    }


//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
//    @ApiOperation(value = "List Transaction Charge", tags = { "BANK-WALLET" })
//    @GetMapping(path = "/transaction/charges")
//    public ResponseEntity<?> ListAllCharge() {
//        return configService.ListTranCharge();
//    }
//
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
//    @ApiOperation(value = "Get Transaction Charge", tags = { "BANK-WALLET" })
//    @GetMapping(path = "/transaction/charges/{chargeId}")
//    public ResponseEntity<?> GetTranCharge(@PathVariable("chargeId") Long chargeId) {
//        return configService.findTranCharge(chargeId);
//    }
//
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
//    @ApiOperation(value = "Update Transaction Charge", tags = { "BANK-WALLET" })
//    @PutMapping(path = "/transaction/charges/{chargeId}")
//    public ResponseEntity<?> creteDefaultCode(@Valid @RequestBody ModifyChargeDTO charge,
//    		@PathVariable("chargeId") Long chargeId) {
//        return configService.updateTranCharge(charge,chargeId);
//    }




}
