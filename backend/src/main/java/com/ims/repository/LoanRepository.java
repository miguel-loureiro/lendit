package com.ims.repository;

import com.ims.models.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Integer> {

    Page<Loan> findAll(Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.item.id = :itemId")
    Page<Loan> findByItemId(@Param("itemId") Integer itemId, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId")
    Page<Loan> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    Optional<Loan> findById(Integer id);
}

