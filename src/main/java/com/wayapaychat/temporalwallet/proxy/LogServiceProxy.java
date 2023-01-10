package com.wayapaychat.temporalwallet.proxy;

import com.wayapaychat.temporalwallet.pojo.ApiResponseBody;
import com.wayapaychat.temporalwallet.pojo.LogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.Map;

@FeignClient(name = "LOGGING-SERVICE-API", url = "${waya.logging-service.base-url}")
public interface LogServiceProxy {

    @PostMapping("/api/v1/log/create")
    ApiResponseBody<LogRequest> saveNewLog(@RequestBody LogRequest logPojo, @RequestHeader("Authorization") String token);

    @PostMapping("/api/v1/requestlog/create")
    Map<String, Object> logRequest(@RequestHeader("Authorization") String token, @RequestBody HashMap request);
}
