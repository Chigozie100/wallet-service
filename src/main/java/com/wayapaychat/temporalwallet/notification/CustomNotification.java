package com.wayapaychat.temporalwallet.notification;

import java.util.Arrays;
import java.util.Objects;

import com.wayapaychat.temporalwallet.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.enumm.EventCategory;
import com.wayapaychat.temporalwallet.enumm.SMSEventStatus;
import com.wayapaychat.temporalwallet.proxy.NotificationProxy;
import com.wayapaychat.temporalwallet.util.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomNotification {

	@Autowired
	private NotificationProxy notificationFeignClient;

	public void pushEMAIL(String subject, String token, String name, String email, String message, Long userId) {

		EmailEvent emailEvent = new EmailEvent();
		EmailPayload data = new EmailPayload();
		data.setMessage(message);
		data.setNames(Arrays.asList(new EmailRecipient(name, email)));
		emailEvent.setData(data);

		emailEvent.setEventType(Constant.EMAIL);
		emailEvent.setEventCategory("TRANSACTION");
		emailEvent.setProductType(Constant.PRODUCT);
		emailEvent.setSubject(subject);
		emailEvent.setInitiator(userId.toString());

		try {
			sendEmailNotification(emailEvent, token);
		} catch (Exception ex) {
			log.error("Unable to pushEMAIL: " + ex.getLocalizedMessage());
		}

	}

	public void pushTranEMAIL(String subject, String token, String name, String email, String message, Long userId,
			String amount, String tranId, String tranDate, String narrate, String accountNumber, String transactionType,
			String availableBalance) {

		TransEmailEvent emailEvent = new TransEmailEvent();
		EmailPayload data = new EmailPayload();
		data.setMessage(message);
		data.setNames(Arrays.asList(new EmailRecipient(name, email)));
		emailEvent.setData(data);

		emailEvent.setEventType(Constant.EMAIL);
		emailEvent.setProductType(Constant.PRODUCT);
		emailEvent.setSubject(subject);
		emailEvent.setEventCategory(EventCategory.TRANSACTION);
		emailEvent.setInitiator(userId.toString());
		emailEvent.setAmount(amount);
		emailEvent.setTransactionId(tranId);
		emailEvent.setTransactionDate(CryptoUtils.getNigeriaDate(tranDate));
		emailEvent.setNarration(narrate);
		emailEvent.setAvailableBalance(availableBalance);
		emailEvent.setAccountName(name);
		emailEvent.setAccountNumber(accountNumber);
		emailEvent.setTransactionCurrency("NGN");
		emailEvent.setTransactionType(transactionType);

		try {
			postEmailNotification(emailEvent, token);
		} catch (Exception ex) {
			log.error("Unable to pushTranEMAIL: " + ex.getLocalizedMessage());
		}

	}

	public void pushNonWayaEMAIL(String token, String name, String email, String message, Long userId,
			String amount, String tranId, String tranDate, String narrate) {

		TransEmailEvent emailEvent = new TransEmailEvent();
		EmailPayload data = new EmailPayload();
		data.setMessage(message);
		data.setNames(Arrays.asList(new EmailRecipient(name, email)));
		emailEvent.setData(data);

		emailEvent.setEventType("EMAIL");
		emailEvent.setProductType("WAYABANK");
		emailEvent.setData(data);
		emailEvent.setEventCategory(EventCategory.NON_WAYA);
		emailEvent.setInitiator(userId.toString());
		emailEvent.setAmount(amount);
		emailEvent.setTransactionId(tranId);
		emailEvent.setTransactionDate(CryptoUtils.getNigeriaDate(tranDate));
		emailEvent.setNarration(narrate);

		try {
			postEmailNotification(emailEvent, token);
		} catch (Exception ex) {
			log.error("Unable to pushNonWayaEMAIL: " + ex.getLocalizedMessage());
		}

	}

	public void pushWayaSMS(String token, String name, String phone, String message, Long userId, String email) {

		SmsEvent smsEvent = new SmsEvent();

		SmsPayload data = new SmsPayload();
		data.setMessage(message);
		data.setSmsEventStatus(SMSEventStatus.TRANSACTION);
		data.setRecipients(Arrays.asList(new SmsRecipient(email, phone)));
		smsEvent.setData(data);

		smsEvent.setEventType("SMS");
		smsEvent.setInitiator(userId.toString());

		try {
			boolean check = smsNotification(smsEvent, token);
		} catch (Exception ex) {
			log.error("Unable to pushWayaSMS: " + ex.getLocalizedMessage());
		}
	}

	public void pushSMS(String token, String name, String phone, String message, Long userId) {

		SmsEvent smsEvent = new SmsEvent();
		SmsPayload data = new SmsPayload();
		data.setMessage(message);
		data.setSmsEventStatus(SMSEventStatus.NONWAYA);
		data.setRecipients(Arrays.asList(new SmsRecipient("admin@wayapaychat.com", phone)));
		smsEvent.setData(data);

		smsEvent.setEventType("SMS");
		smsEvent.setInitiator(userId.toString());

		try {
			smsNotification(smsEvent, token);
		} catch (Exception ex) {
			log.error("Unable to pushSMS: " + ex.getLocalizedMessage());
		}
	}

	public void pushInApp(String token, String name, String recipient, String recipientMessage, String message,
			Long userId, String category) {

		InAppEvent appEvent = new InAppEvent();
		InAppPayload data = new InAppPayload();
		data.setMessage(message);
		data.setUsers(Arrays.asList(new InAppRecipient(Objects.requireNonNullElse(recipient, "0"))));
		appEvent.setData(data);

		appEvent.setCategory(category);
		appEvent.setEventType("IN_APP");
		appEvent.setInitiator(Objects.requireNonNullElse(String.valueOf(userId), "0"));

		try {
			appNotification(appEvent, token);
		} catch (Exception ex) {
			log.error("Unable to pushInApp: " + ex.getLocalizedMessage());
		}
	}

	public void pushInApp(String token, String name, String recipient, String message, Long userId, String category) {

		InAppEvent appEvent = new InAppEvent();
		InAppPayload data = new InAppPayload();
		data.setMessage(message);
		data.setUsers(Arrays.asList(
				new InAppRecipient(Objects.requireNonNullElse(recipient, "0"))));
		appEvent.setData(data);

		appEvent.setCategory(category);
		appEvent.setEventType("IN_APP");
		appEvent.setInitiator(userId.toString());

		try {
			appNotification(appEvent, token);
		} catch (Exception ex) {
			log.error("Unable to pushInApp " + ex.getMessage());
		}
	}

	public void appNotification(InAppEvent appEvent, String token) {
		try {
			ResponseEntity<ResponseObj<?>> responseEntity = notificationFeignClient.InAppNotify(appEvent, token);
			ResponseObj<?> infoResponse = responseEntity.getBody();
			assert infoResponse != null;
			log.info("user response in-app sent status :: " + infoResponse.status);
		} catch (Exception e) {
			log.error("Unable to send SMS" + e.getMessage());
		}

	}

	public Boolean smsNotification(SmsEvent smsEvent, String token) {
		try {
			ResponseEntity<ResponseObj<?>> responseEntity = notificationFeignClient.smsNotifyUser(smsEvent, token);
			ResponseObj<?> infoResponse = responseEntity.getBody();
			log.debug("user response sms sent status :: " + Objects.requireNonNull(infoResponse).status);
			return infoResponse.status;
		} catch (Exception e) {
			log.error("Unable to send SMS: " + e.getLocalizedMessage());
			return false;
		}

	}

	public void sendEmailNotification(EmailEvent emailEvent, String token) {

		try {
			NotifyObjectBody responseEntity = notificationFeignClient.emailNotifyUser(emailEvent, token);
			log.info("User response email sent status :: " + responseEntity.isStatus());
		} catch (Exception ex) {
			log.error("Unable to generate transaction Id", ex);
		}

	}

	public void postEmailNotification(TransEmailEvent emailEvent, String token) {

		try {
			NotifyObjectBody responseEntity = notificationFeignClient.emailNotifyTranUser(emailEvent, token);
			log.info("User response email sent status :: " + responseEntity.isStatus());
		} catch (Exception ex) {
			log.error("Unable to generate transaction Id", ex);
		}

	}

}
