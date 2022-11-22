package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.response.ApiResponse;

import javax.servlet.http.HttpServletRequest;

public interface IPostPaymentService {
    ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer);
}
