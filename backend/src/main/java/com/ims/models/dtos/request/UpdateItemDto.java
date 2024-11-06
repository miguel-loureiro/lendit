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

    @NotEmpty
    @NotNull
    private String barcode;

    @NotEmpty
    @NotNull
    private String brand;

    @Enumerated
    private Category category;

    @NotNull
    @NotEmpty
    private BigDecimal purchasePrice;

    @NotNull
    @NotEmpty
    private Integer stockQuantity;
}
