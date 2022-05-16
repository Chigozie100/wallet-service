package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.temporalwallet.entity.WalletTransactionCharge;

public interface WalletTransactionChargeRepository extends JpaRepository <WalletTransactionCharge, Long> {

    WalletTransactionCharge findByChargeName(String chargeName);

}
