package com.wayapaychat.temporalwallet.service;


import com.wayapaychat.temporalwallet.pojo.AppendToVirtualAccount;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;

import java.time.LocalDate;

public interface VirtualService {

    void transactionWebhookData();

    void createVirtualAccount(VirtualAccountRequest account);

    void appendNameToVirtualAccount(AppendToVirtualAccount account);

    void accountTransactionQuery(String accountNumber, LocalDate startDate, LocalDate endDate);



}
