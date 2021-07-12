package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletInterest;

@Repository
public interface WalletInterestRepository extends JpaRepository<WalletInterest, Long> {
	
	@Query("SELECT u FROM WalletInterest u " +
            "WHERE UPPER(u.intTblCode) = UPPER(:value) " +
            " AND u.del_flg = false")
    Optional<WalletInterest> findByIntTblCodeIgnoreCase(@Param("value") String value);

}
