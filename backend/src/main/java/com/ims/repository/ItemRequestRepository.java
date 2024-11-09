package com.ims.repository;

import com.ims.models.*;
import com.ims.models.dtos.response.ItemStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {

    /**
     * Finds the highest queue position for a given item
     * Used for assigning new queue positions
     */
    @Query("SELECT MAX(ir.queuePosition) FROM ItemRequest ir WHERE ir.item.id = :itemId")
    Optional<Integer> findMaxQueuePositionForItem(@Param("itemId") Integer itemId);

    /**
     * Finds all pending requests for a given item
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.item.id = :itemId " +
            "AND ir.status = 'PENDING' " +
            "ORDER BY ir.queuePosition ASC")
    List<ItemRequest> findPendingRequestsForItem(@Param("itemId") Integer itemId);

    /**
     * Finds all active requests for a given user
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.user.id = :userId " +
            "AND ir.status IN ('PENDING', 'FULFILLED') " +
            "ORDER BY ir.requestDate DESC")
    List<ItemRequest> findActiveRequestsByUser(@Param("userId") Integer userId);

    /**
     * Finds all requests that can potentially be fulfilled based on available quantity
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.item.id = :itemId " +
            "AND ir.status = 'PENDING' " +
            "AND ir.requestedQuantity <= :availableQuantity " +
            "ORDER BY ir.queuePosition ASC")
    List<ItemRequest> findFulfillableRequests(
            @Param("itemId") Integer itemId,
            @Param("availableQuantity") Integer availableQuantity);

    /**
     * Finds overlapping requests for the same item in a date range
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.item.id = :itemId " +
            "AND ir.status = 'FULFILLED' " +
            "AND ((ir.requestDate BETWEEN :startDate AND :endDate) " +
            "OR (ir.returnDate BETWEEN :startDate AND :endDate) " +
            "OR (:startDate BETWEEN ir.requestDate AND ir.returnDate))")
    List<ItemRequest> findOverlappingRequests(
            @Param("itemId") Integer itemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Updates the queue positions for all pending requests after a specific position
     */
    @Modifying
    @Query("UPDATE ItemRequest ir " +
            "SET ir.queuePosition = ir.queuePosition - 1 " +
            "WHERE ir.item.id = :itemId " +
            "AND ir.status = 'PENDING' " +
            "AND ir.queuePosition > :position")
    void reorderQueuePositionsAfter(
            @Param("itemId") Integer itemId,
            @Param("position") Integer position);

    /**
     * Finds requests that are overdue (past return date)
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.status = 'FULFILLED' " +
            "AND ir.returnDate < :date")
    List<ItemRequest> findOverdueRequests(@Param("date") LocalDate date);

    /**
     * Counts active requests for an item
     */
    @Query("SELECT COUNT(ir) FROM ItemRequest ir " +
            "WHERE ir.item.id = :itemId " +
            "AND ir.status IN ('PENDING', 'FULFILLED')")
    long countActiveRequestsForItem(@Param("itemId") Integer itemId);

    /**
     * Finds requests that were fulfilled within a date range
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.status = 'FULFILLED' " +
            "AND ir.requestDate BETWEEN :startDate AND :endDate " +
            "ORDER BY ir.requestDate DESC")
    List<ItemRequest> findFulfilledRequestsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Finds the next pending request for an item that can be fulfilled
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.item.id = :itemId " +
            "AND ir.status = 'PENDING' " +
            "AND ir.requestedQuantity <= :availableQuantity " +
            "ORDER BY ir.queuePosition ASC " +
            "LIMIT 1")
    Optional<ItemRequest> findNextFulfillableRequest(
            @Param("itemId") Integer itemId,
            @Param("availableQuantity") Integer availableQuantity);

    /**
     * Finds requests that need to be processed for auto-return
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.status = 'FULFILLED' " +
            "AND ir.returnDate = :date")
    List<ItemRequest> findRequestsDueForReturn(@Param("date") LocalDate date);

    List<ItemRequest> findByItemAndStatusIn(Item item, List<ItemRequestStatus> statuses);
}

