package com.ims.helpers.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DesignationOrBarcodeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDesignationOrBarcode {
    String message() default "Either designation or barcode must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

