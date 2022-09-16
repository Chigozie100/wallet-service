package com.wayapaychat.temporalwallet.service;


import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.entity.VirtualAccountHook;
import com.wayapaychat.temporalwallet.pojo.AppendToVirtualAccount;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountHookRequest;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface VirtualService {

    ResponseEntity<?> registerWebhookUrl(VirtualAccountHookRequest request);

    void transactionWebhookData();

    ResponseEntity<SuccessResponse> createVirtualAccount(VirtualAccountRequest account);

    void appendNameToVirtualAccount(AppendToVirtualAccount account);

    SuccessResponse accountTransactionQuery(String accountNumber, LocalDate startDate, LocalDate endDate);

    SuccessResponse nameEnquiry(String accountNumber, String bankCode);

    SuccessResponse balanceEnquiry(String accountNumber);

    SuccessResponse fundTransfer(BankPaymentDTO paymentDTO);




}
