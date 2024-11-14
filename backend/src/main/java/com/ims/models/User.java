package com.ims.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @JsonIgnore // Prevent serialization of the password
    @Column(nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String profileImage;

    @Version
    private Long version;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Loan> loans = new HashSet<>(); // Add this field to track loans

    // No-argument constructor
    public User() {}

    // Constructor with all fields except id
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Constructor with loans
    public User(String username, String email, String password, Role role, Set<Loan> loans) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.loans = loans;
    }

    public <T> User(String testEmail, String password, List<T> ts) {
    }

    public User(String mail, String password, Role role) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User ) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // UserDetails methods
    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String toString() {
        return "User {" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", profileImage='" + profileImage + '\'' +
                '}';
    }

    // Check if the user has any active or overdue loans for the given item
    public boolean hasActiveLoanForItem(Item item) {
        return loans.stream()
                .filter(loan -> loan.getItem().equals(item))
                .anyMatch(loan -> loan.getStatus() == LoanStatus.ACTIVE);
    }

    // Check if the user can borrow the item directly (item is available and user has no active loans for the item)
    public boolean isAvailableForDirectLoan(Item item) {
        return item.isAvailableForDirectLoan() && !hasActiveLoanForItem(item);
    }

    public int getLoanLimit() {
        return 2;
    }
}