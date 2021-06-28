package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.Transactions;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, Long> {
	Page<Transactions> findByAccount(Accounts account, Pageable pageable);
	Page<Transactions> findByTransactionType(String transactionType, Pageable pageable);
}
