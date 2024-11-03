package com.ims.repository;

import com.ims.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    Page<Item> findAll(Pageable pageable);
    Item getReferenceById(Integer id);
    Optional<Item> findByDesignation(String designation);
    Optional<Item> findByBarcode(String barcode);

    Page<Item> findByBrand(String brand, Pageable pageable);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.loans WHERE i.id = :id")
    Optional<Item> findByIdWithLoans(@Param("id") Integer id);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.loans WHERE i.designation = :designation")
    Optional<Item> findByDesignationWithLoans(@Param("designation") String designation);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.loans WHERE i.barcode = :barcode")
    Optional<Item> findByBarcodeWithLoans(@Param("barcode") String barcode);
}
