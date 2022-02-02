package com.wayapaychat.temporalwallet.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.temporalwallet.notification.EmailEvent;
import com.wayapaychat.temporalwallet.notification.InAppEvent;
import com.wayapaychat.temporalwallet.notification.NotifyObjectBody;
import com.wayapaychat.temporalwallet.notification.ResponseObj;
import com.wayapaychat.temporalwallet.notification.SmsEvent;
import com.wayapaychat.temporalwallet.notification.TransEmailEvent;

@FeignClient(name = "${waya.notification.service}", url = "${waya.notification.notificationurl}")
public interface NotificationProxy {
	
	@PostMapping("/sms-notification-atalking")
	ResponseEntity<ResponseObj<?>> smsNotifyUser(@RequestBody SmsEvent smsEvent, @RequestHeader("Authorization") String token);
	
	@PostMapping("/email-notification")
	NotifyObjectBody emailNotifyUser(@RequestBody EmailEvent emailDto, @RequestHeader("Authorization") String token);
	
	@PostMapping("/in-app-notification")
	ResponseEntity<ResponseObj<?>> InAppNotify(@RequestBody InAppEvent appEvent, @RequestHeader("Authorization") String token);
	
	@PostMapping("/api/v1/sms-notification")
	ResponseEntity<ResponseObj<?>> smsNotifyUserTwilo(@RequestBody SmsEvent smsEvent, @RequestHeader("Authorization") String token);
	
	@PostMapping("/api/v1/email-notification-transaction")
	NotifyObjectBody emailNotifyTranUser(@RequestBody TransEmailEvent emailDto, @RequestHeader("Authorization") String token);

}
