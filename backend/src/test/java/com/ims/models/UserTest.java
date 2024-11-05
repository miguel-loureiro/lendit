package com.ims.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Test")
class UserTest {

    private User user;
    private static final String USERNAME = "testUser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final Role ROLE = Role.CLIENT;

    @BeforeEach
    void setUp() {
        // Common Arrange
        user = new User(USERNAME, EMAIL, PASSWORD, ROLE);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create user with all fields properly set")
        void shouldCreateUserWithAllFields() {
            // Arrange
            // Using the user created in setUp()

            // Act - nothing additional needed as object creation is the action we're testing

            // Assert
            assertAll(
                    "User Creation Verification",
                    () -> assertNotNull(user),
                    () -> assertEquals(USERNAME, user.getUsername()),
                    () -> assertEquals(EMAIL, user.getEmail()),
                    () -> assertEquals(PASSWORD, user.getPassword()),
                    () -> assertEquals(ROLE, user.getRole())
            );
        }

        @Test
        @DisplayName("Should create user with no-args constructor")
        void shouldCreateUserWithNoArgsConstructor() {
            // Arrange - nothing needed

            // Act
            User emptyUser = new User();

            // Assert
            assertAll(
                    "Empty User Verification",
                    () -> assertNotNull(emptyUser),
                    () -> assertNotNull(emptyUser.getLoans()),
                    () -> assertTrue(emptyUser.getLoans().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when username and email match")
        void shouldBeEqualWhenUsernameAndEmailMatch() {
            // Arrange
            User sameUser = new User(USERNAME, EMAIL, "differentPassword", Role.MANAGER);

            // Act
            boolean areEqual = user.equals(sameUser);
            int userHash = user.hashCode();
            int sameUserHash = sameUser.hashCode();

            // Assert
            assertAll(
                    "Equality Verification",
                    () -> assertTrue(areEqual),
                    () -> assertEquals(userHash, sameUserHash)
            );
        }

        @Test
        @DisplayName("Should not be equal when username or email differ")
        void shouldNotBeEqualWhenUsernameOrEmailDiffer() {
            // Arrange
            User differentUser = new User("otherUser", "other@example.com", PASSWORD, ROLE);
            User sameEmailUser = new User("differentUsername", EMAIL, PASSWORD, ROLE);
            User sameUsernameUser = new User(USERNAME, "different@example.com", PASSWORD, ROLE);

            // Act
            boolean equalsWithDifferent = user.equals(differentUser);
            boolean equalsWithSameEmail = user.equals(sameEmailUser);
            boolean equalsWithSameUsername = user.equals(sameUsernameUser);
            boolean equalsWithNull = false;
            boolean equalsWithDifferentClass = user.equals(new Object());

            // Assert
            assertAll(
                    "Inequality Verification",
                    () -> assertFalse(equalsWithDifferent),
                    () -> assertFalse(equalsWithSameEmail),
                    () -> assertFalse(equalsWithSameUsername),
                    () -> assertFalse(equalsWithNull),
                    () -> assertFalse(equalsWithDifferentClass)
            );
        }
    }

    @Nested
    @DisplayName("UserDetails Implementation Tests")
    class UserDetailsTests {

        @Test
        @DisplayName("Should return correct authorities")
        void shouldReturnCorrectAuthorities() {
            // Arrange
            SimpleGrantedAuthority expectedAuthority = new SimpleGrantedAuthority("ROLE_CLIENT");

            // Act
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            // Assert
            assertAll(
                    "Authorities Verification",
                    () -> assertNotNull(authorities),
                    () -> assertEquals(1, authorities.size()),
                    () -> assertTrue(authorities.contains(expectedAuthority))
            );
        }

        @Test
        @DisplayName("Should return correct account status")
        void shouldReturnCorrectAccountStatus() {
            // Arrange - nothing additional needed

            // Act
            boolean isNonExpired = user.isAccountNonExpired();
            boolean isNonLocked = user.isAccountNonLocked();
            boolean isCredentialsNonExpired = user.isCredentialsNonExpired();
            boolean isEnabled = user.isEnabled();

            // Assert
            assertAll(
                    "Account Status Verification",
                    () -> assertTrue(isNonExpired, "Account should not be expired"),
                    () -> assertTrue(isNonLocked, "Account should not be locked"),
                    () -> assertTrue(isCredentialsNonExpired, "Credentials should not be expired"),
                    () -> assertTrue(isEnabled, "Account should be enabled")
            );
        }
    }

    @Nested
    @DisplayName("Property Management Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should manage all properties correctly")
        void shouldManageAllPropertiesCorrectly() {
            // Arrange
            Integer id = 1;
            String newUsername = "newUsername";
            String newEmail = "new@example.com";
            String newPassword = "newPassword";
            Role newRole = Role.MANAGER;
            String newProfileImage = "new-profile.jpg";
            Long newVersion = 2L;

            // Act
            user.setId(id);
            user.setUsername(newUsername);
            user.setEmail(newEmail);
            user.setPassword(newPassword);
            user.setRole(newRole);
            user.setProfileImage(newProfileImage);
            user.setVersion(newVersion);

            // Assert
            assertAll(
                    "Property Management Verification",
                    () -> assertEquals(id, user.getId()),
                    () -> assertEquals(newUsername, user.getUsername()),
                    () -> assertEquals(newEmail, user.getEmail()),
                    () -> assertEquals(newPassword, user.getPassword()),
                    () -> assertEquals(newRole, user.getRole()),
                    () -> assertEquals(newProfileImage, user.getProfileImage()),
                    () -> assertEquals(newVersion, user.getVersion())
            );
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate correct string representation")
        void shouldGenerateCorrectStringRepresentation() {
            // Arrange
            Integer id = 1;
            String profileImage = "profile.jpg";
            user.setId(id);
            user.setProfileImage(profileImage);

            // Act
            String result = user.toString();

            // Assert
            assertAll(
                    "ToString Verification",
                    () -> assertTrue(result.contains("id=" + id)),
                    () -> assertTrue(result.contains("username='" + USERNAME + "'")),
                    () -> assertTrue(result.contains("email='" + EMAIL + "'")),
                    () -> assertTrue(result.contains("role=" + ROLE)),
                    () -> assertTrue(result.contains("profileImage='" + profileImage + "'")),
                    () -> assertFalse(result.contains(PASSWORD))
            );
        }
    }
}