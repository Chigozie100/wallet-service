package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.Users;
import com.wayapaychat.temporalwallet.enumm.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {
    Optional<Accounts> findByAccountNo(String accountNo);

    List<Accounts> findByUser(Users user);

    Accounts findByIsDefault(boolean b);
    
    Optional<Accounts> findByIdAndUser(Long id, Users user);

    Accounts findByUserAndAccountType(Users user, AccountType commission);

    Accounts findByUserAndIsDefault(Users toUser, boolean b);

    Accounts findByIsDefaultAndUser(boolean b, Users user);
}
