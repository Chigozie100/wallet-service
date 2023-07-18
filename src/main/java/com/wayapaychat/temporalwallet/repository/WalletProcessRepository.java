package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletProcess;

@Repository
public interface WalletProcessRepository extends JpaRepository<WalletProcess, Long> {

    @Query(nativeQuery = true, value = "SELECT TOP 1 u FROM WalletProcess u "
            + "WHERE UPPER(u.processName) = UPPER(:processName) " + " ORDER BY id DESC ")
    WalletProcess findLastProcessExecuted(String processName);

}
