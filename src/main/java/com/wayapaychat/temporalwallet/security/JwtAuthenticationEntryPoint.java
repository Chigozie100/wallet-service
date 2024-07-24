package com.wayapaychat.temporalwallet.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ae) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

    }

//    @Override
//    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException, ServletException {
//        res.setContentType("application/json;charset=UTF-8");
//        res.setStatus(403);
//        res.getWriter().write(JsonBuilder //my util class for creating json strings
//                .put("timestamp", DateGenerator.getDate())
//                .put("status", 403)
//                .put("message", "Access denied")
//                .build());
//    }
}
