package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransAccountRepository extends JpaRepository<WalletTransAccount, Long> {

    Optional<List<WalletTransAccount>> findByTranId(String referenceNumber);
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
    
      @Query("SELECT sum(u.chargeAmount) FROM WalletTransAccount u WHERE u.status = 'SUCCESSFUL' "
              + "AND CAST(u.createdAt AS date)  BETWEEN(:fromtranDate) AND (:totranDate)")
    BigDecimal totalRevenueAmountFilter(Date fromtranDate, Date totranDate);

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
            + "AND CAST(u.createdAt AS date) BETWEEN (:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllBaxiTransactionByDate(Date fromtranDate, Date totranDate);
    
     @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'QUICKTELLER' AND u.status = 'SUCCESSFUL' "
            + "AND CAST(u.createdAt AS date) BETWEEN (:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllQuicktellerTransactionByDate(Date fromtranDate, Date totranDate);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'NIP_PAYOUT' "
            + " AND u.status = 'SUCCESSFUL' AND CAST(u.createdAt AS date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllOutboundExternalTransaction(Date fromtranDate, Date totranDate);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'PAYSTACK_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' AND CAST(u.createdAt AS date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllPaystackTransactionByDate(Date fromtranDate, Date totranDate);

    @Query("SELECT count(u.id) FROM WalletTransAccount u WHERE u.eventId = 'NIP_FUNDING' AND "
            + "u.status = 'SUCCESSFUL' AND CAST(u.createdAt AS date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal nipInboundTRansactionByDate(Date fromtranDate, Date totranDate);

    @Query("SELECT sum(u.tranAmount) FROM WalletTransAccount u WHERE u.eventId = 'WAYATRAN' "
            + " AND u.status = 'SUCCESSFUL' AND CAST(u.createdAt AS date) BETWEEN  "
            + "(:fromtranDate) AND (:totranDate) ")
    BigDecimal findByAllOutboundInternalTransactionByDate(Date fromtranDate, Date totranDate);



}
