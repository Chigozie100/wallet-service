package com.wayapaychat.temporalwallet.service;


import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.entity.VirtualAccountHook;
import com.wayapaychat.temporalwallet.pojo.AppendToVirtualAccount;
import com.wayapaychat.temporalwallet.pojo.VATransactionSearch;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountHookRequest;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import org.springframework.http.ResponseEntity;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public interface VirtualService {

    ResponseEntity<?> registerWebhookUrl(VirtualAccountHookRequest request);

    void transactionWebhookData();

    ResponseEntity<SuccessResponse> createVirtualAccount(VirtualAccountRequest account);

    ResponseEntity<SuccessResponse> createVirtualAccountVersion2(VirtualAccountRequest account);

    ResponseEntity<SuccessResponse> searchVirtualTransactions(VATransactionSearch account);

    void appendNameToVirtualAccount(AppendToVirtualAccount account);

    SuccessResponse accountTransactionQuery(String accountNumber, LocalDate startDate, LocalDate endDate);

    SuccessResponse nameEnquiry(String accountNumber, String bankCode);

    SuccessResponse balanceEnquiry(String accountNumber);

    SuccessResponse decryptString(String obj);

    SuccessResponse fundTransfer(BankPaymentDTO paymentDTO);


}
