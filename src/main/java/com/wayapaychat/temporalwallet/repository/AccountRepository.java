package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountNo(String accountNo);
}
