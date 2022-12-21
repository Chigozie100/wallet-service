package com.wayapaychat.temporalwallet.security;

import com.wayapaychat.temporalwallet.pojo.MyData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;


import java.io.Serializable;
import java.security.Key;
import java.util.*;
import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenHelper implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    @Value("${jwt.secret}")
    private String secret;
    private Key key;


    /**
     * retrieve username from jwt token
     * @param userDetail userObject from authService
     * @return subject
     */
    public String getUsernameFromToken(MyData userDetail) {
//        return getClaimFromToken(token, Claims::getSubject);
        return String.valueOf(userDetail.getId());
    }

    /**
     * retrieve expiration date from jwt token
     * @param token jwtToken
     * @return Date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Get Claim From Token
     * @param token jwt
     * @param claimsResolver claimResolver
     * @param <T> Generic class
     * @return Generic class
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        System.out.println("claims getClaimFromToken ::: " + claims);
        return claimsResolver.apply(claims);
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims getAllClaimsFromToken(String token) {
        key = getSigningKey();
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * check if the token has expired
     * @param token jwt
     * @return boolean
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * validate token
     * @param token jwt from user
     * @param userDetails Spring Security UserDetail
     * @return true or false
     */
    public Boolean isValidToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken(Date expiration) {
        return Jwts.builder()
                .setSubject("olutimedia@gmail.com")
                .claim("ROLE", "admin")
                .signWith(SignatureAlgorithm.HS256, secret)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiration)
                .compact();
    }

    /**
     * Generate Authentication
     * @param userDetails
     * @return
     */
    public UsernamePasswordAuthenticationToken getAuthentication(final UserDetails userDetails) {

        final Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}