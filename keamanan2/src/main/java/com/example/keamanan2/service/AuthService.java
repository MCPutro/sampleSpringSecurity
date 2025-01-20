package com.example.keamanan2.service;


import com.example.keamanan2.config.InMemoryUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    @Qualifier("custom_cuy")
    private AuthenticationManager authenticationManager;

    @Autowired
    private InMemoryUserDetailsService userDetailsService;

    public Authentication authenticate(String username, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authentication);
    }

    public void registerUser(String username, String password, String role) {
        userDetailsService.addUser(username, password, role);
    }
}
