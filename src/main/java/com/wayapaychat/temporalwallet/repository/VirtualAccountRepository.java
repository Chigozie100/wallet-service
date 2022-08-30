package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.VirtualAccountHook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccountHook, Long> {
}
