package com.wayapaychat.temporalwallet.controller;


import com.wayapaychat.temporalwallet.service.ReversalSetupService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reversal-config")
@Tag(name = "CONFIGURE-REVERSAL-DAYS", description = "Ability for waya to manage the number of days to keep the temporal wallet before reversing it")
@Validated
public class SetupController {

    @Autowired
    private ReversalSetupService reversalSetupService;


    @ApiOperation(value = "Create a Temporal-Wallet Reversal Day", tags = { "CONFIGURE-REVERSAL-DAYS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PostMapping(path = "/")
    public ResponseEntity<?> createReversalDay(@RequestParam("days") Integer days) {
        return reversalSetupService.create(days);
    }


    @ApiOperation(value = "View a Temporal-Wallet Reversal Day", tags = { "CONFIGURE-REVERSAL-DAYS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> ViewReversalDay(@PathVariable String id) {
        return reversalSetupService.view(Long.parseLong(id));
    }

    @ApiOperation(value = "View List of Temporal-Wallet Reversal Days", tags = { "CONFIGURE-REVERSAL-DAYS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @GetMapping(path = "/")
    public ResponseEntity<?> viewAllReversalDays() {
        return reversalSetupService.viewAll();
    }

    @ApiOperation(value = "Update active a Temporal-Wallet Reversal Day", tags = { "CONFIGURE-REVERSAL-DAYS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateReversalDay(@RequestParam("days") Integer days, @PathVariable String id) {
        return reversalSetupService.update(days,Long.parseLong(id));
    }

    @ApiOperation(value = "Toggle active a Temporal-Wallet Reversal Day", tags = { "CONFIGURE-REVERSAL-DAYS" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
    @PutMapping(path = "/{id}/toggle")
    public ResponseEntity<?> toggleReversalDay(@PathVariable String id) {
        return reversalSetupService.toggle(Long.parseLong(id));
    }


}
