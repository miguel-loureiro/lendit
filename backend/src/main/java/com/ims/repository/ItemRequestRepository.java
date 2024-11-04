package com.ims.repository;

import com.ims.models.*;
import com.ims.models.dtos.response.ItemStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {

    // Basic queries
    boolean existsByUserAndItemAndStatusIn(User user, Item item, Set<ItemRequestStatus> statuses);

    // Find items with waiting requests
    @Query("SELECT DISTINCT i FROM Item i JOIN i.requests r " +
            "WHERE r.status = 'PENDING'")
    List<Item> findItemsWithPendingRequests();

    // Custom query to find the maximum queue position for a specific item
    @Query("SELECT COALESCE(MAX(ir.queuePosition), 0) FROM ItemRequest ir WHERE ir.item = :item")
    Optional<Integer> findMaxPositionByItem(@Param("item") Item item);

    // Custom query to find ItemRequests by status and request date before a specified date
    List<ItemRequest> findByStatusAndRequestDateBefore(ItemRequestStatus status, LocalDateTime requestDate);

    // Custom query to find an ItemRequest by user, item, and status
    Optional<ItemRequest> findByUserAndItemAndStatus(User user, Item item, ItemRequestStatus status);
}
