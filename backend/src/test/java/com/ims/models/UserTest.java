package com.ims.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;
    private final String USERNAME = "testUser";
    private final String EMAIL = "test@example.com";
    private final String PASSWORD = "password123";
    private final Role ROLE = Role.CLIENT;

    @BeforeEach
    void setUp() {
        user = new User(USERNAME, EMAIL, PASSWORD, ROLE);
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals(USERNAME, user.getUsername());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD, user.getPassword());
        assertEquals(ROLE, user.getRole());
    }

    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
        assertNotNull(emptyUser.getItems()); // Should return empty set, not null
    }

    @Test
    void testGetItems_InitializesEmptySet() {
        User newUser = new User();
        newUser.setItems(null);
        Set<Item> items = newUser.getItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testEquals_SameUser() {
        User sameUser = new User(USERNAME, EMAIL, PASSWORD, ROLE);
        assertEquals(user, sameUser);
        assertEquals(user.hashCode(), sameUser.hashCode());
    }

    @Test
    void testEquals_DifferentUser() {
        User differentUser = new User("otherUser", "other@example.com", PASSWORD, ROLE);
        assertNotEquals(user, differentUser);
        assertNotEquals(user.hashCode(), differentUser.hashCode());
    }

    @Test
    void testEquals_NullAndDifferentClass() {
        assertNotEquals(user, null);
        assertNotEquals(user, new Object());
    }

    @Test
    void testEquals_SameEmailDifferentUsername() {
        User sameEmailUser = new User("differentUsername", EMAIL, PASSWORD, ROLE);
        assertNotEquals(user, sameEmailUser);
    }

    @Test
    void testUserDetails_Authorities() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

    @Test
    void testUserDetails_AccountStatus() {
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void testVersion() {
        Long version = 1L;
        user.setVersion(version);
        assertEquals(version, user.getVersion());
    }

    @Test
    void testProfileImage() {
        String profileImage = "profile.jpg";
        user.setProfileImage(profileImage);
        assertEquals(profileImage, user.getProfileImage());
    }

    @Test
    void testSettersAndGetters() {
        Integer id = 1;
        String newUsername = "newUsername";
        String newEmail = "new@example.com";
        String newPassword = "newPassword";
        Role newRole = Role.MANAGER;

        user.setId(id);
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setPassword(newPassword);
        user.setRole(newRole);

        assertEquals(id, user.getId());
        assertEquals(newUsername, user.getUsername());
        assertEquals(newEmail, user.getEmail());
        assertEquals(newPassword, user.getPassword());
        assertEquals(newRole, user.getRole());
    }
}