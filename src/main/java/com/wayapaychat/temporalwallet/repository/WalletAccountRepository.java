package com.wayapaychat.temporalwallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletUser;

@Repository
public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {
	
	WalletAccount findByAccountNo(String accountNo);

    List<WalletAccount> findByUser(WalletUser user);

}
