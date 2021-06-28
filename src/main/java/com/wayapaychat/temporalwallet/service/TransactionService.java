package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo2;
import org.springframework.http.ResponseEntity;

public interface TransactionService {

    ResponseEntity transactAmount(TransactionPojo transactionPojo);
    ResponseEntity transferTransaction(TransactionTransferPojo transactionTransferPojo);
    ResponseEntity transferTransactionWithId(TransactionTransferPojo2 transactionTransferPojo2);


}
