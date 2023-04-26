package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransAccountRepository extends JpaRepository<WalletTransAccount, Long> {

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'BILLSPAYMENT' ")
    BigDecimal findByAllBillsTransaction();
    
    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' ")
    BigDecimal findByAllOutboundExternalTransaction();
    
    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u " + "WHERE u. = 'D'" + " AND u.del_flg = false")
    BigDecimal findByAllOutboundInternalTransaction();
    
    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u " + "WHERE u. = 'D'" + " AND u.del_flg = false")
    BigDecimal findByAllInboundTransaction();

}
