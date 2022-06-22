package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.service.CommissionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommissionServiceImpl implements CommissionService {


    private String getUserRole(String userId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        Set<String> roles = authentication.getAuthorities().stream()
//                .map(r -> r.getAuthority()).collect(Collectors.toSet());

        boolean hasUserRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_CORP"));

        if (hasUserRole){
            return "ROLE_CORP";
        }
        return null;
    }
//    private UserType getUserType(String userId, String token) throws Exception {
//        UserProfileResponsePojo userProfile =  getUserProfile(userId, token);
//        for(String role : userProfile.getRoles()) {
//            if(UserType.ROLE_CORP.name().equalsIgnoreCase(role) || UserType.ROLE_CORP_ADMIN.name().equalsIgnoreCase(role)){
//                return UserType.ROLE_CORP ==null ? UserType.ROLE_CORP_ADMIN : UserType.ROLE_CORP;
//            }
//        }
//        return null;
//
//    }
}
