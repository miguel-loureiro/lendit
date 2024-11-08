package com.ims.models.dtos.request;

import com.ims.models.Category;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UpdateItemDto {

    @NotEmpty
    @NotNull
    private String designation;

    private String description;
    private String category;
    private String brand;

    @NotEmpty
    @NotNull
    private String barcode;

    @NotNull
    @NotEmpty
    private BigDecimal purchasePrice;

    @NotNull
    @NotEmpty
    private Integer stockQuantity;
}
