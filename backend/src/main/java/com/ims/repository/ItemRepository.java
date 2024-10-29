package com.ims.repository;

import com.ims.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    Page<Item> findAll(Pageable page);
    Item getReferenceById(Integer id);
    Optional<Item> findByDesignation(String designation);
    Optional<Item> findByBarcode(String barcode);

    Page<Item> findItemByBrand(String brand, Pageable page);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.users WHERE i.id = :id")
    Optional<Item> findByIdWithUsers(@Param("id") Integer id);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.users WHERE i.designation = :designation")
    Optional<Item> findByTitleWithUsers(@Param("designation") String designation);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.users WHERE i.barcode = :barcode")
    Optional<Item> findByIsbnWithUsers(@Param("barcode") String barcode);
}
