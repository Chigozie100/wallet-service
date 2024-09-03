package com.wayapaychat.temporalwallet.proxy;

import com.wayapaychat.temporalwallet.config.WebhookClientConfig;
import com.wayapaychat.temporalwallet.pojo.WebhookPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "webhookClient", configuration = WebhookClientConfig.class)
public interface WebhookClient {

    @RequestMapping(method = RequestMethod.POST, value = "/")
    ResponseEntity<String> sendWebhookNotification(@RequestHeader HttpHeaders headers, @RequestBody WebhookPayload payload);
}