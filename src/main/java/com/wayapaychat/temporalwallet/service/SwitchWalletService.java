package com.wayapaychat.temporalwallet.service;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.dto.CreateSwitchDTO;

public interface SwitchWalletService {

	ResponseEntity<?> ListAllSwitches();
	
	ResponseEntity<?> GetSwitch(String ident);
	
	ResponseEntity<?> UpdateSwitche();
	
	ResponseEntity<?> DeleteSwitches();
	
	ResponseEntity<?> CreateWalletOperator(CreateSwitchDTO offno);
}
