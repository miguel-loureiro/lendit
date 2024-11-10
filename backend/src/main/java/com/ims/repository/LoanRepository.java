package com.ims.repository;

import com.ims.models.Loan;
import com.ims.models.LoanStatus;
import com.ims.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Integer> {

    Page<Loan> findAll(Pageable pageable);

    /**
     * Find all loans for a specific user with given statuses
     *
     * @param user The user whose loans to find
     * @param statuses List of loan statuses to filter by
     * @return List of loans matching the criteria
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.status IN :statuses ORDER BY l.startDate DESC")
    List<Loan> findByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<LoanStatus> statuses); // Adjust the type if needed);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = :user AND l.status IN :statuses")
    Long countActiveLoans(@Param("user") User user, @Param("statuses") List<LoanStatus> statuses
    );
    @Query("SELECT l FROM Loan l WHERE l.item.id = :itemId")
    Page<Loan> findByItemId(@Param("itemId") Integer itemId, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId")
    Page<Loan> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    Optional<Loan> findById(Integer id);

    Optional<Loan> findByUserIdAndItemIdAndReturnDateIsNullAndEndDateGreaterThanEqual(
            Integer userId,
            Integer itemId,
            LocalDate date
    );
}

