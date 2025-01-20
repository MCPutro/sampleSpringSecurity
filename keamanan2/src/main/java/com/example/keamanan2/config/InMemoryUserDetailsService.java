package com.example.keamanan2.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserDetailsService implements UserDetailsService {
    private final Map<String, UserDetails> users = new HashMap<>();

    public InMemoryUserDetailsService() {
        // Inisialisasi pengguna default
        users.put("admin", User.builder()
                .username("admin")
                .password(new BCryptPasswordEncoder().encode("password"))
                .roles("ADMIN")
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }

    public void addUser(String username, String password, String role) {
        users.put(username, User.builder()
                .username(username)
                .password(new BCryptPasswordEncoder().encode(password))
                .roles(role)
                .build());
    }
}
