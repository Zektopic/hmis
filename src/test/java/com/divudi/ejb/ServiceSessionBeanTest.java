package com.divudi.ejb;

import com.divudi.core.entity.ServiceSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceSessionBeanTest {

    private ServiceSessionBean serviceSessionBean;
    private ServiceSession serviceSession;

    @BeforeEach
    public void setUp() {
        serviceSessionBean = new ServiceSessionBean();
        serviceSession = new ServiceSession();
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
        assertEquals(99, result.size());
        assertEquals(2, result.get(0));
    }

    @Test
    public void testStringNumbersToIntsContainsGreaterThanWithNonNumeric() {
        ServiceSession ss = new ServiceSession();
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> ABC", ss);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testStringNumbersToInts_NullString_StartingNoGreaterThanZero() {
        serviceSession.setStartingNo(50);
        List<Integer> result = serviceSessionBean.stringNumbersToInts(null, serviceSession);
        assertNotNull(result);
        assertEquals(51, result.size());
        assertEquals(50, result.get(0));
        assertEquals(100, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_EmptyString_StartingNoZero() {
        serviceSession.setStartingNo(0);
        List<Integer> result = serviceSessionBean.stringNumbersToInts("   ", serviceSession);
        assertNotNull(result);
        assertEquals(100, result.size());
        assertEquals(1, result.get(0));
        assertEquals(100, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_ContainsGreaterThan() {
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> 50", serviceSession);
        assertNotNull(result);
        assertEquals(50, result.size());
        assertEquals(51, result.get(0));
        assertEquals(100, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_ContainsGreaterThan_InvalidNumber() {
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> abc", serviceSession);
        assertNotNull(result);
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
        assertEquals(1, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    public void testStringNumbersToIntsContainsDashMissingSecond() {
        ServiceSession ss = new ServiceSession();
        assertThrows(NullPointerException.class, () -> {
            serviceSessionBean.stringNumbersToInts("5 - ", ss);
        });
    }

    @Test
    public void testStringNumbersToInts_ContainsHyphen() {
        List<Integer> result = serviceSessionBean.stringNumbersToInts("10 - 20", serviceSession);
        assertNotNull(result);
        assertEquals(11, result.size());
        assertEquals(10, result.get(0));
        assertEquals(20, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_ContainsHyphen_InvalidNumbers() {
        assertThrows(NullPointerException.class, () -> {
            serviceSessionBean.stringNumbersToInts("abc - def", serviceSession);
        });
    }

    @Test
    public void testStringNumbersToIntsContainsDashMissingFirst() {
        ServiceSession ss = new ServiceSession();
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
        assertFalse(ServiceSessionBean.isNumeric("ABC"));
        assertFalse(ServiceSessionBean.isNumeric("12A"));
    }

    @Test
    public void testStringNumbersToInts_NoMatch() {
        List<Integer> result = serviceSessionBean.stringNumbersToInts("50", serviceSession);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
