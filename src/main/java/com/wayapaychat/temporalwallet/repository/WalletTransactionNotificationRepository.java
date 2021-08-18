package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.temporalwallet.entity.WalletTransactionNotification;

public interface WalletTransactionNotificationRepository extends JpaRepository<WalletTransactionNotification, Long>{

}
