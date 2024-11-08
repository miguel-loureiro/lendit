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

    Optional<Integer> findMaxQueuePositionByItem(Item item);

    Optional<ItemRequest> findFirstByItemAndStatusOrderByQueuePositionAsc(Item item, ItemRequestStatus status);

    List<ItemRequest> findByItemAndStatusOrderByQueuePositionAsc(Item item, ItemRequestStatus status);

    List<ItemRequest> findByUserOrderByRequestDateDesc(User user);

    List<ItemRequest> findByItemOrderByQueuePositionAsc(Item item);

    // Custom query methods

    /**
     * Finds the number of active (PENDING or FULFILLED) requests for the given user and item.
     *
     * @param user The user for which to find the active requests.
     * @param item The item for which to find the active requests.
     * @return The count of active requests for the given user and item.
     */
    int countByUserAndItemAndStatusIn(User user, Item item, List<ItemRequestStatus> statuses);

    /**
     * Finds the number of active (PENDING or FULFILLED) requests for the given item.
     *
     * @param item The item for which to find the active requests.
     * @param statuses The request statuses to include in the count.
     * @return The count of active requests for the given item.
     */
    int countByItemAndStatusIn(Item item, List<ItemRequestStatus> statuses);

    /**
     * Finds the active (PENDING or FULFILLED) requests for the given user and item.
     *
     * @param user The user for which to find the active requests.
     * @param item The item for which to find the active requests.
     * @return The list of active requests for the given user and item.
     */
    List<ItemRequest> findByUserAndItemAndStatusIn(User user, Item item, List<ItemRequestStatus> statuses);

    /**
     * Finds the active (PENDING or FULFILLED) requests for the given item.
     *
     * @param item The item for which to find the active requests.
     * @param statuses The request statuses to include in the search.
     * @return The list of active requests for the given item.
     */
    List<ItemRequest> findByItemAndStatusIn(Item item, List<ItemRequestStatus> statuses);
}

