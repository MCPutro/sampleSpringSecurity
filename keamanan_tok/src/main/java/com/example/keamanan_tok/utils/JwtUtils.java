package com.example.keamanan_tok.utils;

import com.example.jwt.JwtCore;
import com.example.keamanan_tok.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtUtils {
    private JwtCore jwtCore;

    public JwtUtils() {
        this.jwtCore = new JwtCore("9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9x", 1000 * 60 * 60 );
    }

    public String generateToken(String username, List<String> akses) {
        return this.jwtCore.generateToken(username, akses);
    }

    public boolean validateToken(String token) {
        return this.jwtCore.validateToken(token);
    }

    public String getUserId(String token) {
        return this.jwtCore.getUserId();
    }
}
