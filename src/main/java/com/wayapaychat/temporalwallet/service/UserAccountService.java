package com.wayapaychat.temporalwallet.service;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.dto.UserDTO;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;

public interface UserAccountService {
	
	ResponseEntity<?> createUser(UserDTO user);
	
	ResponseEntity<?> createUserAccount(WalletUserDTO user);
	
	ResponseEntity<?> createAccount(AccountPojo2 accountPojo);

}
