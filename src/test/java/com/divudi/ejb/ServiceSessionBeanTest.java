package com.divudi.ejb;

import com.divudi.core.entity.ServiceSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceSessionBeanTest {

    private ServiceSessionBean serviceSessionBean;

    @BeforeEach
    public void setUp() {
        serviceSessionBean = new ServiceSessionBean();
    }

    @Test
    public void testStringNumbersToIntsNullStringWithStartingNo() {
        ServiceSession ss = new ServiceSession();
        ss.setStartingNo(10);
        List<Integer> result = serviceSessionBean.stringNumbersToInts(null, ss);
        assertEquals(91, result.size());
        assertEquals(10, result.get(0));
        assertEquals(100, result.get(90));
    }

    @Test
    public void testStringNumbersToIntsNullStringWithoutStartingNo() {
        ServiceSession ss = new ServiceSession();
        ss.setStartingNo(0);
        List<Integer> result = serviceSessionBean.stringNumbersToInts(null, ss);
        assertEquals(100, result.size());
        assertEquals(1, result.get(0));
        assertEquals(100, result.get(99));
    }

    @Test
    public void testStringNumbersToIntsEmptyString() {
        ServiceSession ss = new ServiceSession();
        ss.setStartingNo(5);
        List<Integer> result = serviceSessionBean.stringNumbersToInts("   ", ss);
        assertEquals(96, result.size());
        assertEquals(5, result.get(0));
    }

    @Test
    public void testStringNumbersToIntsContainsGreaterThan() {
        ServiceSession ss = new ServiceSession();
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> 5", ss);
        assertEquals(95, result.size());
        assertEquals(6, result.get(0));
        assertEquals(100, result.get(94));
    }

    @Test
    public void testStringNumbersToIntsContainsGreaterThanWithFloat() {
        ServiceSession ss = new ServiceSession();
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> 5.5", ss);
        // The parser parses 5.5 as 5, or maybe fails to parse and defaults to 1.
        // In our manual test, "> 5.5" parsed as 1 because '5.5' is not fully numeric under integer parsing constraints without trailing error handling.
        // Let's verify what it returns exactly (manual test returned 1, so 1+1=2, 2 to 100).
        assertEquals(99, result.size());
        assertEquals(2, result.get(0));
    }

    @Test
    public void testStringNumbersToIntsContainsGreaterThanWithNonNumeric() {
        ServiceSession ss = new ServiceSession();
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> ABC", ss);
        // "ABC" is not numeric, so it doesn't trigger the addToIntList inside the if (isNumeric(s))
        assertTrue(result.isEmpty());
    }

    @Test
    public void testStringNumbersToIntsContainsDashValidRange() {
        ServiceSession ss = new ServiceSession();
        List<Integer> result = serviceSessionBean.stringNumbersToInts("5 - 10", ss);
        assertEquals(6, result.size());
        assertEquals(5, result.get(0));
        assertEquals(10, result.get(5));
    }

    @Test
    public void testStringNumbersToIntsContainsDashWithFloat() {
        ServiceSession ss = new ServiceSession();
        List<Integer> result = serviceSessionBean.stringNumbersToInts("5.5 - 10.5", ss);
        // The first float parses/fails to 1, second parses/fails to 1. Returns 1 to 1.
        assertEquals(1, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    public void testStringNumbersToIntsContainsDashMissingSecond() {
        ServiceSession ss = new ServiceSession();
        // NullPointerException expected since fromNo is assigned, toNo remains null
        assertThrows(NullPointerException.class, () -> {
            serviceSessionBean.stringNumbersToInts("5 - ", ss);
        });
    }

    @Test
    public void testStringNumbersToIntsContainsDashMissingFirst() {
        ServiceSession ss = new ServiceSession();
        // NullPointerException expected since fromNo is assigned (as the single number becomes fromNo), toNo remains null
        assertThrows(NullPointerException.class, () -> {
            serviceSessionBean.stringNumbersToInts("- 10", ss);
        });
    }

    @Test
    public void testIsNumericValid() {
        assertTrue(ServiceSessionBean.isNumeric("123"));
        assertTrue(ServiceSessionBean.isNumeric("123.45"));
    }

    @Test
    public void testIsNumericInvalid() {
        assertTrue(!ServiceSessionBean.isNumeric("ABC"));
        assertTrue(!ServiceSessionBean.isNumeric("12A"));
    }
}
