package com.wayapaychat.temporalwallet.proxy;
  
import com.wayapaychat.temporalwallet.response.TransactionAnalysis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "SMU-SERVICE-API", url = "${waya.smu-service.base-url}")
public interface SMUProxy {

    @GetMapping("/api/v1/wallet/transaction/overall-smu-based-analysis")
    TransactionAnalysis GetTransactionAnalytics();
 
}
