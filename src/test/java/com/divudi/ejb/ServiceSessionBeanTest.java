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
    public void testStringNumbersToInts_NullString_StartingNoGreaterThanZero() {
        serviceSession.setStartingNo(50);
        List<Integer> result = serviceSessionBean.stringNumbersToInts(null, serviceSession);

        assertNotNull(result);
        assertEquals(51, result.size()); // 50 to 100 inclusive
        assertEquals(50, result.get(0));
        assertEquals(100, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_EmptyString_StartingNoZero() {
        serviceSession.setStartingNo(0);
        List<Integer> result = serviceSessionBean.stringNumbersToInts("   ", serviceSession);

        assertNotNull(result);
        assertEquals(100, result.size()); // 1 to 100 inclusive
        assertEquals(1, result.get(0));
        assertEquals(100, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_ContainsGreaterThan() {
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> 50", serviceSession);

        assertNotNull(result);
        assertEquals(50, result.size()); // 51 to 100 inclusive
        assertEquals(51, result.get(0));
        assertEquals(100, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_ContainsGreaterThan_InvalidNumber() {
        // 'abc' is not numeric, so isNumeric returns false, and the loop finishes.
        // Then it returns an empty list.
        List<Integer> result = serviceSessionBean.stringNumbersToInts("> abc", serviceSession);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testStringNumbersToInts_ContainsHyphen() {
        List<Integer> result = serviceSessionBean.stringNumbersToInts("10 - 20", serviceSession);

        assertNotNull(result);
        assertEquals(11, result.size()); // 10 to 20 inclusive
        assertEquals(10, result.get(0));
        assertEquals(20, result.get(result.size() - 1));
    }

    @Test
    public void testStringNumbersToInts_ContainsHyphen_InvalidNumbers() {
        // 'abc' and 'def' are not numeric, so they are ignored.
        // fromNo and toNo remain null.
        // addToIntList will be called with (null, null, nits), which throws NPE.
        // In java, unboxing null Integer to int throws NPE in for loop `for (int i = fromInt; i <= toInt; i++)`
        assertThrows(NullPointerException.class, () -> {
            serviceSessionBean.stringNumbersToInts("abc - def", serviceSession);
        });
    }

    @Test
    public void testStringNumbersToInts_NoMatch() {
        List<Integer> result = serviceSessionBean.stringNumbersToInts("50", serviceSession);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
