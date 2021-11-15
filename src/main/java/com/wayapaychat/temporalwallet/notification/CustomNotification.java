package com.wayapaychat.temporalwallet.notification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.enumm.SMSEventStatus;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.proxy.NotificationProxy;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomNotification {

	@Autowired
	private NotificationProxy notificationFeignClient;

	public void pushEMAIL(String token, String name, String email, String message, Long userId) {

		EmailEvent emailEvent = new EmailEvent();

		emailEvent.setEventType("EMAIL");
		EmailPayload data = new EmailPayload();

		data.setMessage(message);

		EmailRecipient emailRecipient = new EmailRecipient();
		emailRecipient.setFullName(name);
		emailRecipient.setEmail(email);

		List<EmailRecipient> addUserId = new ArrayList<>();
		addUserId.add(emailRecipient);
		data.setNames(addUserId);

		emailEvent.setData(data);
		emailEvent.setInitiator(userId.toString());
		log.info(emailEvent.toString());

		try {
			sendEmailNotification(emailEvent, token);
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.NOT_FOUND);
		}

	}
	
	public void pushSMS(String token, String name, String phone, String message, Long userId) {
		
		SmsEvent smsEvent = new SmsEvent();
	    SmsPayload data = new SmsPayload();
	    data.setMessage(message);

	    data.setSmsEventStatus(SMSEventStatus.NONWAYA);

	    SmsRecipient smsRecipient = new SmsRecipient();
	    smsRecipient.setEmail("emmanuel.njoku@wayapaychat.com");
	    smsRecipient.setTelephone(phone);
	    List<SmsRecipient> addList = new ArrayList<>();
	    addList.add(smsRecipient);

	    data.setRecipients(addList);
	    smsEvent.setData(data);

	    smsEvent.setEventType("SMS");
	    smsEvent.setInitiator(userId.toString());
	    log.info(smsEvent.toString());

	    try {
	        smsNotification(smsEvent,token);

	    }catch (Exception ex){
	    	throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
	    }
	}
	
	public Boolean smsNotification(SmsEvent smsEvent, String token) {
	    try {
	        ResponseEntity<ResponseObj<?>>  responseEntity = notificationFeignClient.smsNotifyUser(smsEvent,token);
	        ResponseObj<?> infoResponse = responseEntity.getBody();
	        log.info("user response sms sent status :: " +infoResponse.status);
	        return infoResponse.status;
	    } catch (Exception e) {
	        log.error("Unable to send SMS", e.getMessage());
	        throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
	    }

	}

	public void sendEmailNotification(EmailEvent emailEvent, String token) {

		try {
			NotifyObjectBody responseEntity = notificationFeignClient.emailNotifyUser(emailEvent, token);
			log.info("User response email sent status :: " + responseEntity.isStatus());
		} catch (Exception ex) {
			log.error("Unable to generate transaction Id", ex);
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

}
