package com.example.app.security;

import com.example.app.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode("6QJOXo2Dj6/zCBjYBJvrsyR1AMWht1tgVu9yBRJ/ijQ="));
    private static final long EXPIRATION_TIME = 86400000;

    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRole().name())
                .claim("userId", user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static UUID extractUserId(String token) {
        Claims claims = extractClaims(token);
        String userId = claims.get("userId", String.class);
        return UUID.fromString(userId);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (SignatureException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public static List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = extractClaims(token);
        String role = claims.get("roles", String.class);
        return List.of(new SimpleGrantedAuthority(role));
    }

    public Authentication getAuthentication(String token, UserDetails userDetails) {
        var authorities = getAuthoritiesFromToken(token);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    public static void decodeToken(String token, String secretKey) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        System.out.println("Roles: " + claims.get("roles"));
    }

    public static JwtDecoder getJwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(SECRET_KEY).build();
    }
}

