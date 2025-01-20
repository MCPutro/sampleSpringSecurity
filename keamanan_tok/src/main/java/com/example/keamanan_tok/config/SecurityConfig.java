package com.example.keamanan_tok.config;

import com.example.keamanan_tok.filter.JwtAuthenticationFilter;
import com.example.keamanan_tok.CustomAuthenticationEntryPoint;
import com.example.keamanan_tok.filter.JwtAuthenticationFilter;
import com.example.keamanan_tok.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService CustomUserDetailsService;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // Mengonfigurasi AuthenticationManager Bean
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.userDetailsService(CustomUserDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder(10)); // Menggunakan BCryptPasswordEncoder

        return authenticationManagerBuilder.build();
    }

    // Mengonfigurasi HttpSecurity dan Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Tidak menggunakan 'and()' lagi, langsung konfigurasi dengan lebih eksplisit
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Menambahkan filter JWT
                .authorizeHttpRequests(req -> req
                        .requestMatchers(
                                "/api/public/**"
                        ).permitAll() // Endpoint yang tidak memerlukan autentikasi

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN") // Endpoint untuk Admin hanya

                        .requestMatchers(
                                "/api/user/**"
                        ).hasAnyRole("USER", "ADMIN") // Endpoint untuk USER dan ADMIN

                        .anyRequest().authenticated() // Semua request lainnya memerlukan autentikasi
                )
                .sessionManagement(sessionManag -> sessionManag.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint))
                .formLogin(AbstractHttpConfigurer::disable) // Menonaktifkan form login
                .httpBasic(AbstractHttpConfigurer::disable) // Menonaktifkan autentikasi HTTP Basic (jika Anda menggunakan token JWT)

        ;
//                .requestMatchers("/api/public/**").permitAll()  // Endpoint yang tidak memerlukan autentikasi
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Endpoint untuk Admin hanya
//                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN") // Endpoint untuk USER dan ADMIN
//                .anyRequest().authenticated()  // Semua request lainnya memerlukan autentikasi
//                .formLogin().disable()  // Menonaktifkan form login
//                .httpBasic().disable(); // Menonaktifkan autentikasi HTTP Basic (jika Anda menggunakan token JWT)

        return http.build();  // Mengembalikan konfigurasi SecurityFilterChain
    }
}
