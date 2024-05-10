package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.VirtualAccountHook;
import com.wayapaychat.temporalwallet.entity.VirtualAccountTransactions;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccountTransactions, Long> {

    Optional<VirtualAccountHook> findByUsernameAndPassword(String username, String password);

    @Query("SELECT v FROM VirtualAccountTransactions v " + "WHERE UPPER(v.acctNum) = UPPER(:accountNo) " + " AND v.del_flg = false" + " AND v.tranDate BETWEEN  (:fromtranDate)" + " AND (:totranDate)" + " order by v.tranDate DESC ")
    List<VirtualAccountTransactions> findByOfficialAccount(LocalDate fromtranDate, LocalDate totranDate, String accountNo);

}
