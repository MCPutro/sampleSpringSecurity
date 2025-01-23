package com.example.productservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    // private String secretKey = "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9x"; // Gantilah dengan key yang lebih aman

    private int expirationInMilliSeconds = 1000 * 60 * 60; // 1 detik * 60 * 60 = 1 jam


    // Fungsi untuk mendapatkan kunci yang digunakan untuk memverifikasi tanda tangan token
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Fungsi untuk mendapatkan kunci yang digunakan untuk memverifikasi tanda tangan token
    private SecretKey getSigningKey2() {
        return new SecretKeySpec(secretKey.getBytes(), "HMACSHA256");
    }

    // Generate JWT token
    public String generateToken(String username, List<String> authorities) {
        return Jwts.builder()
                .subject(username)
//                .claim("authorities", String.join(",", authorities))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationInMilliSeconds)) // 1 jam
                .signWith(getSigningKey2())
                .compact();
    }

    // Fungsi untuk memvalidasi token JWT
    public boolean validateToken(String token) {
        try {
            // Mengurai token JWT dan memverifikasi tanda tangan
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey2())// Tentukan kunci untuk memverifikasi token
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            ;

            // Cek apakah token sudah kedaluwarsa
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                System.out.println("Token sudah kedaluwarsa.");
                return false;
            }

//            //cek user
//            if(!claims.getSubject().equals(subject)){
//                System.out.println("subject gak cocok.");
//                return false;
//            }

//            Object authorities = claims.get("authorities");
//            System.out.println("JwtUtil() "+authorities);
//
//            this.userId = claims.getSubject();
            // Jika token valid dan tidak kedaluwarsa, kembalikan true
            return true;
        } catch (SignatureException e) {
            System.out.println("Tanda tangan token tidak valid.");
        } catch (MalformedJwtException e) {
            System.out.println("Token tidak valid.");
        } catch (ExpiredJwtException e) {
            System.out.println("Token sudah kedaluwarsa.");
        } catch (UnsupportedJwtException e) {
            System.out.println("Token tidak didukung.");
        } catch (IllegalArgumentException e) {
            System.out.println("Token kosong atau tidak valid.");
        }

        return false;
    }

    //    // Fungsi untuk mendapatkan klaim dari token JWT
//    public Claims getClaims(String token) {
//        return Jwts.parser()
//                .verifyWith(getSigningKey2())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
}

