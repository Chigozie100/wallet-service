package com.wayapaychat.temporalwallet.config;


import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.security.JwtTokenHelper;
import com.wayapaychat.temporalwallet.util.Constant;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private ProfileDetailsService userDetailsService;

    @Autowired
    private JwtTokenHelper jwtTokenUtil;

    @Value("${virtualAccount.apikey}")
    private String APIKey;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        try{
            System.out.println("HERE WE HAVE THE MONEY");
            String clientId = req.getHeader(SecurityConstants.CLIENT_ID);
            if (clientId == null) {
                log.info("Client ID header not found, proceeding with chain");
                log.error("no clientId provided");
                chain.doFilter(req, res);
                return;
            }
            String clientType = req.getHeader(SecurityConstants.CLIENT_TYPE);
            if (clientType == null) {
                log.info("Client Type header not found, proceeding with chain");
                chain.doFilter(req, res);
                return;
            }

            String apiKey = req.getHeader("API-KEY");
            if (apiKey != null) {
                // If API key is present, validate it and skip token authentication
                if (validateApiKey(apiKey)) {
                    // If API key is valid, set an authenticated user context
                    log.info("Valid API key, setting authentication context");
                    List<GrantedAuthority> grantedAuthorities = List.of(new SimpleGrantedAuthority("ROLE_API_KEY_USER"));
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken("apiKeyUser", null, grantedAuthorities)
                    );
                    chain.doFilter(req, res);
                    return;
                } else {
                    // If API key is invalid, return 401 Unauthorized
                    log.warn("Invalid API key");
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    chain.doFilter(req, res);
                    return;
                }
            }


            String authToken = getAuthToken(req.getHeader(Constant.HEADER_STRING)).orElse(null);

            String username = getUsername(authToken,clientId,clientType).orElse(null);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("Wha are you doing here name {} context {} ",username,SecurityContextHolder.getContext().getAuthentication());
                UserDetails userDetails = userDetailsService.loadUserByUsername(authToken,clientId,clientType);

                if (Boolean.TRUE.equals(jwtTokenUtil.isValidToken(authToken, userDetails))) {
                    UsernamePasswordAuthenticationToken authentication = jwtTokenUtil.getAuthentication(userDetails);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    req.setAttribute(Constant.USERNAME, username);
                    req.setAttribute(Constant.TOKEN, authToken);
                }
            }

            chain.doFilter(req, res);

        } catch (Exception e) {
            log.error("Error in JwtAuthenticationFilter: {}", e.getMessage());
            // Optionally, add more custom error handling logic here
        }
    }

    private Optional<String> getAuthToken(String header){
        if (!Objects.isNull(header) && header.startsWith(Constant.TOKEN_PREFIX)) {
            return Optional.of(header);
        }
        return Optional.ofNullable(header);
    }

    private Optional<String> getUsername(String authToken, String clientId, String clientType){
        if (!Objects.isNull(authToken)){
            try {
                Optional<UserDetail> userDetailOptional = jwtTokenUtil.getUserDetail(authToken,clientId,clientType);
                return userDetailOptional.map(userDetail -> jwtTokenUtil.getUsernameFromToken(String.valueOf(userDetail)));
            } catch (IllegalArgumentException e) {
                log.error("an error occurred during getting username fromUser token", e);
            } catch (ExpiredJwtException e) {
                log.warn("the token is expired and not valid anymore "+ e.getMessage());
            } catch (SignatureException e) {
                log.warn("Authentication Failed. Username or Password not valid. "+e.getMessage());
            } catch (MalformedJwtException e) {
                log.warn("Malformed token."+e.getMessage());
            }
        }

        return Optional.empty();
    }

    private boolean validateApiKey(String apiKey) {
        // Implement your API key validation logic here
        // For example, check if the API key is present in a list of valid keys
        // or query a database/service to validate the key
        List<String> validApiKeys = Arrays.asList(APIKey, "valid-api-key-2");
        return validApiKeys.contains(apiKey);
    }
}