package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransAccountRepository extends JpaRepository<WalletTransAccount, Long> {

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' ")
    BigDecimal findByAllBillsTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' ")
    BigDecimal findByAllOutboundExternalTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'WAYATRAN' ")
    BigDecimal findByAllOutboundInternalTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' ")
    BigDecimal findByAllInboundTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u ")
    BigDecimal totalTransactionAmount();

    @Query("SELECT sum(u.chargeAmount) FROM WalletTransAccount u ")
    BigDecimal totalRevenueAmount();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'PAYSTACK_FUNDING' ")
    BigDecimal findByAllPaystackTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.creditAccountNumber = (:accountNumber) "
            + " OR u.debitAccountNumber = (:accountNumber)")
    BigDecimal findByAllTransactionByUser(String accountNumber);

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' ")
    long countBillsTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.debitAccountNumber = (:accountNumber) "
            + " AND u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' ")
    BigDecimal findBillsTransactionByUser(String accountNumber);
    
     @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.debitAccountNumber = (:accountNumber) "
            + " AND u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' ")
    BigDecimal findNipInboundByUser(String accountNumber);
    
    
    @Query("SELECT sum(u.chargeAmount) FROM WalletTransAccount u WHERE u.debitAccountNumber = (:accountNumber) ")
    BigDecimal totalRevenueAmountByUser(String accountNumber);
    
    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' "
            + "AND u.creditAccountNumber = (:accountNumber) ")
    BigDecimal findByAllInboundTransactionByUser(String accountNumber);
}
