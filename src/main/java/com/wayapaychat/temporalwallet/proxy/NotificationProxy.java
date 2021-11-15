package com.wayapaychat.temporalwallet.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.temporalwallet.notification.EmailEvent;
import com.wayapaychat.temporalwallet.notification.NotifyObjectBody;
import com.wayapaychat.temporalwallet.notification.ResponseObj;
import com.wayapaychat.temporalwallet.notification.SmsEvent;

@FeignClient(name = "${waya.notification.service}", url = "${waya.notification.notificationurl}")
public interface NotificationProxy {
	
	@PostMapping("/api/v1/sms-notification")
	ResponseEntity<ResponseObj<?>> smsNotifyUser(@RequestBody SmsEvent smsEvent, @RequestHeader("Authorization") String token);
	
	@PostMapping("/email-notification")
	NotifyObjectBody emailNotifyUser(@RequestBody EmailEvent emailDto, @RequestHeader("Authorization") String token);

}
