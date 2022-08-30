package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.pojo.RecurrentConfigPojo;
import org.springframework.http.ResponseEntity;

public interface ConfigService {
	
	ResponseEntity<?> createDefaultCode(WalletConfigDTO configPojo);
	ResponseEntity<?> getListDefaultCode();
	ResponseEntity<?> getListCodeValue(Long id);
	ResponseEntity<?> getAllCodeValue(String name);
	ResponseEntity<?> getCode(Long codeId);
	ResponseEntity<?> createProduct(ProductCodeDTO product);
	ResponseEntity<?> ListProduct();
	ResponseEntity<?> findProduct(Long id);
	ResponseEntity<?> getProduct(String schm, String gl);
	ResponseEntity<?> ListProductCode();
	ResponseEntity<?> ListAccountProductCode();
	ResponseEntity<?> createProductParameter(ProductDTO product);
	ResponseEntity<?> createInterestParameter(InterestDTO interest);
	ResponseEntity<?> createParamCOA(AccountGLDTO chat);
	ResponseEntity<?> ListCOA();
	ResponseEntity<?> createdTeller(WalletTellerDTO teller);
	ResponseEntity<?> ListTellersTill();
	ResponseEntity<?> createdEvents(EventChargeDTO event);
	ResponseEntity<?> updateEvents(UpdateEventChargeDTO event, Long eventId);
	ResponseEntity<?> deleteEvent(Long eventId);
	//
	ResponseEntity<?> ListEvents();
	ResponseEntity<?> getSingleEvents(Long id);
	ResponseEntity<?> createCharge(ChargeDTO event);
	ResponseEntity<?> ListTranCharge();
	ResponseEntity<?> findTranCharge(Long id);
	ResponseEntity<?> updateTranCharge(ModifyChargeDTO event, Long chargeId);

	ResponseEntity<?> createRecurrentPayment(RecurrentConfigPojo request);
	ResponseEntity<?> updateRecurrentPayment(RecurrentConfigPojo request, Long id);
	ResponseEntity<?> toggleRecurrentPayment(Long id);
	ResponseEntity<?> getAllRecurrentPayment();
	ResponseEntity<?> getRecurrentPayment(Long id);

}
