package com.ims.models;

import org.springframework.security.core.GrantedAuthority;
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

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Getter
    @Column(unique = true, nullable = false)
    private String username;

    @Getter
    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Getter
    @Column(nullable = false)
    private String password;

    @Getter
    @Enumerated(EnumType.STRING)
    private Type type;

    @Setter
    @Getter
    private String profileImage;

    @Version
    @Getter
    private Long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="user_items", joinColumns = @JoinColumn(name= "user_id"), inverseJoinColumns = @JoinColumn(name= "item_id"))
    private Set<Item> items = new HashSet<>();

    public Set<Item> getItems() {

        if(items == null) {

            items = new HashSet<>();
        }
        return items;
    }

    // No-argument constructor
    public User() {
    }

    // Constructor with all fields except id and books
    public User(String username, String email, String password, Type type) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
