package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "BATCH-OPERATIONS", description = "Batch Transaction Wallet Service API")
@Validated
@Slf4j
public class BatchOperation {

    @Autowired
    TransAccountService transAccountService;

    public String massDebit(){
        return "";
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "To transfer money from one waya official account to simulated user wallet", notes = "Post Money", tags = {
            "BATCH-OPERATIONS" })
    @PostMapping("/official/simulated-user/transfer")
    public ResponseEntity<?> OfficialUserMoney(HttpServletRequest request,
                                               @Valid @RequestBody List<OfficeUserTransferDTO> transfer) {
        ApiResponse<?> res = transAccountService.OfficialUserTransfer(request, transfer);
        if (!res.getStatus()) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        log.info("Send Money: {}", transfer);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }


}
