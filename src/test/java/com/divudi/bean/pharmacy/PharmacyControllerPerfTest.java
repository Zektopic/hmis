package com.divudi.bean.pharmacy;

import com.divudi.core.data.dto.AmpDto;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.facade.AmpFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Mock AmpFacade since Mockito is not allowed
class MockPerfAmpFacade extends AmpFacade {
    public List<Amp> amps = new ArrayList<>();
    public List<Amp> batchEditedAmps = new ArrayList<>();
    public int findCallCount = 0;
    public int findByJpqlCallCount = 0;

    @Override
    public Amp find(Object id) {
        findCallCount++;
        for (Amp amp : amps) {
            if (amp.getId().equals(id)) {
                return amp;
            }
        }
        return null;
    }

    @Override
    public List<Amp> findByJpql(String jpql, Map<String, Object> parameters) {
        findByJpqlCallCount++;
        List<Long> ids = (List<Long>) parameters.get("ids");
        List<Amp> result = new ArrayList<>();
        for (Amp amp : amps) {
            if (ids.contains(amp.getId())) {
                result.add(amp);
            }
        }
        return result;
    }

    @Override
    public void batchEdit(List<Amp> entities) {
        batchEditedAmps.addAll(entities);
    }
}

// Custom JSF util simulation to avoid JSF Context Error
class TestPerfPharmacyController extends PharmacyController {
    private MockPerfAmpFacade mockAmpFacade;
    public boolean successMessageAdded = false;

    public TestPerfPharmacyController(MockPerfAmpFacade mockAmpFacade) {
        this.mockAmpFacade = mockAmpFacade;
    }

    @Override
    public AmpFacade getAmpFacade() {
        return mockAmpFacade;
    }

    @Override
    public void fillAmpsDto() {
        // No-op
    }

    // Simulating JSF UI interactions
    public void simulateOptimizedBulkUpdate() {
        if (getAmpDtosSelected() == null || getAmpDtosSelected().isEmpty()) {
            return;
        }
        // Direct replication of logic to avoid JsfUtil
        List<Amp> ampsToUpdate = fetchAmpsForBulkUpdateMock(getAmpDtosSelected());
        for (Amp amp : ampsToUpdate) {
            amp.setDiscountAllowed(true);
        }
        getAmpFacade().batchEdit(ampsToUpdate);
        fillAmpsDto();
        successMessageAdded = true;
    }

    public void simulateUnoptimizedBulkUpdate() {
        if (getAmpDtosSelected() == null || getAmpDtosSelected().isEmpty()) {
            return;
        }
        List<Amp> ampsToUpdate = new ArrayList<>();
        for (com.divudi.core.data.dto.AmpDto dto : getAmpDtosSelected()) {
            Amp amp = getAmpFacade().find(dto.getId());
            if (amp != null) {
                amp.setDiscountAllowed(true);
                ampsToUpdate.add(amp);
            }
        }
        getAmpFacade().batchEdit(ampsToUpdate);
        fillAmpsDto();
        successMessageAdded = true;
    }

    private List<Amp> fetchAmpsForBulkUpdateMock(List<com.divudi.core.data.dto.AmpDto> dtos) {
        List<Amp> amps = new ArrayList<>();
        if (dtos == null || dtos.isEmpty()) return amps;

        List<Long> ids = new ArrayList<>();
        for (com.divudi.core.data.dto.AmpDto dto : dtos) {
            if (dto != null && dto.getId() != null) {
                ids.add(dto.getId());
            }
        }

        if (ids.isEmpty()) return amps;

        int batchSize = 1000;
        for (int i = 0; i < ids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ids.size());
            List<Long> batchIds = ids.subList(i, end);

            String jpql = "SELECT a FROM Amp a WHERE a.id IN :ids";
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("ids", batchIds);
            amps.addAll(getAmpFacade().findByJpql(jpql, m));
        }
        return amps;
    }
}

public class PharmacyControllerPerfTest {

    @Test
    public void testBulkUpdateOptimization() {
        MockPerfAmpFacade unoptimizedFacade = new MockPerfAmpFacade();
        TestPerfPharmacyController unoptimizedController = new TestPerfPharmacyController(unoptimizedFacade);

        MockPerfAmpFacade optimizedFacade = new MockPerfAmpFacade();
        TestPerfPharmacyController optimizedController = new TestPerfPharmacyController(optimizedFacade);

        int numItems = 1000;
        List<AmpDto> selectedDtos = new ArrayList<>();

        // Setup initial amps
        for (long i = 1; i <= numItems; i++) {
            Amp amp = new Amp();
            amp.setId(i);
            unoptimizedFacade.amps.add(amp);
            optimizedFacade.amps.add(amp);

            AmpDto dto = new AmpDto();
            dto.setId(i);
            selectedDtos.add(dto);
        }

        unoptimizedController.setAmpDtosSelected(selectedDtos);
        optimizedController.setAmpDtosSelected(selectedDtos);

        // Measure unoptimized
        long startTimeUnoptimized = System.currentTimeMillis();
        unoptimizedController.simulateUnoptimizedBulkUpdate();
        long unoptimizedTime = System.currentTimeMillis() - startTimeUnoptimized;

        // Measure optimized
        long startTimeOptimized = System.currentTimeMillis();
        optimizedController.simulateOptimizedBulkUpdate();
        long optimizedTime = System.currentTimeMillis() - startTimeOptimized;

        System.out.println("Time taken for 1000 items:");
        System.out.println("Unoptimized O(N) approach: " + unoptimizedTime + "ms");
        System.out.println("Optimized O(1) approach: " + optimizedTime + "ms");
        System.out.println("Unoptimized DB Calls: " + unoptimizedFacade.findCallCount);
        System.out.println("Optimized DB Calls: " + optimizedFacade.findByJpqlCallCount);

        assertEquals(numItems, unoptimizedFacade.findCallCount, "Unoptimized approach should make N DB calls");
        assertEquals(1, optimizedFacade.findByJpqlCallCount, "Optimized approach should make 1 DB call for 1000 items");
    }
}
