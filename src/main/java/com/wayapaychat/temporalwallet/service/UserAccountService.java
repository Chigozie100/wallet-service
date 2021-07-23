package com.wayapaychat.temporalwallet.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.dto.AdminAccountRestrictionDTO;
import com.wayapaychat.temporalwallet.dto.UserAccountDTO;
import com.wayapaychat.temporalwallet.dto.UserDTO;
import com.wayapaychat.temporalwallet.dto.WalletCashAccountDTO;
import com.wayapaychat.temporalwallet.dto.WalletEventAccountDTO;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.response.ApiResponse;

public interface UserAccountService {
	
	ResponseEntity<?> createUser(UserDTO user);
	
	ResponseEntity<?> createUserAccount(WalletUserDTO user);
	
	ResponseEntity<?> createCashAccount(WalletCashAccountDTO user);
	
	ResponseEntity<?> createEventAccount(WalletEventAccountDTO user);
	
	ResponseEntity<?> createAccount(AccountPojo2 accountPojo);
	
	ApiResponse<?> findCustWalletById(Long walletId);
	
	ApiResponse<?> findAcctWalletById(Long walletId);
	
	ResponseEntity<?> getListCommissionAccount(List<Integer> ids);
	
	ResponseEntity<?> getAccountInfo(String accountNo);
	
	ResponseEntity<?> getUserAccountList(long userId);
	
	ResponseEntity<?> getAllAccount();
	
	ResponseEntity<?> getUserCommissionList(long userId);
	
	ResponseEntity<?> makeDefaultWallet(String accountNo);
	
	ResponseEntity<?> UserWalletLimit(long userId);
	
	ResponseEntity<?> getALLCommissionAccount();
	
	ResponseEntity<?> getAccountCommission(String accountNo);
	
	ResponseEntity<?> getAccountDefault(Long user_id);
	
	ResponseEntity<?> searchAccount(String search);
	
	ResponseEntity<?> modifyUserAccount(UserAccountDTO user);
	
	ResponseEntity<?> UserAccountAccess(AdminAccountRestrictionDTO user);

}
