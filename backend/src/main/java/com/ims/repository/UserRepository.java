package com.ims.repository;

import com.ims.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Optional<Integer> findIdByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.loans WHERE u.id = :id")
    Optional<User> findByIdWithLoans(@Param("id") Integer id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.loans WHERE u.username = :username")
    Optional<User> findByUsernameWithLoans(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.loans WHERE u.email = :email")
    Optional<User> findByEmailWithLoans(@Param("email") String email);
}
