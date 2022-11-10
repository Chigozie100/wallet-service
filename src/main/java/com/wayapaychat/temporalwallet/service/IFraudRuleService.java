package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.entity.FraudRule;
import com.wayapaychat.temporalwallet.pojo.FraudRequest;

public interface IFraudRuleService {
    FraudRule creat(FraudRequest request);
}
