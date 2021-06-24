package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.SwitchWallet;

@Repository
public interface SwitchWalletRepository extends JpaRepository<SwitchWallet, Long> {

}
