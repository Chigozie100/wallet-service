package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.VirtualAccountHook;
import com.wayapaychat.temporalwallet.entity.VirtualAccountTransactions;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccountTransactions, Long> {

    @Query("SELECT v FROM VirtualAccountTransactions v " + "WHERE UPPER(v.creditAccountNumber) = UPPER(:accountNo) "+" AND v.createdAt BETWEEN  (:fromtranDate)" + " AND (:totranDate)" + " order by v.createdAt DESC ")
    Page<VirtualAccountTransactions> findAllByDateRange(LocalDateTime fromtranDate, LocalDateTime totranDate, String accountNo, Pageable pageable);


}
