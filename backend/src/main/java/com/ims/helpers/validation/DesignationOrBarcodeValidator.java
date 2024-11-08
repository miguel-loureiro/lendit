package com.ims.helpers.validation;

import com.ims.models.dtos.request.ItemRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DesignationOrBarcodeValidator implements ConstraintValidator<ValidDesignationOrBarcode, ItemRequestDto> {

    @Override
    public boolean isValid(ItemRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true; // or false, depending on your needs
        }

        // Check if at least one of the fields is not empty
        return (dto.getDesignation() != null && !dto.getDesignation().isEmpty()) ||
                (dto.getBarcode() != null && !dto.getBarcode().isEmpty());
    }
}
