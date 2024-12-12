package com.wayapaychat.temporalwallet.config;

import com.wayapaychat.temporalwallet.security.JwtTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    public JwtAuthenticationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Autowired
    private JwtTokenHelper jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
//
//        String header = req.getHeader(Constants.HEADER_STRING);
//        if (header == null) {
//            chain.doFilter(req, res);
//            return;
//        }
//        String clientId = req.getHeader(Constants.CLIENT_ID);
//        if (clientId == null) {
//            chain.doFilter(req, res);
//            return;
//        }
//        String clientType = req.getHeader(Constants.CLIENT_TYPE);
//        if (clientType == null) {
//            chain.doFilter(req, res);
//            return;
//        }
//
//        Optional<AuthResponse> tokenResponse = jwtTokenUtil.getUserDetail(header,clientId,clientType);
//        if(tokenResponse.isPresent() && tokenResponse.get().getStatus()){
//            List<GrantedAuthority> grantedAuthorities = tokenResponse.get().getData().getRoles().stream().map(r -> {
//                log.info("Privilege List::: {}, {} and {}", r, 2, 3);
//                return new SimpleGrantedAuthority(r);
//            }).collect(Collectors.toList());
//
//            SecurityContextHolder.getContext().setAuthentication(
//                    new UsernamePasswordAuthenticationToken(tokenResponse.get().getData(), null, grantedAuthorities)
//            );
//        }

        chain.doFilter(req, res);
    }


}