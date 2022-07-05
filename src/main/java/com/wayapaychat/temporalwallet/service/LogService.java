package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.pojo.LogRequest;

public interface LogService {
    void saveLog(LogRequest logPojo);
}
