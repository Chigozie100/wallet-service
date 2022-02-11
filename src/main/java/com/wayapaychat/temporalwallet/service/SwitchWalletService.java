package com.wayapaychat.temporalwallet.service;


import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.dto.CreateSwitchDTO;
import com.wayapaychat.temporalwallet.dto.ToggleSwitchDTO;
import com.wayapaychat.temporalwallet.entity.Provider;

public interface SwitchWalletService {

	ResponseEntity<?> ListAllSwitches();
	
	ResponseEntity<?> GetSwitch(String ident);
	
	ResponseEntity<?> UpdateSwitche(ToggleSwitchDTO toggle);
	
	ResponseEntity<?> DeleteSwitches(Long id);
	
	ResponseEntity<?> CreateWalletOperator(CreateSwitchDTO offno);
	
	ResponseEntity<?> getProvider();
	
	ResponseEntity<?> enableProvider(long providerId);
	
	public Provider getActiveProvider();

}
