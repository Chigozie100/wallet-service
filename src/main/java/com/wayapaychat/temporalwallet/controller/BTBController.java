// package com.wayapaychat.temporalwallet.controller;

// import java.util.List;
// import java.util.concurrent.CompletableFuture;

// import javax.servlet.http.HttpServletRequest;
// import javax.validation.Valid;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestPart;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;

// import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
// import com.wayapaychat.temporalwallet.dto.BulkTransactionCreationDTO;
// import com.wayapaychat.temporalwallet.exception.CustomException;
// import com.wayapaychat.temporalwallet.service.CoreBankingService;
// import com.wayapaychat.temporalwallet.service.TransAccountService;
// import com.wayapaychat.temporalwallet.util.SuccessResponse;

// import io.swagger.annotations.ApiImplicitParam;
// import io.swagger.annotations.ApiImplicitParams;
// import io.swagger.annotations.ApiOperation;
// import io.swagger.v3.oas.annotations.parameters.RequestBody;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.extern.slf4j.Slf4j;

// @RestController
// @RequestMapping("/api/v1/")
// @Tag(name = "B2B", description = "ThirdParty API Service")
// @Validated
// @Slf4j
// public class BTBController {
    
//     private final TransAccountService transAccountService;
//     @Autowired
//     public BTBController(TransAccountService transAccountService, CoreBankingService coreBankingService) {
//         this.transAccountService = transAccountService;
//     }
//     @ApiImplicitParams({
//         @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
// @ApiOperation(value = "Create multiple transaction", notes = "Transfer amount from one account to another account", tags = {
//         "B2B" })
// @PostMapping("/transfer/bulk-transaction")
// public ResponseEntity<?> createBulkTrans(HttpServletRequest request,
//                                          @Valid @RequestBody BulkTransactionCreationDTO userList) {
//     String apiKey = request.getHeader("API-KEY");
    
//     ResponseEntity<?> res = transAccountService.createBulkTransaction(request, userList);
//     if (!res.getStatusCode().is2xxSuccessful()) {
//         return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
//     }
//     log.info("Send Money: {}", userList);
//     return new ResponseEntity<>(res, HttpStatus.OK);
// }

// @ApiImplicitParams({
//         @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
// @ApiOperation(value = "Create multiple transaction", notes = "Transfer amount from one account to another account", tags = {
//         "B2B" })
// @PostMapping(path = "/transfer/bulk-transaction-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
// public ResponseEntity<?> createBulkTransExcel(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
//     String apiKey = request.getHeader("API-KEY");

//     ResponseEntity<?> res = transAccountService.createBulkExcelTrans(request, file);
//     if (!res.getStatusCode().is2xxSuccessful()) {
//         return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
//     }
//     log.info("Send Money: {}", file);
//     return new ResponseEntity<>(res, HttpStatus.OK);
// }


// @ApiImplicitParams({
//     @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
// @ApiOperation(value = "Send Money to commercial bank", notes = "Post Money", tags = { "TRANSACTION-WALLET" })
// @PostMapping("/fund/bank/account")
// public CompletableFuture<?> fundBank(HttpServletRequest request, @Valid @RequestBody List<BankPaymentDTO> transfer) {
//     try {
//         return CompletableFuture.completedFuture(processTransaction(request, transfer));
//     } catch (Exception e) {
//         throw new CustomException(e.getLocalizedMessage(), HttpStatus.EXPECTATION_FAILED); 
//     }

// }

//     private SuccessResponse processTransaction(HttpServletRequest request, List<BankPaymentDTO> transfer) {
//         for(BankPaymentDTO data: transfer){
//             transAccountService.BankTransferPayment(request, data);
//        }
//        return new SuccessResponse("message", null);
//     }
 

//     @Async
//     public CompletableFuture<ServiceResponse> getAccountsByUserId(@PathVariable("userId") String userId) {
//         return CompletableFuture.completedFuture(accountCreationService.getAccountsByUserId(userId));
//     }
// }
