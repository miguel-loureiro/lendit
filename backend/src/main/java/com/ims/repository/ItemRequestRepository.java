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
     * Finds requests that need to be processed for auto-return
     */
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.status = 'FULFILLED' " +
            "AND ir.returnDate = :date")
    List<ItemRequest> findRequestsDueForReturn(@Param("date") LocalDate date);

    /**
     * Finds pending requests for a specific item, ordered by queue position.
     * Used when processing the request queue after item returns.
     *
     * @param item The item being requested
     * @param status The status of requests to find (typically PENDING)
     * @return List of requests ordered by queue position
     */
    List<ItemRequest> findByItemAndStatusOrderByQueuePosition(Item item, ItemRequestStatus status);

    /**
     * Finds the highest queue position for a specific item.
     * Used when assigning queue positions to new requests.
     *
     * @param itemId The ID of the item
     * @return Optional containing the highest queue position if any requests exist
     */
    @Query("SELECT MAX(r.queuePosition) FROM ItemRequest r WHERE r.item.id = :itemId")
    Optional<Integer> findMaxQueuePositionForItem(@Param("itemId") int itemId);

    /**
     * Reorders queue positions after a request is fulfilled or canceled.
     * Decrements queue positions for all requests after the given position.
     *
     * @param itemId The ID of the item
     * @param currentPosition Position from which to reorder
     * @return Number of updated records
     */
    @Modifying
    @Query("UPDATE ItemRequest r SET r.queuePosition = r.queuePosition - 1 " +
            "WHERE r.item.id = :itemId AND r.queuePosition > :currentPosition " +
            "AND r.status = 'PENDING'")
    int reorderQueuePositionsAfter(
            @Param("itemId") int itemId,
            @Param("currentPosition") int currentPosition
    );

    /**
     * Gets all pending requests that could potentially be fulfilled based on quantity.
     * Used for batch processing of requests when items become available.
     *
     * @param itemId The ID of the item
     * @param availableQuantity The available quantity of the item
     * @param status The status to filter by (typically PENDING)
     * @return List of requests that could be fulfilled
     */
    @Query("SELECT r FROM ItemRequest r " +
            "WHERE r.item.id = :itemId " +
            "AND r.requestedQuantity <= :availableQuantity " +
            "AND r.status = :status " +
            "ORDER BY r.queuePosition")
    List<ItemRequest> findFulfillableRequests(
            @Param("itemId") Long itemId,
            @Param("availableQuantity") int availableQuantity,
            @Param("status") ItemRequestStatus status
    );

    /**
     * Counts pending requests for an item.
     * Used for inventory management and reporting.
     *
     * @param itemId The ID of the item
     * @param status The status to count (typically PENDING)
     * @return Count of pending requests
     */
    @Query("SELECT COUNT(r) FROM ItemRequest r " +
            "WHERE r.item.id = :itemId AND r.status = :status")
    long countRequestsByItemAndStatus(
            @Param("itemId") Long itemId,
            @Param("status") ItemRequestStatus status
    );

    /**
     * Gets total quantity requested across all pending requests for an item.
     * Used for inventory planning.
     *
     * @param itemId The ID of the item
     * @param status The status to sum (typically PENDING)
     * @return Sum of requested quantities
     */
    @Query("SELECT COALESCE(SUM(r.requestedQuantity), 0) FROM ItemRequest r " +
            "WHERE r.item.id = :itemId AND r.status = :status")
    int sumRequestedQuantityByItemAndStatus(
            @Param("itemId") Long itemId,
            @Param("status") ItemRequestStatus status
    );

    List<ItemRequest> findByItemAndStatusIn(Item item, List<ItemRequestStatus> statuses);
}

