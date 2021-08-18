package com.wayapaychat.temporalwallet.service;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.dto.AccountGLDTO;
import com.wayapaychat.temporalwallet.dto.EventChargeDTO;
import com.wayapaychat.temporalwallet.dto.InterestDTO;
import com.wayapaychat.temporalwallet.dto.ProductCodeDTO;
import com.wayapaychat.temporalwallet.dto.ProductDTO;
import com.wayapaychat.temporalwallet.dto.WalletConfigDTO;
import com.wayapaychat.temporalwallet.dto.WalletTellerDTO;

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
	ResponseEntity<?> ListEvents();

}
