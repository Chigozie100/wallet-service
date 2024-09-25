package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.VirtualAccountSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountSettingRepository extends JpaRepository<VirtualAccountSettings, Long> {

    @Query("SELECT v FROM VirtualAccountSettings v WHERE v.businessId =:businessId")
    Optional<VirtualAccountSettings> findByBusinessId(String businessId);

    @Query("SELECT v FROM VirtualAccountSettings v WHERE v.accountNo =:accountNo")
    Optional<VirtualAccountSettings> findByAccountNo(String accountNo);


    @Query("SELECT v FROM VirtualAccountSettings v WHERE v.accountNo =:accountNo OR v.callbackUrl =:callbackUrl")
    Optional<VirtualAccountSettings> findByAccountNoORCallbackUrl(String accountNo, String callbackUrl);
}
