package com.example.transactionservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ini token static, nya harus di set di properties
 */

@Configuration
public class FeignConfigStatic implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                requestTemplate.header("Authorization", authorizationHeader);
            }
        }
    }

//    @Value("${service.jwt.token}")
//    private String jwtToken;
//
//    @Override
//    public void apply(RequestTemplate requestTemplate) {
//        requestTemplate.header("Authorization", "Bearer " + jwtToken);
//    }



}
