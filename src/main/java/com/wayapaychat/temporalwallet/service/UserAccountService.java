package com.wayapaychat.temporalwallet.service;

import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.dto.AccountCloseDTO;
import com.wayapaychat.temporalwallet.dto.AccountFreezeDTO;
import com.wayapaychat.temporalwallet.dto.AccountLienDTO;
import com.wayapaychat.temporalwallet.dto.AccountProductDTO;
import com.wayapaychat.temporalwallet.dto.AccountToggleDTO;
import com.wayapaychat.temporalwallet.dto.AdminAccountRestrictionDTO;
import com.wayapaychat.temporalwallet.dto.OfficialAccountDTO;
import com.wayapaychat.temporalwallet.dto.UserAccountDTO;
import com.wayapaychat.temporalwallet.dto.UserAccountDelete;
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
	
	ResponseEntity<?> createOfficialAccount(OfficialAccountDTO account);
	
	ResponseEntity<?> createAccountProduct(AccountProductDTO accountPojo);
	
	ApiResponse<?> findCustWalletById(Long walletId);
	
	ApiResponse<?> findAcctWalletById(Long walletId);
	
	ResponseEntity<?> getListCommissionAccount(List<Integer> ids);
	
	ResponseEntity<?> getListWayaAccount();
	
	ResponseEntity<?> getAccountInfo(String accountNo);
	
	ResponseEntity<?> fetchAccountDetail(String accountNo);
	
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
	
	ResponseEntity<?> ToggleAccount(AccountToggleDTO user);
	
	ResponseEntity<?> UserAccountAccess(AdminAccountRestrictionDTO user);
	
	ApiResponse<?> fetchTransaction(String acctNo);
	
	ApiResponse<?> fetchFilterTransaction(String acctNo, Date fromdate, Date todate);
	
	ApiResponse<?> fetchRecentTransaction(Long user_id);
	
	ResponseEntity<?> getListWalletAccount();
	
	ResponseEntity<?> AccountAccessDelete(UserAccountDelete user);
	
	ResponseEntity<?> AccountAccessPause(AccountFreezeDTO user);
	
	ResponseEntity<?> AccountAccessClosure(AccountCloseDTO user);
	
	ResponseEntity<?> AccountAccessLien(AccountLienDTO user);
	
	ResponseEntity<?> getAccountSimulated(Long user_id);
	
	ResponseEntity<?> getListSimulatedAccount();

}
