package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransAccountRepository extends JpaRepository<WalletTransAccount, Long> {

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' AND u.status = 'SUCCESSFUL'")
    BigDecimal findByAllBillsTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' AND "
            + "u.status = 'SUCCESSFUL'")
    BigDecimal findByAllOutboundExternalTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'WAYATRAN' AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findByAllOutboundInternalTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findByAllInboundTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.status = 'SUCCESSFUL' ")
    BigDecimal totalTransactionAmount();

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.status = 'SUCCESSFUL' ")
    long totalTransaction();

    @Query("SELECT sum(u.chargeAmount) FROM WalletTransAccount u WHERE u.status = 'SUCCESSFUL'")
    BigDecimal totalRevenueAmount();

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.status = 'SUCCESSFUL'")
    long totalRevenue();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'PAYSTACK_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findByAllPaystackTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.creditAccountNumber = (:accountNumber) "
            + " OR u.debitAccountNumber = (:accountNumber) AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findByAllTransactionByUser(String accountNumber);

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' AND "
            + "u.status = 'SUCCESSFUL' ")
    long countBillsTransaction();

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' ")
    long nipInboundCount();

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' AND "
            + "u.status = 'SUCCESSFUL' ")
    long nipOutboundExternalCount();

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'WAYATRAN' AND "
            + "u.status = 'SUCCESSFUL' ")
    long nipOutboundInternalCount();

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'PAYSTACK_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' ")
    long payStackCount();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.debitAccountNumber =:accountNumber "
            + " AND u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findBillsTransactionByUser(String accountNumber);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.debitAccountNumber =:accountNumber "
            + " AND u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findNipInboundByUser(String accountNumber);

    @Query("SELECT sum(u.chargeAmount) FROM WalletTransAccount u WHERE u.debitAccountNumber =:accountNumber AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal totalRevenueAmountByUser(String accountNumber);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' "
            + "AND u.creditAccountNumber =:accountNumber AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findByAllInboundTransactionByUser(String accountNumber);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' "
            + " OR u.eventId = 'BAXI' AND u.status = 'SUCCESSFUL' AND u.createdAt BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllBillsTransactionByDate(LocalDate fromtranDate, LocalDate totranDate);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' "
            + " AND u.status = 'SUCCESSFUL' AND u.createdAt BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllOutboundExternalTransaction(LocalDate fromtranDate, LocalDate totranDate);
    
     @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'PAYSTACK_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' AND u.createdAt BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllPaystackTransactionByDate(LocalDate fromtranDate, LocalDate totranDate);
    
     @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' AND u.createdAt BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal nipInboundTRansactionByDate(LocalDate fromtranDate, LocalDate totranDate);
    
    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'WAYATRAN' "
            + " AND u.status = 'SUCCESSFUL' AND u.createdAt BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllOutboundInternalTransactionByDate(LocalDate fromtranDate, LocalDate totranDate);
    
}
