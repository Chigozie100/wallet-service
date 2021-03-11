package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {
    Accounts findByAccountNo(String accountNo);
}
