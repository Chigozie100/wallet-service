package com.wayapaychat.temporalwallet.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import org.springframework.http.ResponseEntity;

import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.response.ApiResponse;

import javax.servlet.http.HttpServletRequest;

public interface UserAccountService {

    ResponseEntity<?> createUser(HttpServletRequest request,UserDTO user, String token);

    WalletAccount createNubanAccount(WalletUserDTO user);

    WalletAccount createNubanAccountVersion2(WalletUser user);

    ResponseEntity<?> createUserAccount(String clientId,String clientType,WalletUserDTO user, String token);

    ResponseEntity<?> createCashAccount(HttpServletRequest request,WalletCashAccountDTO user);

    ResponseEntity<?> createEventAccount(WalletEventAccountDTO user);

    ResponseEntity<?> createAccount(HttpServletRequest request,AccountPojo2 accountPojo, String token);

    ResponseEntity<?> createOfficialAccount(OfficialAccountDTO account);

    ArrayList<Object> createOfficialAccount(List<OfficialAccountDTO> account);

    ResponseEntity<?> createAccountProduct(HttpServletRequest request,AccountProductDTO accountPojo);

    ApiResponse<?> findCustWalletById(Long walletId);

    ApiResponse<?> findAcctWalletById(Long walletId);

    ResponseEntity<?> getListCommissionAccount(List<Integer> ids);

    ResponseEntity<?> getListWayaAccount();

    ResponseEntity<?> getAccountInfo(String accountNo);

    ResponseEntity<?> getAccountInfoWithUserInfo(String accountNo);

    ResponseEntity<?> fetchAccountDetail(String accountNo, Boolean isAdmin);

    ResponseEntity<?> fetchUserByAccountNo(String accountNo);

    ResponseEntity<?> fetchVirtualAccountDetail(String accountNo);

    ResponseEntity<?> getUserAccountList(HttpServletRequest request,long userId,String profileId,String token);

    ResponseEntity<?> getAllAccount();

    ResponseEntity<?> getUserCommissionList(long userId, Boolean isAdmin,String profileId);

    ResponseEntity<?> makeDefaultWallet(String accountNo);

    ResponseEntity<?> UserWalletLimit(long userId,String profileId);

    ResponseEntity<?> getALLCommissionAccount();

    ResponseEntity<?> getAccountCommission(String accountNo);

    ResponseEntity<?> getAccountDetails(String accountNo, Boolean isAdmin) throws Exception;

    ResponseEntity<?> nameEnquiry(String accountNo);

    ResponseEntity<?> getAccountDefault(Long user_id,String profileId) throws JsonProcessingException;

    ResponseEntity<?> searchAccount(String search);

    ResponseEntity<?> modifyUserAccount(HttpServletRequest request,UserAccountDTO user);

    ResponseEntity<?> createNubbanAccountAuto();

    ResponseEntity<?> ToggleAccount(HttpServletRequest request,AccountToggleDTO user);

    ResponseEntity<?> UserAccountAccess(AdminAccountRestrictionDTO user);

    ApiResponse<?> fetchTransaction(String acctNo);

    ApiResponse<?> fetchFilterTransaction(String acctNo, Date fromdate, Date todate);

    ApiResponse<?> fetchRecentTransaction(Long user_id,String profileId);

    ResponseEntity<?> getListWalletAccount();

    ResponseEntity<?> AccountAccessDelete(UserAccountDelete user);

    ResponseEntity<?> AccountAccessPause(AccountFreezeDTO user);

    ResponseEntity<?> AccountAccessBlockAndUnblock(AccountBlockDTO user, HttpServletRequest request);

    ResponseEntity<?> AccountAccessClosure(AccountCloseDTO user);

    ResponseEntity<?> AccountAccessClosureMultiple(List<AccountCloseDTO> user);

    ResponseEntity<?> AccountAccessLien(AccountLienDTO user);

    ResponseEntity<?> getAccountSimulated(Long user_id);

    ResponseEntity<?> getListSimulatedAccount();

    ResponseEntity<?> getUserAccountCount(Long userId,String profileId);

    ResponseEntity<?> ListUserAccount(HttpServletRequest request,long userId);

    ResponseEntity<?> AccountLookUp(String account, SecureDTO secureKey);

    ResponseEntity<?> getTotalActiveAccount();

    ResponseEntity<?> countActiveAccount();

    ResponseEntity<?> countInActiveAccount();

    ResponseEntity<?> createDefaultWallet(HttpServletRequest request,MyData user, String token,String profileId);

    ResponseEntity<?> getCustomerDebitLimit(String userId,String profileId);

    ResponseEntity<?> updateCustomerDebitLimit(String userId, BigDecimal amount,String profileId);

    void securityCheck(long userId,String profileId);

    ResponseEntity<?> createExternalAccount(String accountNumber);

    ResponseEntity<?> updateNotificationEmail(String accountNumber, String email);

    void setupSystemUser();

    void setupExternalCBA();

    ResponseEntity<?> setupAccountOnExternalCBA(String accounNumber);

    ApiResponse<?> getAllAccounts(int page, int size, String filter, LocalDate fromdate, LocalDate todate);

    ApiResponse<?> toggleTransactionType(HttpServletRequest request,long userId, String type, String token);

    ApiResponse<?> transTypeStatus(long userId);

    ApiResponse<?> totalTransactionByUserId(Long user_id, boolean filter, LocalDate fromdate, LocalDate todate,String profileId);

    ApiResponse<?> fetchAllUsersTransactionAnalysis();

    ApiResponse<?> fetchUserTransactionStatForReferral(String user_id, String accountNo,String profileId);

    ResponseEntity<?> updateAccountDescription(String accountNo, String token, String description);

    ApiResponse<?> updateAccountName(String accountNo, String token, String name);
    
    ApiResponse<?> createCommisionAccount(long userId,String token,String profileId);

    ApiResponse<?> updateBulkAmount(String accountNumber, String token, double limit);

    WalletUser findUserWalletByEmailAndPhone(String email);
}
