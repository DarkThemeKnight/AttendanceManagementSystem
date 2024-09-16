package com.backend.FaceRecognition.services.jwt_service;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.ResetPasswordToken;
import com.backend.FaceRecognition.repository.ResetPasswordTokenSaltRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    @Value("${token}")
    private String tokenSecretKey;
    private final ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository;
    public JwtService(ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository) {
        this.resetPasswordTokenSaltRepository = resetPasswordTokenSaltRepository;
    }
    public String extractTokenFromHeader(String authorizationHeader) {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                return authorizationHeader.substring(7); // Extract the token excluding "Bearer "
            } else {
                log.error("Invalid JWT token in Authorization header");
                throw new IllegalArgumentException("Invalid JWT token in Authorization header");
            }
        }
    public String getId(String jwtToken) {
            return extractClaim(jwtToken, Claims::getSubject);
        }
    private <T> T extractClaim(String tokenSecretKey, Function<Claims,T> fn) {
        final Claims claims = extractAllClaims(tokenSecretKey);
        return fn.apply(claims);
    }
    public boolean isValidToken(String token, UserDetails applicationUser) {
        final String username = getId(token);
        return username.equals(applicationUser.getUsername()) && !isExpired(token);
    }
    public String generate(Map<String,Object> map, ApplicationUser user, Date expiry){
        log.info("Expiry date {}",expiry);
        Date date = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setClaims(map)
                .setSubject(user.getId())
                .setIssuedAt(date)
                .setExpiration(expiry)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public static Date getDate(int value, char unit) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        switch (unit){
            case 'H'-> currentDateTime = currentDateTime.plusHours(value);
            case 'M'-> currentDateTime = currentDateTime.plusMinutes(value);
            case 'S'-> currentDateTime = currentDateTime.plusSeconds(value);
            case 'D'-> currentDateTime = currentDateTime.plusDays(value);
            case 'Y'-> currentDateTime = currentDateTime.plusYears(value);
            default -> throw new IllegalArgumentException("Invalid parameters");
        }
        log.info("Created date => {}",currentDateTime);
        return Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
        public String generateTemporaryToken(Map<String,Object> map, String userId)
        {
        String salt = UUID.randomUUID().toString();
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken();
        resetPasswordToken.setSalt(salt);
        resetPasswordToken.setExpiryDateTime(LocalDateTime.now().plusMinutes(10));
        resetPasswordToken.setUserId(userId);
        resetPasswordTokenSaltRepository.save(resetPasswordToken);
        Date date = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setClaims(map)
                .setSubject(userId+salt)
                .setIssuedAt(date)
                .setExpiration(new Date(System.currentTimeMillis() + (10 * 60 * 1000))) // 10 minutes
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
        }
        private boolean isExpired(String tokenSecretKey){
            return extractClaim(tokenSecretKey,Claims::getExpiration).before(new Date(System.currentTimeMillis()));
        }
        private Claims extractAllClaims(String token){
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
        private Key getSecretKey(){
            byte[] keyBytes = Decoders.BASE64.decode(tokenSecretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        }
}