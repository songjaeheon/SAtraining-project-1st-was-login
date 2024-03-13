package com.footballmania.waslogin.util;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.footballmania.waslogin.service.RedisService;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private String accessTokenRedisPrefix = "REDIS_JWT_";

    private static final long JWT_ACCESS_EXPIRATION_TIME = 3600; // 1 hour
    
    @Value("${jwt_access.secret}")
    private String JWT_ACCESS_SECRET_KEY;

    @Autowired
    private RedisService redisService;

    public long getJwtAccessExpirationTime() {
        return JWT_ACCESS_EXPIRATION_TIME;
    }

    // Generate token for user
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 필요한 정보를 claims에 넣어준다.
        claims.put("email", userDetails.getUsername());
        return createAccessToken(claims, userDetails.getUsername());
    }

    // Create token
    private String createAccessToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        // 1 hour
        Date expiryDate = new Date(now.getTime() + JWT_ACCESS_EXPIRATION_TIME * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(generalKey(JWT_ACCESS_SECRET_KEY))
                .compact();
    }

    // Validate Access token
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(generalKey(JWT_ACCESS_SECRET_KEY)).build().parseClaimsJws(token);
            // access token이 만료되었거나 redis에 존재하면(logout한 token) false
            if(! isAccessTokenExpired(token) && redisService.get(accessTokenRedisPrefix + token) == null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get username from Access token
    public String getUsernameFromAccessToken(String token) {
        return Jwts.parserBuilder().setSigningKey(generalKey(JWT_ACCESS_SECRET_KEY)).build().parseClaimsJws(token).getBody().getSubject();
    }

    // Get expiration date from Access token
    public Date getExpirationDateFromAccessToken(String token) {
        return Jwts.parserBuilder().setSigningKey(generalKey(JWT_ACCESS_SECRET_KEY)).build().parseClaimsJws(token).getBody().getExpiration();
    }

    // Check if Access token is expired
    private boolean isAccessTokenExpired(String token) {
        final Date expiration = getExpirationDateFromAccessToken(token);
        // 현재 시간이 만료 시간보다 뒤에 있으면 true
        return expiration.before(new Date());
    }

    private SecretKey generalKey(String key) {
        byte[] encodedKey = Base64.getDecoder().decode(key);
        // SecretKeySpec(byte[] key, int offset, int len, String algorithm)
        SecretKey returnKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "HmacSHA256");
        return returnKey;
    }
    
}
