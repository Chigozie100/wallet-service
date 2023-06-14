package com.wayapaychat.temporalwallet.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.response.ApiResponse;

import javax.servlet.http.HttpServletRequest;

public interface UserAccountService {

    ResponseEntity<?> createUser(UserDTO user, String token);

    WalletAccount createNubanAccount(WalletUserDTO user);

    ResponseEntity<?> createUserAccount(WalletUserDTO user, String token);

    ResponseEntity<?> createCashAccount(WalletCashAccountDTO user);

    ResponseEntity<?> createEventAccount(WalletEventAccountDTO user);

    ResponseEntity<?> createAccount(AccountPojo2 accountPojo, String token);

    ResponseEntity<?> createOfficialAccount(OfficialAccountDTO account);

    ArrayList<Object> createOfficialAccount(List<OfficialAccountDTO> account);

    ResponseEntity<?> createAccountProduct(AccountProductDTO accountPojo);

    ApiResponse<?> findCustWalletById(Long walletId);

    ApiResponse<?> findAcctWalletById(Long walletId);

    ResponseEntity<?> getListCommissionAccount(List<Integer> ids);

    ResponseEntity<?> getListWayaAccount();

    ResponseEntity<?> getAccountInfo(String accountNo);

    ResponseEntity<?> getAccountInfoWithUserInfo(String accountNo);

    ResponseEntity<?> fetchAccountDetail(String accountNo, Boolean isAdmin);

    ResponseEntity<?> fetchVirtualAccountDetail(String accountNo);

    ResponseEntity<?> getUserAccountList(long userId, String token);

    ResponseEntity<?> getAllAccount();

    ResponseEntity<?> getUserCommissionList(long userId, Boolean isAdmin);

    ResponseEntity<?> makeDefaultWallet(String accountNo);

    ResponseEntity<?> UserWalletLimit(long userId);

    ResponseEntity<?> getALLCommissionAccount();

    ResponseEntity<?> getAccountCommission(String accountNo);

    ResponseEntity<?> getAccountDetails(String accountNo, Boolean isAdmin) throws Exception;

    ResponseEntity<?> nameEnquiry(String accountNo);

    ResponseEntity<?> getAccountDefault(Long user_id) throws JsonProcessingException;

    ResponseEntity<?> searchAccount(String search);

    ResponseEntity<?> modifyUserAccount(UserAccountDTO user);

    ResponseEntity<?> createNubbanAccountAuto();

    ResponseEntity<?> ToggleAccount(AccountToggleDTO user);

    ResponseEntity<?> UserAccountAccess(AdminAccountRestrictionDTO user);

    ApiResponse<?> fetchTransaction(String acctNo);

    ApiResponse<?> fetchFilterTransaction(String acctNo, Date fromdate, Date todate);

    ApiResponse<?> fetchRecentTransaction(Long user_id);

    ResponseEntity<?> getListWalletAccount();

    ResponseEntity<?> AccountAccessDelete(UserAccountDelete user);

    ResponseEntity<?> AccountAccessPause(AccountFreezeDTO user);

    ResponseEntity<?> AccountAccessBlockAndUnblock(AccountBlockDTO user, HttpServletRequest request);

    ResponseEntity<?> AccountAccessClosure(AccountCloseDTO user);

    ResponseEntity<?> AccountAccessClosureMultiple(List<AccountCloseDTO> user);

    ResponseEntity<?> AccountAccessLien(AccountLienDTO user);

    ResponseEntity<?> getAccountSimulated(Long user_id);

    ResponseEntity<?> getListSimulatedAccount();

    ResponseEntity<?> getUserAccountCount(Long userId);

    ResponseEntity<?> ListUserAccount(long userId);

    ResponseEntity<?> AccountLookUp(String account, SecureDTO secureKey);

    ResponseEntity<?> getTotalActiveAccount();

    ResponseEntity<?> countActiveAccount();

    ResponseEntity<?> countInActiveAccount();

    ResponseEntity<?> createDefaultWallet(MyData user, String token);

    ResponseEntity<?> updateCustomerDebitLimit(String userId, BigDecimal amount);

    void securityCheck(long userId);

    ResponseEntity<?> createExternalAccount(String accountNumber);

    ResponseEntity<?> updateNotificationEmail(String accountNumber, String email);

    void setupSystemUser();

    void setupExternalCBA();

    ResponseEntity<?>  setupAccountOnExternalCBA(String accounNumber);

    ApiResponse<?> getAllAccounts(int page, int size, String filter, LocalDate fromdate, LocalDate todate);

    ApiResponse<?> toggleTransactionType(long userId, String type, String token);

    ApiResponse<?> transTypeStatus(long userId);
    
    ApiResponse<?> totalTransactionByUserId(Long user_id, boolean filter, LocalDate fromdate, LocalDate todate);

    ApiResponse<?> fetchAllUsersTransactionAnalysis();

    ApiResponse<?> fetchUserTransactionStatForReferral(String user_id, String accountNo);
    
    ResponseEntity<?> updateAccountDescription(String accountNo, String token, String description);

}
