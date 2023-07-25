package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.AccountingEntryMap;

@Repository
public interface AccountingEntryMapRepository extends JpaRepository<AccountingEntryMap, Long> {


}
