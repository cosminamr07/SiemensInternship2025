package com.siemens.internship;

import com.siemens.internship.Model.Item;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemValidationTest {

    private static Validator validator;

    @BeforeAll
    static void iniValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidEmailPassesValidation() {
        Item item = new Item(1L, "Laptop", "Thin", "PROCESSED", "valid@example.com");
        Set<ConstraintViolation<Item>> errors =validator.validate(item);
             assertTrue(errors.isEmpty(),"Expected no errors for valid email");
    }

    @Test
    void testInvalidEmailFailsValidation() {
        Item item = new Item(2L, "Monitor", "Wide Screen", "PROCESSED", "invalid-Email");
        Set<ConstraintViolation<Item>> errors = validator.validate(item);
        assertFalse(errors.isEmpty(),"Expected validation errors for invalid email");
        assertTrue(errors.stream().anyMatch(v -> v.getMessage().contains("Invalid email format")));
    }
}
