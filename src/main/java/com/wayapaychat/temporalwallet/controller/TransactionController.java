package com.wayapaychat.temporalwallet.controller;

import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo2;
import com.wayapaychat.temporalwallet.pojo.UserPojo;
import com.wayapaychat.temporalwallet.service.TransactionService;
import com.wayapaychat.temporalwallet.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @ApiOperation(value = "Withdraw or Deposit Amount")
    @PostMapping(path = "/dotransaction")
    public ResponseEntity doTransaction(@RequestBody TransactionPojo transactionPojo) {
        return transactionService.transactAmount(transactionPojo);
    }

    @ApiOperation(value = "Wallet to Wallet Transfer")
    @PostMapping(path = "/wallet2wallet")
    public ResponseEntity doTransaction(@RequestBody TransactionTransferPojo transactionTransferPojo) {
        return transactionService.transferTransaction(transactionTransferPojo);
    }

    @ApiOperation(value = "Wallet to Wallet Transfer by User Id's")
    @PostMapping(path = "/wallet2wallet-by-ids")
    public ResponseEntity doTransaction2(@RequestBody TransactionTransferPojo2 transactionTransferPojo) {
        return transactionService.transferTransactionWithId(transactionTransferPojo);
    }

}