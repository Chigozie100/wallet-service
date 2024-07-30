//package com.wayapaychat.temporalwallet.security;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import com.wayapaychat.temporalwallet.config.SecurityConstants;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
//
//import com.wayapaychat.temporalwallet.SpringApplicationContext;
//import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
//import com.wayapaychat.temporalwallet.service.GetUserDataService;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class AuthorizationFilter extends BasicAuthenticationFilter {
//
//    public AuthorizationFilter(AuthenticationManager authManager) {
//        super(authManager);
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws IOException, ServletException {
//
//        String header = req.getHeader(SecurityConstants.HEADER_STRING);
//        if (header == null) {
//            log.info("Authorization header not found, proceeding with chain");
//            chain.doFilter(req, res);
//            return;
//        }
//        String clientId = req.getHeader(SecurityConstants.CLIENT_ID);
//        if (clientId == null) {
//            log.info("Client ID header not found, proceeding with chain");
//            chain.doFilter(req, res);
//            return;
//        }
//        String clientType = req.getHeader(SecurityConstants.CLIENT_TYPE);
//        if (clientType == null) {
//            log.info("Client Type header not found, proceeding with chain");
//            chain.doFilter(req, res);
//            return;
//        }
//
//        GetUserDataService authProxy = (GetUserDataService) SpringApplicationContext.getBean("getUserDataService");
//        log.info("Sending request to authProxy for user data retrieval");
//        TokenCheckResponse tokenResponse = authProxy.getUserData(header, clientId, clientType);
//        log.info("Received response from authProxy: {}", tokenResponse);
//
//        if (tokenResponse.isStatus()) {
//            List<GrantedAuthority> grantedAuthorities = tokenResponse.getData().getRoles().stream().map(r -> {
//                log.info("Privilege List::: {}", r);
//                return new SimpleGrantedAuthority(r);
//            }).collect(Collectors.toList());
//
//            SecurityContextHolder.getContext().setAuthentication(
//                    new UsernamePasswordAuthenticationToken(tokenResponse.getData(), null, grantedAuthorities)
//            );
//        }
//
//        chain.doFilter(req, res);
//
//    }
//}
