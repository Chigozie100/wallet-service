package com.wayapaychat.temporalwallet.controller;


import com.wayapaychat.temporalwallet.pojo.RecurrentConfigPojo;
import com.wayapaychat.temporalwallet.service.ConfigService;
import com.wayapaychat.temporalwallet.service.ReversalSetupService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/config")
@Tag(name = "CONFIGURATIONS", description = "Ability for waya to manage configurations")
@Validated
public class SetupController {

    @Autowired
    private ReversalSetupService reversalSetupService;

    @Autowired
    ConfigService configService;


    @ApiOperation(value = "Create a Reversal Day", tags = { "CONFIGURATIONS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PostMapping("")
    public ResponseEntity<?> createReversalDay(@RequestParam("days") Integer days) {
        return reversalSetupService.create(days);
    }


    @ApiOperation(value = "View Reversal Day", tags = { "CONFIGURATIONS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> ViewReversalDay(@PathVariable String id) {
        return reversalSetupService.view(Long.parseLong(id));
    }

    @ApiOperation(value = "View List of Reversal Days", tags = { "CONFIGURATIONS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @GetMapping("")
    public ResponseEntity<?> viewAllReversalDays() {
        return reversalSetupService.viewAll();
    }

    @ApiOperation(value = "Update active a Reversal Day", tags = { "CONFIGURATIONS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateReversalDay(@RequestParam("days") Integer days, @PathVariable String id) {
        return reversalSetupService.update(days,Long.parseLong(id));
    }

    @ApiOperation(value = "Toggle active Reversal Day", tags = { "CONFIGURATIONS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PutMapping(path = "/{id}/toggle")
    public ResponseEntity<?> toggleReversalDay(@PathVariable String id) {
        return reversalSetupService.toggle(Long.parseLong(id));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Create RecurrentConfig ", tags = { "CONFIGURATIONS" })
    @PostMapping(path = "/recurrent-payment")
    public ResponseEntity<?> createRecurrentConfig(@Valid @RequestBody RecurrentConfigPojo request) {
        return configService.createRecurrentPayment(request);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Update RecurrentConfig", tags = { "CONFIGURATIONS" })
    @PutMapping(path = "/recurrent-payment/{id}")
    public ResponseEntity<?> updateRecurrentPayment(@Valid @RequestBody RecurrentConfigPojo request,
                                                    @PathVariable("id") Long id) {
        return configService.updateRecurrentPayment(request,id);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "toggle RecurrentConfig", tags = { "CONFIGURATIONS" })
    @PutMapping(path = "/recurrent-payment/{id}/toggle")
    public ResponseEntity<?> toggleRecurrentPayment(@PathVariable("id") Long id) {
        return configService.toggleRecurrentPayment(id);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get Single RecurrentConfig", tags = { "CONFIGURATIONS" })
    @GetMapping(path = "/recurrent-payment/{id}")
    public ResponseEntity<?> getRecurrentPayment(@PathVariable("id") Long id) {
        return configService.getRecurrentPayment(id);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Get All RecurrentConfig", tags = { "CONFIGURATIONS" })
    @GetMapping(path = "/recurrent-payment")
    public ResponseEntity<?> getAllRecurrentPayment() {
        return configService.getAllRecurrentPayment();
    }

    ///



}
