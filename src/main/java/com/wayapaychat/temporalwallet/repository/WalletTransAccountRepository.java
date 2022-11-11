package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.WalletTransAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransAccountRepository extends JpaRepository<WalletTransAccount,Long> {

}
