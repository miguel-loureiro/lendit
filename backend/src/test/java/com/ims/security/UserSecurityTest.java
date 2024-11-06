package com.ims.security;

import com.ims.models.Role;
import com.ims.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSecurityTest {

    @InjectMocks
    private UserSecurity userSecurity;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void isCurrentUser_WithAuthenticatedUser_ShouldReturnTrue() {
        User user = new User("testUser", "test@example.com", "password", Role.CLIENT);
        user.setId(1);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        boolean result = userSecurity.isCurrentUser(1);

        assertTrue(result);
    }

    @Test
    void isCurrentUser_WithDifferentUser_ShouldReturnFalse() {
        User user = new User("testUser", "test@example.com", "password", Role.CLIENT);
        user.setId(1);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        boolean result = userSecurity.isCurrentUser(2);

        assertFalse(result);
    }

    @Test
    void isCurrentUser_WithUnauthenticatedUser_ShouldReturnFalse() {
        when(securityContext.getAuthentication()).thenReturn(null);

        boolean result = userSecurity.isCurrentUser(1);

        assertFalse(result);
    }

    @Test
    void isCurrentUser_WithNonUserPrincipal_ShouldReturnFalse() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", null, Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        boolean result = userSecurity.isCurrentUser(1);

        assertFalse(result);
    }
}