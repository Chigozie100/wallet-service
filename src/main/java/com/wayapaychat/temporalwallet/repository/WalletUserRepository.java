package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletUser;

@Repository
public interface WalletUserRepository extends JpaRepository<WalletUser, Long> {
	
	WalletUser findByEmailAddress(String email);
	WalletUser findByUserId(Long userId);
	WalletUser findByMobileNo(String phone);

}
