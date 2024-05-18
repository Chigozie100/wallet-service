package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtualServiceImplTest {

    private VirtualAccountRequest accountRequest = null;

    @BeforeAll
    void setUp(){
        accountRequest = new VirtualAccountRequest();
        accountRequest.setAccountName("4332454467");
        accountRequest.setEmail("agbe.terseer@gmail.com");
        accountRequest.setPhoneNumber("2347030355396");
        accountRequest.setUserId("9");
        accountRequest.setBvn("23834232342");
        accountRequest.setNin("7847482323");
    }

    @Test
    void createVirtualAccountVersion2() {
        

    }
}