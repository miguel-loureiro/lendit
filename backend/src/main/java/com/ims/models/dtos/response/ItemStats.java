package com.ims.models.dtos.response;

import com.ims.models.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ItemStats {
    private Category category;
    private Long totalItems;
    private Long totalStock;
    private BigDecimal averagePrice;
}
