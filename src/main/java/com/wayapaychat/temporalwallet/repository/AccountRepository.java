package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {
    Accounts findByAccountNo(String accountNo);

    List<Accounts> findByUser(Users user);
}
