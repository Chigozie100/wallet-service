package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.pojo.LogRequest;
import com.wayapaychat.temporalwallet.proxy.LogServiceProxy;
import com.wayapaychat.temporalwallet.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogServiceImpl implements LogService {


    private final LogServiceProxy logServiceProxy;

    @Autowired
    public LogServiceImpl(LogServiceProxy logServiceProxy) {
        this.logServiceProxy = logServiceProxy;
    }

    @Override
    public void saveLog(LogRequest logPojo) {
        try {
            logServiceProxy.saveNewLog(logPojo);
        } catch (Exception e) {
            log.error("Error saving Logs:: {}", e.getMessage());
        }
    }
}
