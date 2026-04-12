package com.taskflow.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    private static final String SECRET = System.getenv("JWT_SECRET") != null
            ? System.getenv("JWT_SECRET")
            : "supersecretkeysupersecretkeysupersecretkey";

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(UUID userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("user_id", userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    public static String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public static UUID extractUserId(String token) {
        String id = (String) getClaims(token).get("user_id");
        return UUID.fromString(id);
    }

    public static boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}