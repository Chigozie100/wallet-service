package com.wayapaychat.temporalwallet.proxy;

import com.wayapaychat.temporalwallet.pojo.ApiResponseBody;
import com.wayapaychat.temporalwallet.pojo.LogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LOGGING-SERVICE-API", url = "${waya.logging-service.base-url}")
public interface LogServiceProxy {

    @PostMapping("/api/v1/log/create")
    ApiResponseBody<LogRequest> saveNewLog(@RequestBody LogRequest logPojo);

}
