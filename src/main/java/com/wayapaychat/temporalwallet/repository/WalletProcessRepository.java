package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletProcess;

@Repository
public interface WalletProcessRepository extends JpaRepository<WalletProcess, Long> {

    WalletProcess findFirstByProcessNameOrderByIdDesc(String processName);

}
