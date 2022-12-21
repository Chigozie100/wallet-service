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
 
    @Query("select u from UserPricing u where u.code = :#{#code}")
    List<UserPricing> getAllDetailsByCode(@Param("code") String code);
 

    @Query("select u from UserPricing u where u.userId = :userId")
    List<UserPricing> findByUserId(@Param("userId") long userId);
}
