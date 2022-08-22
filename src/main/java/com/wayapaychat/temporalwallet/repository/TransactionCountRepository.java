package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.dto.TransactionCountDto;
import com.wayapaychat.temporalwallet.entity.TransactionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCountRepository extends JpaRepository<TransactionCount, Long> {

    @Override
    long count();

    @Query("select count(t.id) from TransactionCount t where t.userId =:userId")
    long countByUserId(String userId);

    @Query("select count(t.id) as totalCount, t.userId from TransactionCount t group by count(t.id), t.userId")
    List<TransactionCount> getAllBy();

    @Query("SELECT new com.wayapaychat.temporalwallet.dto.TransactionCountDto(t.userId, COUNT(t.id)) " +
            "FROM TransactionCount t  GROUP BY  t.userId")
    List<TransactionCountDto> findSurveyCount();


    // "select distinct c, count(part.id) from TestCategory c left join c.parts part group by c"
}
