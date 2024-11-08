package com.ims.models.dtos.request;

import com.ims.helpers.validation.ValidDesignationOrBarcode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ValidDesignationOrBarcode
public class ItemRequestDto {

    @NotEmpty
    @NotNull
    private String username;

    @NotEmpty
    @NotNull
    private String designation;

    @NotEmpty
    @NotNull
    private String barcode;

    @NotNull
    @NotEmpty
    private Integer requestedQuantity;
}
