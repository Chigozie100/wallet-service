package com.wayapaychat.temporalwallet.repository;

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

    List<WalletAccount> findByUser(WalletUser user);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.acct_name LIKE ('%COMMISSION%')" + " AND u.user = (:user)")
    Optional<WalletAccount> findByAccountUser(WalletUser user);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.bacid = (:placeholder)" + " AND u.acct_crncy_code = (:crncycode)" + " AND u.sol_id = (:solid)")
    Optional<WalletAccount> findByUserPlaceholder(String placeholder, String crncycode, String solid);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.product_code = (:productCode)" + " AND u.acct_name LIKE ('%COMMISSION%')")
    Optional<WalletAccount> findByProductCode(String productCode);
    
    @Query("SELECT u FROM WalletAccount u WHERE u.product_code = (:productCode)" + " AND u.acct_name LIKE ('%COMMISSION%')" + " AND u.accountNo = (:account)")
    Optional<WalletAccount> findByAccountProductCode(String productCode, String account);

}
