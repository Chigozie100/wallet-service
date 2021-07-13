package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletGLAccount;

@Repository
public interface WalletGLAccountRepository extends JpaRepository<WalletGLAccount, Long> {
	
	WalletGLAccount findByGlSubHeadCode(String glcode);
}
