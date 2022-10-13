package com.wayapaychat.temporalwallet.proxy;

import com.wayapaychat.temporalwallet.dto.BillerManagementResponse;
import com.wayapaychat.temporalwallet.notification.ResponseObj;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "BILLS-SERVICE-API", url = "${app.config.bills-service.base-url}")
public interface BillsPayProxy {

    @GetMapping("/config/biller/sync-commission-billers")
    ResponseEntity<ResponseObj<List<BillerManagementResponse>>> getBillers();
}
