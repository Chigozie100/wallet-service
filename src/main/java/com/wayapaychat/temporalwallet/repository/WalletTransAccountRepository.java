package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransAccountRepository extends JpaRepository<WalletTransAccount, Long> {

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'BAXI' AND u.status = 'SUCCESSFUL'")
    BigDecimal findByAllBaxiTransaction();
    
     @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' AND u.status = 'SUCCESSFUL'")
    BigDecimal findByAllQUICKTELLERTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' AND "
            + "u.status = 'SUCCESSFUL'")
    BigDecimal findByAllOutboundExternalTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'WAYATRAN' AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findByAllOutboundInternalTransaction();

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal findByAllInboundTransaction();


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

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'BAXI' AND "
            + "u.status = 'SUCCESSFUL' ")
    long countBaxiTransaction();
    
     @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' AND "
            + "u.status = 'SUCCESSFUL' ")
    long countQuickTellerTransaction();

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

    @Query("SELECT sum(u.chargeAmount) FROM WalletTransAccount u WHERE u.debitAccountNumber =:accountNumber AND "
            + "u.status = 'SUCCESSFUL' ")
    BigDecimal totalRevenueAmountByUser(String accountNumber);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'BAXI' AND u.status = 'SUCCESSFUL' "
            + "AND u.createdAt BETWEEN (:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllBaxiTransactionByDate(LocalDate fromtranDate, LocalDate totranDate);
    
     @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' AND u.status = 'SUCCESSFUL' "
            + "AND u.createdAt BETWEEN (:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllQuicktellerTransactionByDate(LocalDate fromtranDate, LocalDate totranDate);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' "
            + " AND u.status = 'SUCCESSFUL' AND CAST(u.createdAt as date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllOutboundExternalTransaction(LocalDate fromtranDate, LocalDate totranDate);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'PAYSTACK_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' AND CAST(u.createdAt as date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllPaystackTransactionByDate(LocalDate fromtranDate, LocalDate totranDate);

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' AND CAST(u.createdAt as date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal nipInboundTRansactionByDate(LocalDate fromtranDate, LocalDate totranDate);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'WAYATRAN' "
            + " AND u.status = 'SUCCESSFUL' AND CAST(u.createdAt as date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllOutboundInternalTransactionByDate(LocalDate fromtranDate, LocalDate totranDate);



}
