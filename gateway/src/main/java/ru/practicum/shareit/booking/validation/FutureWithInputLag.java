package ru.practicum.shareit.booking.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureWithInputLagValidator.class)
public @interface FutureWithInputLag {
    String message() default "Дата бронирования должна быть в настоящем или будущем";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long inputLag() default 5; // Допуск в секундах
}
