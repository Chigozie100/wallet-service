package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.UserPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPricingRepository extends JpaRepository<UserPricing, Long> {

//    @Override
//    Optional<UserPricing> findByUserIdAndProduct(Long aLong);

    @Query("select u from UserPricing u where u.userId = :#{#userId} and u.product = :#{#product}")
    Optional<UserPricing> findDetails(@Param("userId") long userId, @Param("product") String product);
}
