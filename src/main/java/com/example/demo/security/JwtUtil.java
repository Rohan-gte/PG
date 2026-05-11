package com.example.demo.security;

import com.example.demo.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtUtil(JwtProperties props) {
        this.props = props;
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generate(Long userId, String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpirationMs());
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("uid", userId, "role", role))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return props.getExpirationMs();
    }
}
