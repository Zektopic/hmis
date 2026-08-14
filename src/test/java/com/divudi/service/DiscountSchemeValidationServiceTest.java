package com.divudi.service;

import com.divudi.core.data.BooleanMessage;
import com.divudi.core.data.PaymentMethod;
import com.divudi.core.entity.PaymentScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiscountSchemeValidationServiceTest {

    private DiscountSchemeValidationService service;

    @BeforeEach
    public void setUp() {
        service = new DiscountSchemeValidationService();
    }

    @Test
    public void testValidateNotRestrictedPaymentMethodWithNulls() {
        // Test with all nulls
        BooleanMessage result = service.validateNotRestrictedPaymentMethod(null, null, null);
        assertNotNull(result);
        assertTrue(result.isFlag());
        assertEquals("No restricted payment method.", result.getMessage());

        // Test with discountScheme null
        result = service.validateNotRestrictedPaymentMethod(PaymentMethod.Cash, null, null);
        assertNotNull(result);
        assertTrue(result.isFlag());
        assertEquals("No restricted payment method.", result.getMessage());

        // Test with paymentMethod null
        result = service.validateNotRestrictedPaymentMethod(null, new PaymentScheme(), null);
        assertNotNull(result);
        assertTrue(result.isFlag());
        assertEquals("No restricted payment method.", result.getMessage());
    }
}
