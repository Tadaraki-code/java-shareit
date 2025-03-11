package ru.practicum.shareit.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class FutureWithInputLagValidator implements ConstraintValidator<FutureWithInputLag, LocalDateTime> {

    private long inputLag;

    @Override
    public void initialize(FutureWithInputLag constraintAnnotation) {
        this.inputLag = constraintAnnotation.inputLag();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        LocalDateTime nowWithInputLag = LocalDateTime.now().minusSeconds(inputLag);
        return value.isAfter(nowWithInputLag) || value.isEqual(nowWithInputLag);
    }
}
