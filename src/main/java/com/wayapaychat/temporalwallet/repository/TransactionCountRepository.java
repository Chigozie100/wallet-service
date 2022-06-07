package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.TransactionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCountRepository extends JpaRepository<TransactionCount, Long> {

    @Override
    long count();

    @Query("select count(t.id) from TransactionCount t where t.userId =:userId")
    long countByUserId(String userId);
}
