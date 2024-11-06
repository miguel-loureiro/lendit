package com.ims.security;

import com.ims.models.Role;
import com.ims.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private User mockUser;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION = 86400000; // 1 day in milliseconds

    @BeforeEach
    public void setUp() {
        jwtService.secretKey = SECRET_KEY;
        jwtService.jwtExpiration = JWT_EXPIRATION;

    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        String token = jwtService.generateToken(mockUser);
        String extractedEmail = jwtService.extractEmail(token);
        when(mockUser.getEmail()).thenReturn(TEST_EMAIL);
        assertEquals(TEST_EMAIL, extractedEmail);
    }

    @Test
    void extractClaim_ShouldReturnCorrectClaim() {
        String token = jwtService.generateToken(mockUser);
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        when(mockUser.getEmail()).thenReturn(TEST_EMAIL);
        assertEquals(TEST_EMAIL, subject);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtService.generateToken(mockUser);
        UserDetails userDetails = new User(TEST_EMAIL, "password", Collections.emptyList());
        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Set a very short expiration time for this test
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 10000L);
        String token = jwtService.generateToken(mockUser);

        // Wait for the token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UserDetails userDetails = new User(TEST_EMAIL, "password", Collections.emptyList());
        assertFalse(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_WithInvalidEmail_ShouldReturnFalse() {
        String token = jwtService.generateToken(mockUser);
        UserDetails userDetails = new User("wrong@example.com", "password", Collections.emptyList());
        assertFalse(jwtService.validateToken(token, userDetails));
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtService.generateToken(mockUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getExpirationTime_ShouldReturnCorrectTime() {
        assertEquals(JWT_EXPIRATION, jwtService.getExpirationTime());
    }

    @Test
    void createToken_ShouldContainCorrectClaims() {
        String token = jwtService.generateToken(mockUser);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(TEST_EMAIL, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void extractAllClaims_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalidToken";
        assertThrows(JwtException.class, () -> jwtService.extractClaim(invalidToken, Claims::getSubject));
    }
}