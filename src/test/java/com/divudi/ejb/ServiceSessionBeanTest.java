package com.divudi.ejb;

import com.divudi.core.data.SessionNumberType;
import com.divudi.core.entity.BillSession;
import com.divudi.core.entity.Category;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.ServiceSession;
import com.divudi.core.entity.channel.SessionInstance;
import com.divudi.core.facade.BillSessionFacade;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.TemporalType;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceSessionBeanTest {

    private static class MockBillSessionFacade extends BillSessionFacade {
        public List<BillSession> mockBillSessions = new ArrayList<>();
        public List<Integer> mockLightResults = new ArrayList<>();

        @Override
        public List<BillSession> findByJpql(String jpql, Map m, TemporalType temporalType) {
            return mockBillSessions;
        }

        @Override
        public List findLightsByJpql(String jpql, Map m) {
            return mockLightResults;
        }
    }

    @Test
    public void testIsNumeric() {
        assertTrue(ServiceSessionBean.isNumeric("123"));
        assertTrue(ServiceSessionBean.isNumeric("0"));
        assertFalse(ServiceSessionBean.isNumeric("123a"));
        assertFalse(ServiceSessionBean.isNumeric("abc"));
    }

    @Test
    public void testStringNumbersToInts_Empty() {
        ServiceSessionBean bean = new ServiceSessionBean();
        ServiceSession ss = new ServiceSession();
        ss.setStartingNo(0);

        List<Integer> result = bean.stringNumbersToInts("", ss);
        assertEquals(100, result.size());
        assertEquals(1, result.get(0).intValue());
        assertEquals(100, result.get(99).intValue());
    }

    @Test
    public void testStringNumbersToInts_EmptyWithStartingNo() {
        ServiceSessionBean bean = new ServiceSessionBean();
        ServiceSession ss = new ServiceSession();
        ss.setStartingNo(50);

        List<Integer> result = bean.stringNumbersToInts(" ", ss);
        assertEquals(51, result.size());
        assertEquals(50, result.get(0).intValue());
        assertEquals(100, result.get(50).intValue());
    }

    @Test
    public void testStringNumbersToInts_GreaterThan() {
        ServiceSessionBean bean = new ServiceSessionBean();
        ServiceSession ss = new ServiceSession();

        List<Integer> result = bean.stringNumbersToInts("10 >", ss);
        assertEquals(90, result.size());
        assertEquals(11, result.get(0).intValue());
        assertEquals(100, result.get(89).intValue());
    }

    @Test
    public void testStringNumbersToInts_Range() {
        ServiceSessionBean bean = new ServiceSessionBean();
        ServiceSession ss = new ServiceSession();

        List<Integer> result = bean.stringNumbersToInts("5 - 10", ss);
        assertEquals(6, result.size());
        assertEquals(5, result.get(0).intValue());
        assertEquals(10, result.get(5).intValue());
    }

    @Test
    public void testGetBillSessions_NullItem() {
        ServiceSessionBean bean = new ServiceSessionBean();
        assertNull(bean.getBillSessions(null, new Date()));
    }

    @Test
    public void testGetBillSessions_NullSessionNumberType() {
        ServiceSessionBean bean = new ServiceSessionBean();
        Item item = new Item() {};
        item.setSessionNumberType(SessionNumberType.None);
        List<BillSession> result = bean.getBillSessions(item, new Date());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetBillSessions_ByCategory_NoParent() {
        ServiceSessionBean bean = new ServiceSessionBean();
        MockBillSessionFacade facade = new MockBillSessionFacade();
        bean.setBillSessionFacade(facade);

        BillSession bs1 = new BillSession();
        facade.mockBillSessions.add(bs1);

        Item item = new Item() {};
        item.setSessionNumberType(SessionNumberType.ByCategory);
        Category cat = new Category();
        cat.setId(1L);
        item.setCategory(cat);

        List<BillSession> result = bean.getBillSessions(item, new Date());
        assertEquals(1, result.size());
        assertEquals(bs1, result.get(0));
    }

    @Test
    public void testGetBillSessions_ByItem() {
        ServiceSessionBean bean = new ServiceSessionBean();
        MockBillSessionFacade facade = new MockBillSessionFacade();
        bean.setBillSessionFacade(facade);

        BillSession bs1 = new BillSession();
        facade.mockBillSessions.add(bs1);

        Item item = new Item() {};
        item.setId(1L);
        item.setSessionNumberType(SessionNumberType.ByItem);

        List<BillSession> result = bean.getBillSessions(item, new Date());
        assertEquals(1, result.size());
        assertEquals(bs1, result.get(0));
    }

    @Test
    public void testGetNextAvailableReservedNumber_NullSelected() {
        ServiceSessionBean bean = new ServiceSessionBean();
        MockBillSessionFacade facade = new MockBillSessionFacade();
        bean.setBillSessionFacade(facade);

        facade.mockLightResults = Arrays.asList(2, 4);

        SessionInstance si = new SessionInstance();
        List<Integer> reservedNumbers = Arrays.asList(1, 2, 3, 4, 5);

        Integer result = bean.getNextAvailableReservedNumber(si, reservedNumbers, null);
        assertEquals(1, result);
    }

    @Test
    public void testGetNextAvailableReservedNumber_SelectedAvailable() {
        ServiceSessionBean bean = new ServiceSessionBean();
        MockBillSessionFacade facade = new MockBillSessionFacade();
        bean.setBillSessionFacade(facade);

        facade.mockLightResults = Arrays.asList(2, 4);

        SessionInstance si = new SessionInstance();
        List<Integer> reservedNumbers = Arrays.asList(1, 2, 3, 4, 5);

        Integer result = bean.getNextAvailableReservedNumber(si, reservedNumbers, 5);
        assertEquals(5, result);
    }

    @Test
    public void testGetNextAvailableReservedNumber_SelectedBookedFallbackToMin() {
        ServiceSessionBean bean = new ServiceSessionBean();
        MockBillSessionFacade facade = new MockBillSessionFacade();
        bean.setBillSessionFacade(facade);

        facade.mockLightResults = Arrays.asList(2, 4);

        SessionInstance si = new SessionInstance();
        List<Integer> reservedNumbers = Arrays.asList(1, 2, 3, 4, 5);

        Integer result = bean.getNextAvailableReservedNumber(si, reservedNumbers, 2);
        // 2 is booked, so it will fallback to minimum available which is 1
        assertEquals(1, result);
    }

}
