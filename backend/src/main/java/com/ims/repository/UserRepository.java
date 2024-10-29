package com.ims.repository;

import com.ims.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findAll(Pageable page);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.items WHERE u.id = :id")
    Optional<User> findByIdWithItems(@Param("id") Integer id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.items WHERE u.username = :username")
    Optional<User> findByUsernameWithItems(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.items WHERE u.email = :email")
    Optional<User> findByEmailWithItems(@Param("email") String email);
}
