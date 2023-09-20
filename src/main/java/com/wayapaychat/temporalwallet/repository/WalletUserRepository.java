package com.wayapaychat.temporalwallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletUser;

@Repository("walletUserRepository")
public interface WalletUserRepository extends JpaRepository<WalletUser, Long> {
	
	//@Query(value = "SELECT u FROM WalletUser u " + "WHERE UPPER(u.emailAddress) = UPPER(:email) " + " AND u.userId = (:userId) " + " AND u.del_flg = false")
	WalletUser findByEmailAddress(String email);
	WalletUser findByEmailAddressAndProfileId(String email,String profileId);
	
	WalletUser findByUserIdAndProfileId(Long userId,String profileId);
	
//	@Query(value = "SELECT u FROM WalletUser u " + "WHERE (u.userId) = (:Id) " + " AND u.del_flg = false")
//	Optional<WalletUser> findUserId(Long Id);

	@Query(value = "SELECT u FROM WalletUser u " + "WHERE (u.userId) = (:Id) " + " AND u.del_flg = false AND (u.profileId) = (:profileId) ")
	Optional<WalletUser> findUserIdAndProfileId(Long Id,String profileId);

	List<WalletUser> findAllByUserIdAndProfileId(Long userId,String profileId);
	List<WalletUser> findAllByUserId(Long userId);
	
	@Query(value = "SELECT u FROM WalletUser u " + "WHERE u.isVirtualAccount = false " + " AND u.del_flg = false")
	List<WalletUser> findUserVirtual();

	List<WalletUser> findAllByOrderByIdDesc(Pageable pageable);
	long countByUserIdIsNotNull();
	
	//@Query(value = "SELECT u FROM WalletUser u " + "WHERE UPPER(u.mobileNo) = UPPER(:phone) " + " AND u.userId = (:userId) " + " AND u.del_flg = false")
	WalletUser findByMobileNo(String phone);
	
	WalletUser findByAccount(WalletAccount account);
	WalletUser findByUserIdAndAccount(Long userId,WalletAccount account);
	
	@Query(value = "SELECT u FROM WalletUser u " + "WHERE (UPPER(u.emailAddress) = UPPER(:value) OR  u.mobileNo LIKE CONCAT('%', :value) OR u.userId = (:value) ) AND u.del_flg = false")
	Optional<WalletUser> findByEmailOrPhoneNumberOrUserId(@Param("value") String value);

	@Query(value = "SELECT u FROM WalletUser u " + "WHERE (UPPER(u.emailAddress) = UPPER(:value) OR  u.mobileNo LIKE CONCAT('%', :value)) AND u.del_flg = false")
	Optional<WalletUser> findByEmailOrPhoneNumber(@Param("value") String value);

	@Query(value = "SELECT u FROM WalletUser u " + "WHERE (u.userId) = (:Id) " + " AND u.cust_sex = ('S') " + " AND u.del_flg = false")
	WalletUser findBySimulated(Long Id);
	
	@Query(value = "SELECT u FROM WalletUser u " + "WHERE u.cust_sex = ('S') " + " AND u.del_flg = false")
	List<WalletUser> findBySimulated();

	@Query(value = "SELECT u FROM WalletUser u WHERE u.del_flg = false and (u.userId) = (:userId) ")
	List<WalletUser> findAllWalletByUserId(Long userId);

	@Query(value = "SELECT u FROM WalletUser u WHERE u.del_flg = false AND (u.userId) = (:userId) AND profileId IS NULL ")
	List<WalletUser> findUserIdAndProfileIdIsNull(Long userId);

	List<WalletUser> findAllByUserIdAndProfileIdIsNull(Long userId);

    Optional<WalletUser> findByEmailAddressOrMobileNoAndProfileId(String email,String phone, String profileId);

    //Select CASE WHEN ID = 5 THEN 1 ELSE 2 END as ord, ID FROM tbl ORDER BY ord ASC, ID ASC

}
