package com.divudi.ejb;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StockHistoryRecorderTest {
    private StockHistoryRecorder recorder;

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
            // The StockHistoryRecorder code performs an unsafe cast from FingerPrintRecord to ArrivalRecord:
            // arrivalRecord = (ArrivalRecord) fingerPrintRecordFacade.findFirstByJpql(sql, hh);
            // In a real environment this code works because the query returns an ArrivalRecord
            // (since the JPQL is 'Select bs From ArrivalRecord bs').
            // To emulate this in a mock without causing ClassCastException, we must return the ArrivalRecord
            // disguised as the expected generic type FingerPrintRecord.
            return (FingerPrintRecord) (Object) mockResult;
        }
    }

    private MockFingerPrintRecordFacade mockFacade;

    @BeforeEach
    public void setup() {
        recorder = new StockHistoryRecorder();
        mockFacade = new MockFingerPrintRecordFacade();
        recorder.fingerPrintRecordFacade = mockFacade;
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
}
