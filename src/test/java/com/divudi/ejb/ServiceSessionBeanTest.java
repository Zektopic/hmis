package com.divudi.ejb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ServiceSessionBean utility methods.
 */
public class ServiceSessionBeanTest {

    @Test
    @DisplayName("Should return true for valid integers")
    public void testIsNumericValidIntegers() {
        assertTrue(ServiceSessionBean.isNumeric("123"), "Positive integer should be numeric");
        assertTrue(ServiceSessionBean.isNumeric("-456"), "Negative integer should be numeric");
        assertTrue(ServiceSessionBean.isNumeric("0"), "Zero should be numeric");
    }

    @Test
    @DisplayName("Should return true for valid decimal numbers")
    public void testIsNumericValidDecimals() {
        assertTrue(ServiceSessionBean.isNumeric("12.34"), "Positive decimal should be numeric");
        assertTrue(ServiceSessionBean.isNumeric("-12.34"), "Negative decimal should be numeric");
        assertTrue(ServiceSessionBean.isNumeric("0.0"), "Zero decimal should be numeric");
    }

    @Test
    @DisplayName("Should return false for invalid strings")
    public void testIsNumericInvalidStrings() {
        assertFalse(ServiceSessionBean.isNumeric("abc"), "Alphabetic string should not be numeric");
        assertFalse(ServiceSessionBean.isNumeric("12a"), "Alphanumeric string should not be numeric");
        assertFalse(ServiceSessionBean.isNumeric("a12"), "Alphanumeric string should not be numeric");
        assertFalse(ServiceSessionBean.isNumeric(" 123"), "String with leading space should not be numeric");
        assertFalse(ServiceSessionBean.isNumeric("123 "), "String with trailing space should not be numeric");
        assertFalse(ServiceSessionBean.isNumeric(" "), "Space string should not be numeric");
    }

    @Test
    @DisplayName("Should handle empty string edge case")
    public void testIsNumericEmptyString() {
        // Based on current implementation, empty string returns true
        // length is 0, and pos.getIndex() remains 0
        assertTrue(ServiceSessionBean.isNumeric(""), "Empty string returns true with current implementation");
    }

    @Test
    @DisplayName("Should throw NullPointerException for null input")
    public void testIsNumericNullInput() {
        assertThrows(NullPointerException.class, () -> {
            ServiceSessionBean.isNumeric(null);
        }, "Null input should throw NullPointerException");
    }
}
