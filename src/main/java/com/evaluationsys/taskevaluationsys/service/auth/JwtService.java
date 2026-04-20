package com.evaluationsys.taskevaluationsys.service.auth;

import com.evaluationsys.taskevaluationsys.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {


    private static final String SECRET_KEY = "mysecretkey1234567890123456789012";
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 hours

    private final Key key;

    public JwtService() {
        // Convert the string secret to a Key object
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getStaffCode().toString())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}