package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletUser;

@Repository
public interface WalletUserRepository extends JpaRepository<WalletUser, Long> {
	
	WalletUser findByEmailAddress(String email);
	
	WalletUser findByUserId(Long userId);
	
	WalletUser findByMobileNo(String phone);
	
	WalletUser findByAccount(WalletAccount account);
	
	@Query(value = "SELECT u FROM WalletUser u " + "WHERE (UPPER(u.emailAddress) = UPPER(:value) OR "
			+ "u.mobileNo LIKE CONCAT('%', :value)) AND u.del_flg = false")
	Optional<WalletUser> findByEmailOrPhoneNumber(@Param("value") String value);

}
