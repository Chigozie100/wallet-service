package com.wayapaychat.temporalwallet.service;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.dto.WalletConfigDTO;

public interface ConfigService {
	
	ResponseEntity<?> createDefaultCode(WalletConfigDTO configPojo);
	ResponseEntity<?> getListDefaultCode();
	ResponseEntity<?> getListCodeValue(Long id);

}
