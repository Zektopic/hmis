package com.divudi.ejb;

import com.divudi.core.entity.Department;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.facade.StockFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StockHistoryRecorderTest {

    private StockHistoryRecorder recorder;
    private MockStockFacade mockStockFacade;

    @BeforeEach
    public void setUp() {
        recorder = new StockHistoryRecorder();
        mockStockFacade = new MockStockFacade();
        recorder.setStockFacade(mockStockFacade);
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

    /**
     * Mock facade to intercept the JPA calls and verify parameters without needing Mockito or a DB.
     */
    private static class MockStockFacade extends StockFacade {
        private int callCount = 0;
        private String lastJpql;
        private Map<String, Object> lastParameters;

        @Override
        public double findDoubleByJpql(String jpql, Map parameters) {
            this.callCount++;
            this.lastJpql = jpql;
            this.lastParameters = (Map<String, Object>) parameters;

            // Return a fixed value so we can verify it's passed through correctly
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
}
