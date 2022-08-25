package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.pojo.AppendToVirtualAccount;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import com.wayapaychat.temporalwallet.service.VirtualService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VirtualServiceImpl implements VirtualService {

    @Override
    public void transactionWebhookData() {

    }

    @Override
    public void createVirtualAccount(VirtualAccountRequest account) {

    }

    @Override
    public void appendNameToVirtualAccount(AppendToVirtualAccount account) {

    }

    @Override
    public void accountTransactionQuery(String accountNumber, LocalDate startDate, LocalDate endDate) {

    }


}
