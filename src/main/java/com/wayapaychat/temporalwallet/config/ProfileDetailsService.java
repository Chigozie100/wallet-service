package com.wayapaychat.temporalwallet.config;



import com.wayapaychat.temporalwallet.security.JwtTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDetailsService implements UserDetailsService {

    private final JwtTokenHelper jwtTokenHelper;
    private static final String MESSAGE = "Invalid Token provided";


    public UserDetails loadUserByUsername(String token,String clientId, String clientType) {

        if (Objects.isNull(token)){
            throw new UsernameNotFoundException(MESSAGE);
        }
        UserDetail userDetail = jwtTokenHelper.getUserDetail(token,clientId,clientType).orElseThrow(() -> new UsernameNotFoundException(MESSAGE));
        return new ProfileDetails(Collections.singletonList(jwtTokenHelper.getRoleFromToken(userDetail)), jwtTokenHelper.getUsernameFromToken(userDetail));
    }

   @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return null;
    }

//    public UserDetail getUser(String token){
//        if (Objects.isNull(token)){
//            throw new UsernameNotFoundException(MESSAGE);
//        }
//        UserDetail userDetail = jwtTokenHelper.getUserDetail(token).orElseThrow(() -> new UsernameNotFoundException(MESSAGE));
//
//        return userDetail;
//    }

}