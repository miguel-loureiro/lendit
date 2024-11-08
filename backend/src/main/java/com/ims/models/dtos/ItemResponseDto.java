package com.ims.models.dtos;

import com.ims.models.Category;
import com.ims.models.Item;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@NoArgsConstructor
public class ItemResponseDto {

    @Getter
    @Setter
    private String designation;
    @Getter
    @Setter
    private String category;
    @Getter
    @Setter
    private String barcode;
    @Getter
    @Setter
    private String brand;
    @Getter
    @Setter
    private BigDecimal purchasePrice;
    @Getter
    @Setter
    private Integer stockQuantity;

    public ItemResponseDto(Item item) {
        this.designation = item.getDesignation();
        this.category = item.getCategory();
        this.barcode = item.getBarcode();
        this.brand = item.getBrand();
        this.purchasePrice = item.getPurchasePrice();
        this.stockQuantity = item.getAvailableQuantity();
    }

    public ItemResponseDto(String designation, String category, String barcode, String brand, BigDecimal purchasePrice, Integer stockQuantity) {
        this.designation = designation;
        this.category = category;
        this.barcode = barcode;
        this.brand = brand;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemResponseDto that = (ItemResponseDto) o;
        return Objects.equals(designation, that.designation) && category == that.category && Objects.equals(barcode, that.barcode) && Objects.equals(brand, that.brand) && Objects.equals(purchasePrice, that.purchasePrice) && Objects.equals(stockQuantity, that.stockQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(designation, category, barcode, brand, purchasePrice, stockQuantity);
    }
}

