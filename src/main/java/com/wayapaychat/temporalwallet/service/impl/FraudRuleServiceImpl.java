package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.FraudRule;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.FraudRequest;
import com.wayapaychat.temporalwallet.repository.FraudRuleRepository;
import com.wayapaychat.temporalwallet.service.IFraudRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FraudRuleServiceImpl implements IFraudRuleService {

    private final FraudRuleRepository ruleRepository;

    @Autowired
    public FraudRuleServiceImpl(FraudRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public FraudRule creat(FraudRequest request) {
        FraudRule rule = new FraudRule();
        rule.setAccountType(request.getAccountType());
        rule.setDescription(request.getDescription());
        rule.setName(request.getName());
        try{

            return null;
        }catch (Exception ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }
}
