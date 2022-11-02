package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface FraudRuleRepository extends JpaRepository<FraudRule,Long> {
}
