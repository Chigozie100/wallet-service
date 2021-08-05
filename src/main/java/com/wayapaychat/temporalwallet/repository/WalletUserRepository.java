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
	
	@Query(value = "SELECT u FROM WalletUser u " + "WHERE UPPER(u.emailAddress) = UPPER(:email) " + " AND u.userId = (:userId) " + " AND u.del_flg = false")
	WalletUser findByEmailAddress(String email, Long userId);
	
	WalletUser findByUserId(Long userId);
	
	@Query(value = "SELECT u FROM WalletUser u " + "WHERE UPPER(u.mobileNo) = UPPER(:phone) " + " AND u.userId = (:userId) " + " AND u.del_flg = false")
	WalletUser findByMobileNo(String phone, Long userId);
	
	WalletUser findByAccount(WalletAccount account);
	
	@Query(value = "SELECT u FROM WalletUser u " + "WHERE (UPPER(u.emailAddress) = UPPER(:value) OR "
			+ "u.mobileNo LIKE CONCAT('%', :value)) AND u.del_flg = false")
	Optional<WalletUser> findByEmailOrPhoneNumber(@Param("value") String value);

}
