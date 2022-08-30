package com.wayapaychat.temporalwallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.temporalwallet.entity.WalletEventCharges;

public interface WalletEventRepository extends JpaRepository <WalletEventCharges, Long> {
	
    Optional<WalletEventCharges> findByEventId(String id);

    
    @Query(value = "SELECT u FROM WalletEventCharges u " + "WHERE (u.eventId) = (:account) " + " AND u.crncyCode = (:crny)" + " AND u.del_flg = false")
    Optional<WalletEventCharges> findByEventCurrency(String account, String crny);

    @Query("select w from WalletEventCharges w where w.del_flg =:deFlag")
    List<WalletEventCharges> findByDel_flg(boolean deFlag);
}
