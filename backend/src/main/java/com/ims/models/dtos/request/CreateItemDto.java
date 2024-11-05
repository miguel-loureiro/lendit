package com.ims.models.dtos.request;

import com.ims.models.Category;
import com.ims.models.Item;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemDto {

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotNull(message = "Category is required")
    private Category category;

    @DecimalMin(value = "0.01", message = "Purchase price must be greater than 0")
    private BigDecimal purchasePrice;

    private Integer stockQuantity = 1; // Default value

    // You can add a method to convert this DTO to an Item entity
    public Item toItem() {
        return new Item(designation, barcode, brand, category, purchasePrice != null ? purchasePrice : BigDecimal.ZERO, stockQuantity);
    }
}
