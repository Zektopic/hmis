package com.divudi.ejb;

import com.divudi.core.entity.Department;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.facade.StockFacade;
import com.divudi.core.entity.ServiceSession;
import com.divudi.core.entity.channel.ArrivalRecord;
import com.divudi.core.entity.hr.FingerPrintRecord;
import com.divudi.core.facade.FingerPrintRecordFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StockHistoryRecorderTest {

    private StockHistoryRecorder recorder;
    private MockStockFacade mockStockFacade;
    private MockFingerPrintRecordFacade mockFacade;

    @BeforeEach
    public void setUp() {
        recorder = new StockHistoryRecorder();
        mockStockFacade = new MockStockFacade();
        recorder.setStockFacade(mockStockFacade);
        mockFacade = new MockFingerPrintRecordFacade();
        recorder.fingerPrintRecordFacade = mockFacade;
    }

    @Test
    public void testGetStockQty_withStandardItem() {
        // Arrange
        Item item = new Item();
        item.setName("Test Item");

        Department dept = new Department();
        dept.setName("Test Dept");

        // Act
        double result = recorder.getStockQty(item, dept);

        // Assert
        assertEquals(42.5, result, "Should return the value from the mock facade");
        assertEquals(1, mockStockFacade.getCallCount(), "Should call the facade once");

        String expectedSql = "select sum(s.stock) from Stock s where s.department=:d and s.itemBatch.item=:i";
        assertEquals(expectedSql, mockStockFacade.getLastJpql(), "JPQL should match");

        Map<String, Object> params = mockStockFacade.getLastParameters();
        assertNotNull(params, "Parameters map should not be null");
        assertEquals(2, params.size(), "Should have exactly two parameters");
        assertEquals(dept, params.get("d"), "Department parameter should match");
        assertEquals(item, params.get("i"), "Item parameter should match");
    }

    @Test
    public void testGetStockQty_withAmpp() {
        // Arrange
        Amp underlyingAmp = new Amp();
        underlyingAmp.setName("Underlying Amp");

        Ampp ampp = new Ampp();
        ampp.setName("Test Ampp");
        ampp.setAmp(underlyingAmp);

        Department dept = new Department();
        dept.setName("Test Dept");

        // Act
        double result = recorder.getStockQty(ampp, dept);

        // Assert
        assertEquals(42.5, result, "Should return the value from the mock facade");
        assertEquals(1, mockStockFacade.getCallCount(), "Should call the facade once");

        Map<String, Object> params = mockStockFacade.getLastParameters();
        assertNotNull(params, "Parameters map should not be null");
        assertEquals(2, params.size(), "Should have exactly two parameters");
        assertEquals(dept, params.get("d"), "Department parameter should match");

        // Critical part: it should have unwrapped the Ampp to get the Amp
        assertEquals(underlyingAmp, params.get("i"), "Item parameter should be the unwrapped Amp, not the Ampp");
    }

    @Test
    public void testFindArrivals_NullArrivalRecord() {
        mockFacade.setMockResult(null);
        ServiceSession ss = new ServiceSession();
        ss.setId(1L);
        ss.setSessionDate(new Date());

        Boolean result = recorder.findArrivals(ss);

        assertNull(result, "Should return null if arrival record is not found");

        assertTrue(mockFacade.lastJpql.contains("Select bs From ArrivalRecord bs"));
        assertEquals(ss.getId(), mockFacade.lastParams.get("ss"));
        assertEquals(ss.getSessionDate(), mockFacade.lastParams.get("ssDate"));
    }

    @Test
    public void testFindArrivals_ApprovedArrivalRecord() {
        ArrivalRecord mockRecord = new ArrivalRecord();
        mockRecord.setApproved(true);
        mockFacade.setMockResult(mockRecord);

        ServiceSession ss = new ServiceSession();
        ss.setId(1L);
        ss.setSessionDate(new Date());

        Boolean result = recorder.findArrivals(ss);

        assertTrue(result, "Should return true if arrival record is approved");
    }

    @Test
    public void testFindArrivals_NotApprovedArrivalRecord() {
        ArrivalRecord mockRecord = new ArrivalRecord();
        mockRecord.setApproved(false);
        mockFacade.setMockResult(mockRecord);

        ServiceSession ss = new ServiceSession();
        ss.setId(1L);
        ss.setSessionDate(new Date());

        Boolean result = recorder.findArrivals(ss);

        assertFalse(result, "Should return false if arrival record is not approved");
    }

    private static class MockStockFacade extends StockFacade {
        private int callCount = 0;
        private String lastJpql;
        private Map<String, Object> lastParameters;

        @Override
        public double findDoubleByJpql(String jpql, Map parameters) {
            this.callCount++;
            this.lastJpql = jpql;
            this.lastParameters = (Map<String, Object>) parameters;
            return 42.5;
        }

        public int getCallCount() {
            return callCount;
        }

        public String getLastJpql() {
            return lastJpql;
        }

        public Map<String, Object> getLastParameters() {
            return lastParameters;
        }
    }

    private class MockFingerPrintRecordFacade extends FingerPrintRecordFacade {
        private ArrivalRecord mockResult = null;
        private String lastJpql = null;
        private Map<String, Object> lastParams = null;

        public void setMockResult(ArrivalRecord mockResult) {
            this.mockResult = mockResult;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FingerPrintRecord findFirstByJpql(String jpql, Map<String, Object> parameters) {
            this.lastJpql = jpql;
            this.lastParams = parameters;
            return (FingerPrintRecord) (Object) mockResult;
        }
    }
}
