package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.pojo.LogRequest;

import javax.servlet.http.HttpServletRequest;

public interface LogService {
    void saveLog(LogRequest logPojo, String token, HttpServletRequest request);
}
