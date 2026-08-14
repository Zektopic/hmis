package com.divudi.service;

import com.divudi.bean.common.ConfigOptionApplicationController;
import com.divudi.core.data.BooleanMessage;
import com.divudi.core.data.PaymentMethod;
import com.divudi.core.data.dataStructure.PaymentMethodData;
import com.divudi.core.entity.PaymentScheme;
import com.divudi.core.entity.membership.RestrictedPaymentMethod;
import com.divudi.core.facade.RestrictedPaymentMethodFacade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiscountSchemeValidationServiceTest {

    private DiscountSchemeValidationService service;
    private MockConfigOptionApplicationController mockConfigController;
    private MockRestrictedPaymentMethodFacade mockFacade;

    @BeforeEach
    public void setUp() {
        service = new DiscountSchemeValidationService();
        mockConfigController = new MockConfigOptionApplicationController();
        mockFacade = new MockRestrictedPaymentMethodFacade();

        service.configOptionApplicationController = mockConfigController;
        service.restrictedPaymentMethodFacade = mockFacade;
    }

    @Test
    public void testValidateDiscountScheme_NullPaymentMethod() {
        PaymentScheme scheme = new PaymentScheme();
        BooleanMessage result = service.validateDiscountScheme(null, scheme, null);
        assertFalse(result.isFlag());
        assertEquals("Payment Method is missing.", result.getMessage());
    }

    @Test
    public void testValidateDiscountScheme_NullDiscountScheme() {
        BooleanMessage result = service.validateDiscountScheme(PaymentMethod.Cash, null, null);
        assertTrue(result.isFlag());
        assertEquals("No Discount Scheme. Therefore nothing to validate.", result.getMessage());
    }

    @Test
    public void testValidateDiscountScheme_ValidAndNotRestricted() {
        PaymentScheme scheme = new PaymentScheme();
        scheme.setId(1L);
        scheme.setStaffRequired(false);

        BooleanMessage result = service.validateDiscountScheme(PaymentMethod.Cash, scheme, null);
        assertTrue(result.isFlag());
        assertEquals("No error in validating discount scheme.", result.getMessage());
    }

    @Test
    public void testValidateDiscountScheme_RestrictedPaymentMethod() {
        PaymentScheme scheme = new PaymentScheme();
        scheme.setId(1L);
        scheme.setName("Test Scheme");

        RestrictedPaymentMethod rpm = new RestrictedPaymentMethod();
        rpm.setPaymentMethod(PaymentMethod.Cash);
        rpm.setPaymentScheme(scheme);
        rpm.setRetired(false);

        List<RestrictedPaymentMethod> rows = new ArrayList<>();
        rows.add(rpm);
        mockFacade.setReturnRows(rows);

        BooleanMessage result = service.validateDiscountScheme(PaymentMethod.Cash, scheme, null);
        assertFalse(result.isFlag());
        assertEquals("Payment method Cash is restricted for discount scheme Test Scheme.", result.getMessage());
    }

    @Test
    public void testValidateDiscountScheme_StaffRequired_ValidStaff() {
        PaymentScheme scheme = new PaymentScheme();
        scheme.setId(1L);
        scheme.setStaffRequired(true);

        BooleanMessage result = service.validateDiscountScheme(PaymentMethod.Staff, scheme, null);
        assertTrue(result.isFlag());
        assertEquals("No error in validating discount scheme.", result.getMessage()); // Should be the top level message
    }

    @Test
    public void testValidateDiscountScheme_StaffRequired_InvalidStaff() {
        PaymentScheme scheme = new PaymentScheme();
        scheme.setId(1L);
        scheme.setName("Staff Scheme");
        scheme.setStaffRequired(true);

        BooleanMessage result = service.validateDiscountScheme(PaymentMethod.Cash, scheme, null);
        assertFalse(result.isFlag());
        assertEquals("Discount scheme Staff Scheme can NOT be allowed with the payment method Cash", result.getMessage());
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

    // Mock classes

    private static class MockConfigOptionApplicationController extends ConfigOptionApplicationController {
        @Override
        public Boolean getBooleanValueByKey(String key, boolean defaultValue) {
            return defaultValue;
        }
    }

    private static class MockRestrictedPaymentMethodFacade extends RestrictedPaymentMethodFacade {
        private List<RestrictedPaymentMethod> returnRows = new ArrayList<>();

        public void setReturnRows(List<RestrictedPaymentMethod> returnRows) {
            this.returnRows = returnRows;
        }

        @Override
        public List<RestrictedPaymentMethod> findByJpql(String jpql, Map<String, Object> parameters) {
            return returnRows;
        }
    }
}
