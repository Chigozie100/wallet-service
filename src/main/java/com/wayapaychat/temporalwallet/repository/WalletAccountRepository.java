package com.wayapaychat.temporalwallet.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletUser;

@Repository
public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {
	
	WalletAccount findByAccountNo(String accountNo);

    WalletAccount findByNubanAccountNo(String nubanAccountNo);

    List<WalletAccount> findByUser(WalletUser user);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.acct_name LIKE ('%COMMISSION%')" + " AND u.user = (:user)")
    Optional<WalletAccount> findByAccountUser(WalletUser user);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.bacid = (:placeholder)" + " AND u.acct_crncy_code = (:crncycode)" + " AND u.sol_id = (:solid)")
    Optional<WalletAccount> findByUserPlaceholder(String placeholder, String crncycode, String solid);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.product_code = (:productCode)" + " AND u.acct_name LIKE ('%COMMISSION%')")
    Optional<WalletAccount> findByProductCode(String productCode);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.product_code = (:productCode)" + " AND u.acct_name LIKE ('%COMMISSION%')" + " AND u.accountNo = (:account)")
    Optional<WalletAccount> findByAccountProductCode(String productCode, String account);

    @Query("SELECT u FROM WalletAccount u WHERE  u.accountNo = (:account) AND u.del_flg = false")
    Optional<WalletAccount> findByAccount(String account);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.user = (:user)" + " AND u.walletDefault = true")
    Optional<WalletAccount> findByDefaultAccount(WalletUser user);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.product_type = ('OAB')" + " AND u.del_flg = false")
    List<WalletAccount> findByWayaAccount();
    
    @Query("SELECT u FROM WalletAccount u WHERE u.product_type != ('OAB')" + " AND u.del_flg = false" + " AND u.accountNo NOT LIKE ('7%')")
    List<WalletAccount> findByWalletAccount();
    
    @Query("SELECT u FROM WalletAccount u WHERE u.accountNo LIKE ('7%')" + " AND u.product_type != ('OAB')" + " AND u.del_flg = false")
    List<WalletAccount> findBySimulatedAccount();
    
    @Query("SELECT u FROM WalletAccount u WHERE u.product_code = (:productCode)" + " AND u.acct_name LIKE ('%COMMISSION%')")
    List<WalletAccount> findByProductList(String productCode);

    @Query("SELECT count(u.id) FROM WalletAccount u WHERE u.del_flg = true")
    long countActiveAccount();

    @Query("SELECT count(u.id) FROM WalletAccount u WHERE u.del_flg = false")
    long countInActiveAccount();

    @Query("SELECT sum(u.clr_bal_amt) FROM WalletAccount u WHERE u.del_flg = false")
    BigDecimal totalActiveAccount();


}
