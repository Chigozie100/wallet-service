package com.wayapaychat.temporalwallet.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.dto.WalletConfigDTO;
import com.wayapaychat.temporalwallet.service.ConfigService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/v1/bank")
public class WalletBankController {
	
	@Autowired
    ConfigService configService;
	
	@ApiOperation(value = "Create a Wallet Default Special Code")
    @PostMapping(path = "/create/code")
    public ResponseEntity<?> creteDefaultCode(@Valid @RequestBody WalletConfigDTO configPojo) {
        return configService.createDefaultCode(configPojo);
    }

    @ApiOperation(value = "Get List of Wallet Default Special Code")
    @GetMapping(path = "/codes")
    public ResponseEntity<?> getDefaultCode() {
        return configService.getListDefaultCode();
    }
    
    @ApiOperation(value = "Get List of Wallet Code Values")
    @GetMapping(path = "/codeValue/{code_id}")
    public ResponseEntity<?> getCodeValue(@PathVariable("code_id") Long code_id) {
        return configService.getListCodeValue(code_id);
    }

}
