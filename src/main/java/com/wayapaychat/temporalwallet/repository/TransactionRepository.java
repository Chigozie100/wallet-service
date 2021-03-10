package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
