package com.wayapaychat.temporalwallet.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.enumm.EventCategory;
import com.wayapaychat.temporalwallet.enumm.SMSEventStatus;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.proxy.NotificationProxy;
import com.wayapaychat.temporalwallet.util.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomNotification {

	@Autowired
	private NotificationProxy notificationFeignClient;

	public void pushEMAIL(String token, String name, String email, String message, Long userId) {

		EmailEvent emailEvent = new EmailEvent();

		emailEvent.setEventType("EMAIL");
		emailEvent.setEventCategory("TRANSACTION");
		emailEvent.setProductType("WAYABANK");
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
		log.info("REQUEST EMAIL WAYABANK: " + emailEvent.toString());

		try {
			sendEmailNotification(emailEvent, token);
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.NOT_FOUND);
		}

	}
	
	public void pushTranEMAIL(String token, String name, String email, String message, Long userId,
			String amount, String tranId, String tranDate, String narrate) {

		TransEmailEvent emailEvent = new TransEmailEvent();

		emailEvent.setEventType("EMAIL");
		emailEvent.setProductType("WAYABANK");
		EmailPayload data = new EmailPayload();

		data.setMessage(message);

		EmailRecipient emailRecipient = new EmailRecipient();
		emailRecipient.setFullName(name);
		emailRecipient.setEmail(email);

		List<EmailRecipient> addUserId = new ArrayList<>();
		addUserId.add(emailRecipient);
		data.setNames(addUserId);

		emailEvent.setData(data);
		emailEvent.setEventCategory(EventCategory.TRANSACTION);
		emailEvent.setInitiator(userId.toString());
		emailEvent.setAmount(amount);
		emailEvent.setTransactionId(tranId);
		emailEvent.setTransactionDate(CryptoUtils.getNigeriaDate(tranDate));
		emailEvent.setNarration(narrate);
		log.info("REQUEST EMAIL TRANSACTION: " + emailEvent.toString());

		try {
			postEmailNotification(emailEvent, token);
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.NOT_FOUND);
		}

	}
	
	public void pushNonWayaEMAIL(String token, String name, String email, String message, Long userId,
			String amount, String tranId, String tranDate, String narrate) {

		TransEmailEvent emailEvent = new TransEmailEvent();

		emailEvent.setEventType("EMAIL");
		emailEvent.setProductType("WAYABANK");
		EmailPayload data = new EmailPayload();

		data.setMessage(message);

		EmailRecipient emailRecipient = new EmailRecipient();
		emailRecipient.setFullName(name);
		emailRecipient.setEmail(email);

		List<EmailRecipient> addUserId = new ArrayList<>();
		addUserId.add(emailRecipient);
		data.setNames(addUserId);

		emailEvent.setData(data);
		emailEvent.setEventCategory(EventCategory.NON_WAYA);
		emailEvent.setInitiator(userId.toString());
		emailEvent.setAmount(amount);
		emailEvent.setTransactionId(tranId);
		emailEvent.setTransactionDate(CryptoUtils.getNigeriaDate(tranDate));
		emailEvent.setNarration(narrate);
		log.info("REQUEST EMAIL: " + emailEvent.toString());

		try {
			postEmailNotification(emailEvent, token);
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
		log.info("REQUEST SMS: " +name +" -"  +smsEvent.toString());

		try {
			boolean check = smsNotification(smsEvent, token);
			log.info("response"+check);
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public void pushInApp(String token, String name, String recipient, String recipientMessage, String message, Long userId, String category) {

		InAppEvent appEvent = new InAppEvent();
		InAppPayload data = new InAppPayload();
		data.setMessage(message);

		InAppRecipient appRecipient = new InAppRecipient();
		appRecipient.setUserId(Objects.requireNonNullElse(recipient, "0"));
		List<InAppRecipient> addUserId = new ArrayList<>();
		addUserId.add(appRecipient);

		data.setUsers(addUserId);
		appEvent.setData(data);
		appEvent.setCategory(category);

		appEvent.setEventType("IN_APP");
		if(userId !=null){
			appEvent.setInitiator(userId.toString());
		}else{
			appEvent.setInitiator("0");
		}

		log.info( name + recipientMessage + " "+ appEvent.toString());

		try {
			appNotification(appEvent, token);

		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}


	public void pushInApp(String token, String name, String recipient, String message, Long userId, String category) {
		InAppEvent appEvent = new InAppEvent();
		InAppPayload data = new InAppPayload();
		data.setMessage(message);

		InAppRecipient appRecipient = new InAppRecipient();
		appRecipient.setUserId(recipient);
		List<InAppRecipient> addUserId = new ArrayList<>();
		addUserId.add(appRecipient);

		data.setUsers(addUserId);
		appEvent.setData(data);
		appEvent.setCategory(category);

		appEvent.setEventType("IN_APP");
		appEvent.setInitiator(userId.toString());
		log.info( name + " " + appEvent.toString());

		try {
			appNotification(appEvent, token);

		} catch (Exception ex) {
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public void appNotification(InAppEvent appEvent, String token) {
		try {
			ResponseEntity<ResponseObj<?>> responseEntity = notificationFeignClient.InAppNotify(appEvent, token);
			ResponseObj<?> infoResponse = responseEntity.getBody();
			assert infoResponse != null;
			log.info("user response in-app sent status :: " + infoResponse.status);
		} catch (Exception e) {
			log.error("Unable to send SMS"+ e.getMessage());
			throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

	public Boolean smsNotification(SmsEvent smsEvent, String token) {
		try {
			ResponseEntity<ResponseObj<?>> responseEntity = notificationFeignClient.smsNotifyUser(smsEvent, token);
			ResponseObj<?> infoResponse = responseEntity.getBody();
			log.info("user response sms sent status :: " + Objects.requireNonNull(infoResponse).status);
			return infoResponse.status;
		} catch (Exception e) {
			log.error("Unable to send SMS: " + e.getLocalizedMessage());
			throw new CustomException(e.getLocalizedMessage(), HttpStatus.EXPECTATION_FAILED);
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
	
	public void postEmailNotification(TransEmailEvent emailEvent, String token) {

		try {
			NotifyObjectBody responseEntity = notificationFeignClient.emailNotifyTranUser(emailEvent, token);
			log.info("User response email sent status :: " + responseEntity.isStatus());
		} catch (Exception ex) {
			log.error("Unable to generate transaction Id", ex);
			throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

}
