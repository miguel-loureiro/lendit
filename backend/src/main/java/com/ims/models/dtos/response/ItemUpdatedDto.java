package com.ims.models.dtos.response;

import com.ims.models.Category;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemUpdatedDto {
    private Integer id;
    private String designation;
    private String description;
    private String category;
    private String barcode;
    private String brand;

    private BigDecimal purchasePrice;
    private Integer stockQuantity;
    private Long version;
}
