package com.divudi.performance;

import com.divudi.bean.pharmacy.PharmacyController;


import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.dto.AmpDto;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.facade.AmpFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PharmacyControllerPerfTest {

    private PharmacyController controller;
    private MockAmpFacade mockAmpFacade;

    @BeforeEach
    public void setup() {
        controller = new PharmacyController() {
            @Override
            public void fillAmpsDto() {
                // Do nothing in test
            }
        };
        mockAmpFacade = new MockAmpFacade();
        controller.setAmpFacade(mockAmpFacade);
    }

    @Test
    public void testBulkUpdateDiscountAllowedDto_Optimization() {

        // Setup data
        int numItems = 500;
        List<AmpDto> selectedDtos = new ArrayList<>();
        List<Amp> allAmps = new ArrayList<>();

        for (long i = 1; i <= numItems; i++) {
            AmpDto dto = new AmpDto(i, "Amp " + i, "Code " + i, "Barcode " + i, false);
            selectedDtos.add(dto);

            Amp amp = new Amp();
            amp.setId(i);
            amp.setDiscountAllowed(false); // initially false
            allAmps.add(amp);
        }

        controller.setAmpDtosSelected(selectedDtos);
        mockAmpFacade.setAllAmps(allAmps);

        // Ensure no interactions yet
        assertEquals(0, mockAmpFacade.getFindCallCount());
        assertEquals(0, mockAmpFacade.getFindByJpqlCallCount());

        // Execute the method under test
        try {
            controller.bulkUpdateDiscountAllowedDto();
        } catch (ExceptionInInitializerError e) {
            // Expected since JsfUtil fails without a real JSF context, but the DB logic is what we care about
            // It happens after the DB operations, so we can still verify
        } catch (NoClassDefFoundError e) {
            // Also expected for JSF issues
        }

        // Assertions
        // The optimization should have changed from N find() calls to ~N/1000 findByJpql() calls

        // Expected: 0 find() calls instead of N
        assertEquals(0, mockAmpFacade.getFindCallCount(), "Should not call find() inside loop (N+1 query issue)");

        // Expected: 1 findByJpql() call for 500 items (since chunk size is 1000)
        assertTrue(mockAmpFacade.getFindByJpqlCallCount() > 0, "Should use findByJpql() with IN clause");
        assertEquals(1, mockAmpFacade.getFindByJpqlCallCount(), "Should process 500 items in a single chunk");

        // Ensure batchEdit was called
        assertTrue(mockAmpFacade.isBatchEditCalled(), "batchEdit should be called");

        // Ensure the items were actually modified
        int countAllowed = 0;
        for (Amp amp : mockAmpFacade.getUpdatedAmps()) {
            if (amp.getDiscountAllowed() != null && amp.getDiscountAllowed()) {
                countAllowed++;
            }
        }
        assertEquals(numItems, countAllowed, "All processed items should have discountAllowed set to true");
    }

    // A simple mock facade to count method invocations
    private static class MockAmpFacade extends AmpFacade {
        private int findCallCount = 0;
        private int findByJpqlCallCount = 0;
        private boolean batchEditCalled = false;
        private List<Amp> allAmps = new ArrayList<>();
        private List<Amp> updatedAmps = new ArrayList<>();

        public void setAllAmps(List<Amp> allAmps) {
            this.allAmps = allAmps;
        }

        public int getFindCallCount() {
            return findCallCount;
        }

        public int getFindByJpqlCallCount() {
            return findByJpqlCallCount;
        }

        public boolean isBatchEditCalled() {
            return batchEditCalled;
        }

        public List<Amp> getUpdatedAmps() {
            return updatedAmps;
        }

        @Override
        public Amp find(Object id) {
            findCallCount++;
            for (Amp amp : allAmps) {
                if (amp.getId().equals(id)) {
                    return amp;
                }
            }
            return null;
        }

        @Override
        public List<Object> findLightsByJpql(String jpql) { return new ArrayList<>(); }
        @Override
        public List<Amp> findByJpql(String jpql, Map<String, Object> parameters) {
            findByJpqlCallCount++;

            // Very simple mock implementation to return items matching IDs
            List<Amp> result = new ArrayList<>();
            if (parameters.containsKey("ids")) {
                List<Long> ids = (List<Long>) parameters.get("ids");
                for (Amp amp : allAmps) {
                    if (ids.contains(amp.getId())) {
                        result.add(amp);
                    }
                }
            }
            return result;
        }

        public void batchEdit(List<Amp> entities) {
            batchEditCalled = true;
            updatedAmps.addAll(entities);
        }
    }
}
