package com.pickleball.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String secretKey;

    @Value("${app.security.jwt.expiration-ms}")
    private long jwtExpiration;

    /**
     * Extract username (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract a specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate token for user
     */
    public String generateToken(Long userId, String email) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        return generateToken(extraClaims, email);
    }

    /**
     * Generate token with extra claims
     */
    public String generateToken(Map<String, Object> extraClaims, String email) {
        return buildToken(extraClaims, email, jwtExpiration);
    }

    /**
     * Generate refresh token (longer expiration)
     */
    public String generateRefreshToken(Long userId, String email) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        // Refresh token valid for 7 days
        return buildToken(extraClaims, email, jwtExpiration * 7);
    }

    /**
     * Generate token for venue staff
     */
    public String generateStaffToken(Long staffId, String username, Long venueId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("staffId", staffId);
        extraClaims.put("venueId", venueId);
        extraClaims.put("type", "STAFF");
        return buildToken(extraClaims, username, jwtExpiration);
    }

    /**
     * Extract staff ID from token
     */
    public Long extractStaffId(String token) {
        return extractClaim(token, claims -> claims.get("staffId", Long.class));
    }

    /**
     * Extract venue ID from token
     */
    public Long extractVenueId(String token) {
        return extractClaim(token, claims -> claims.get("venueId", Long.class));
    }

    /**
     * Check if token is for staff
     */
    public boolean isStaffToken(String token) {
        String type = extractClaim(token, claims -> claims.get("type", String.class));
        return "STAFF".equals(type);
    }

    /**
     * Check if token is valid
     */
    public boolean isTokenValid(String token, String userEmail) {
        final String username = extractUsername(token);
        return (username.equals(userEmail)) && !isTokenExpired(token);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Build JWT token
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get signing key from secret
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get token expiration time in milliseconds
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}
