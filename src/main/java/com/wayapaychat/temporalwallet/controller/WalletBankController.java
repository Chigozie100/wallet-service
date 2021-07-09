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

import com.wayapaychat.temporalwallet.dto.ProductCodeDTO;
import com.wayapaychat.temporalwallet.dto.ProductDTO;
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
    
    @ApiOperation(value = "Get Wallet CodeValues using codeValueId")
    @GetMapping(path = "/codeValue/{codeValueId}")
    public ResponseEntity<?> getCodeValue(@PathVariable("codeValueId") Long codeValueId) {
        return configService.getListCodeValue(codeValueId);
    }
    
    @ApiOperation(value = "Get Wallet CodeValues using codename")
    @GetMapping(path = "/codeValue/{codeName}/command")
    public ResponseEntity<?> FetchCodeValue(@PathVariable("codeName") String codeName) {
        return configService.getAllCodeValue(codeName);
    }
    
    @ApiOperation(value = "Get Wallet CodeValues using codeId")
    @GetMapping(path = "/codes/{codeId}")
    public ResponseEntity<?> getCode(@PathVariable("codeId") Long codeId) {
        return configService.getCode(codeId);
    }
    
    @ApiOperation(value = "Create a Wallet Product Code")
    @PostMapping(path = "/create/product")
    public ResponseEntity<?> creteProductCode(@Valid @RequestBody ProductCodeDTO product) {
        return configService.createProduct(product);
    }
    
    @ApiOperation(value = "Get Wallet Product Code")
    @GetMapping(path = "/product/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable("productId") Long productId) {
        return configService.findProduct(productId);
    }
    
    @ApiOperation(value = "Get Wallet Product Code")
    @GetMapping(path = "/product/code/{productCode}")
    public ResponseEntity<?> getProduct(@PathVariable("productCode") String productCode) {
        return configService.getProduct(productCode);
    }
    
    @ApiOperation(value = "List Wallet Product Code")
    @GetMapping(path = "/product")
    public ResponseEntity<?> getListProductCode() {
        return configService.ListProductCode();
    }
    
    @ApiOperation(value = "Create a Wallet Product Code")
    @PostMapping(path = "/create/product/parameter")
    public ResponseEntity<?> creteProductParameter(@Valid @RequestBody ProductDTO product) {
        return configService.createProductParameter(product);
    }

}
