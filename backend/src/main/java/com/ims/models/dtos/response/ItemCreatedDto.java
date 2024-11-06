package com.ims.models.dtos.response;

import com.ims.models.Category;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemCreatedDto {
    private Integer id;
    private String designation;
    private String barcode;
    private String brand;
    private Category category;
    private BigDecimal purchasePrice;
    private Integer stockQuantity;
    private Long version;
}
