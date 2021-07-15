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

}
