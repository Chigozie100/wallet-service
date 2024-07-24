//package com.wayapaychat.temporalwallet.security;
//
//import java.io.IOException;
//import java.util.Arrays;
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
//        System.out.println("######### request ######### " + req);
//
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
//        String apiKey = req.getHeader("API-Key");
//        if (apiKey != null) {
//            // If API key is present, validate it and skip token authentication
//            if (validateApiKey(apiKey)) {
//                // If API key is valid, set an authenticated user context
//                log.info("Valid API key, setting authentication context");
//                List<GrantedAuthority> grantedAuthorities = List.of(new SimpleGrantedAuthority("ROLE_API_KEY_USER"));
//                SecurityContextHolder.getContext().setAuthentication(
//                        new UsernamePasswordAuthenticationToken("apiKeyUser", null, grantedAuthorities)
//                );
//                chain.doFilter(req, res);
//                return;
//            } else {
//                // If API key is invalid, return 401 Unauthorized
//                log.warn("Invalid API key");
//                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                return;
//            }
//        }
//
//
//        String header = req.getHeader(SecurityConstants.HEADER_STRING);
//        if (header == null) {
//            log.info("Authorization header not found, proceeding with chain");
//            chain.doFilter(req, res);
//            return;
//        }
//
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
//    private boolean validateApiKey(String apiKey) {
//        // Implement your API key validation logic here
//        // For example, check if the API key is present in a list of valid keys
//        // or query a database/service to validate the key
//        List<String> validApiKeys = Arrays.asList("valid-api-key-1", "valid-api-key-2");
//        return validApiKeys.contains(apiKey);
//    }
//}
