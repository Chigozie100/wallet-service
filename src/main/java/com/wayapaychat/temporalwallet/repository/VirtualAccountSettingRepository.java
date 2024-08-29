package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.VirtualAccountSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountSettingRepository extends JpaRepository<VirtualAccountSettings, Long> {

    @Query("SELECT v FROM VirtualAccountSettings v WHERE v.merchantId =:merchantId")
    Optional<VirtualAccountSettings> findByMerchantId(Long merchantId);
}
