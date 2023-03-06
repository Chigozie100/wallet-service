package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.UserPricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPricingRepository extends JpaRepository<UserPricing, Long> {

    List<UserPricing> findByProduct(String product);

    @Query("select u from UserPricing u where u.userId = :#{#userId} and u.code = :#{#code}")
    Optional<UserPricing> findDetails(@Param("userId") long userId, @Param("code") String code);

    @Query("select u from UserPricing u where u.userId = :#{#userId} and u.code = :#{#code}")
    Optional<UserPricing> findDetailsByCode(@Param("userId") long userId, @Param("code") String code);

    
    @Query("select u from UserPricing u where u.userId = :#{#userId} and u.code = :#{#code}")
    List<UserPricing> findDetailsByCode2(@Param("userId") long userId, @Param("code") String code);
 
    @Query("select u from UserPricing u where u.code = :#{#code}")
    List<UserPricing> getAllDetailsByCode(@Param("code") String code);
 

    @Query("select u from UserPricing u where u.userId = :userId")
    List<UserPricing> findByUserId(@Param("userId") long userId);

    @Query("SELECT u.product FROM UserPricing u GROUP BY u.product")
    List<String> advancedSearch();
 

    @Query("select distinct count(a.id) from UserPricing a")
    long countProducts();
    
    //@Query(value = "SELECT u FROM CorporateUser u WHERE (UPPER(u.email) = UPPER(:value) OR "
		//	+ "u.phoneNumber LIKE CONCAT('%', :value)) AND u.isDeleted = false")
        //SELECT * FROM "m_wallet_product" WHERE CAST("del_flg" AS text) LIKE '%41%'
    @Query("SELECT u FROM UserPricing u WHERE UPPER(u.fullName) LIKE CONCAT('%', :value)")
	List<UserPricing> findByFullNameContaining(String value);

    List<UserPricing> findByFullNameLike(String fullName);

    @Query("SELECT m FROM UserPricing m WHERE m.fullName LIKE %:fullName%")
    List<UserPricing> searchByFullNameLike(@Param("fullName") String fullName);
    
}
