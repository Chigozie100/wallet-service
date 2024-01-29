package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.FraudRule;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.FraudRequest;
import com.wayapaychat.temporalwallet.repository.FraudRuleRepository;
import com.wayapaychat.temporalwallet.service.IFraudRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FraudRuleServiceImpl implements IFraudRuleService {

    private final FraudRuleRepository ruleRepository;

    @Autowired
    public FraudRuleServiceImpl(FraudRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public FraudRule creat(FraudRequest request) {
        log.info("Creating fraud rule with name: {}", request.getName());
        FraudRule rule = new FraudRule();
        rule.setAccountType(request.getAccountType());
        rule.setDescription(request.getDescription());
        rule.setName(request.getName());
        try {
            FraudRule savedRule = ruleRepository.save(rule);
            log.info("Fraud rule created successfully");
            return savedRule;
        } catch (Exception ex) {
            log.error("Error creating fraud rule: {}", ex.getMessage());
            throw new CustomException("Error creating fraud rule", HttpStatus.EXPECTATION_FAILED);
        }
    }
}
