package com.example.keamanan_tok.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    private int id;
    private String username;
    private String name;
    private String email;
    private String password;
    private List<String> akses;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Ambil peran pengguna dan konversikan menjadi List<GrantedAuthority>
        List<SimpleGrantedAuthority> authorities = this.getAkses().stream()
                .map(role -> new SimpleGrantedAuthority(role)) // Misalnya, makan, mandi
                .collect(Collectors.toList());
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}

