package com.ims.services;

import com.ims.models.CustomUserDetails;
import com.ims.models.Type;
import com.ims.models.User;
import com.ims.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    String secretKey;

    @Value("${security.jwt.expiration}")
    long jwtExpiration;

    @Autowired
    private UserRepository userRepository;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaimsAllowExpired(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(CustomUserDetails userDetails) {
        if (userDetails == null || "guestuser".equals(userDetails.getUsername())) {
            CustomUserDetails guestUserDetails = createGuestUserDetails();
            return generateToken(new HashMap<>(), guestUserDetails);
        }

        Optional<User> userOptional = userRepository.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            return generateToken(new HashMap<>(), userDetails);
        } else {
            throw new UsernameNotFoundException("User not found or invalid role for token generation: " + userDetails.getUsername());
        }
    }

    public String generateToken(Map<String, Object> extraClaims, CustomUserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSignInKey() {
        byte[] bytes = Base64.getDecoder()
                .decode(secretKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(bytes, "HmacSHA256"); }

    Claims extractAllClaimsAllowExpired(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();  // Return claims even if the token is expired
        }
    }

    String buildToken(
            Map<String, Object> extraClaims,
            CustomUserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private CustomUserDetails createGuestUserDetails() {
        // Create a User object for the GUEST user
        User guestUser = new User();
        guestUser.setUsername("guestuser");
        guestUser.setPassword(""); //
        guestUser.setType(Type.GUEST);

        return new CustomUserDetails(guestUser);
    }
}