package com.divudi.performance;

import com.divudi.bean.common.SessionController;
import com.divudi.bean.pharmacy.PharmacyController;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.WebUser;
import com.divudi.core.facade.ItemFacade;
import com.divudi.core.light.pharmacy.PharmaceuticalItemLight;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Mock ItemFacade to track queries
class MockPerfItemFacade extends ItemFacade {
    public List<Item> items = new ArrayList<>();
    public List<Item> batchEditedItems = new ArrayList<>();
    public int findCallCount = 0;
    public int findByJpqlCallCount = 0;

    @Override
    public Item find(Object id) {
        findCallCount++;
        for (Item item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public List<Item> findByJpql(String jpql, Map<String, Object> parameters) {
        findByJpqlCallCount++;
        List<Long> ids = (List<Long>) parameters.get("ids");
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (ids.contains(item.getId())) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public void batchEdit(List<Item> entities) {
        batchEditedItems.addAll(entities);
    }
}

// Custom session controller to bypass dependency injection
class MockSessionController extends SessionController {
    @Override
    public WebUser getLoggedUser() {
        return new WebUser();
    }
}

// Custom JSF util simulation to avoid JSF Context Error
class TestPerfPharmacyController extends PharmacyController {
    private MockPerfItemFacade mockItemFacade;
    private MockSessionController mockSessionController;

    public TestPerfPharmacyController(MockPerfItemFacade mockItemFacade) {
        this.mockItemFacade = mockItemFacade;
        this.mockSessionController = new MockSessionController();
    }

    @Override
    public ItemFacade getItemFacade() {
        return mockItemFacade;
    }

    @Override
    public SessionController getSessionController() {
        return mockSessionController;
    }

    @Override
    public void fillPharmaceuticalLights() {
        // No-op to avoid DB query in JSF util
    }

    // Simulating JSF UI interactions
    public void simulateOptimizedBulkDelete() {
        if (getSelectedLights() == null || getSelectedLights().isEmpty()) {
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (PharmaceuticalItemLight l : getSelectedLights()) {
            if (l.getId() != null) {
                ids.add(l.getId());
            }
        }

        if (ids.isEmpty()) {
            return;
        }

        List<Item> itemsToUpdate = new ArrayList<>();

        int batchSize = 500;
        for (int i = 0; i < ids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ids.size());
            List<Long> batchIds = ids.subList(i, end);

            String jpql = "SELECT i FROM Item i WHERE i.id IN :ids";
            Map<String, Object> params = new HashMap<>();
            params.put("ids", batchIds);

            List<Item> items = getItemFacade().findByJpql(jpql, params);
            if (items != null) {
                for (Item item : items) {
                    item.setRetired(true);
                    item.setRetirer(getSessionController().getLoggedUser());
                    item.setRetiredAt(new Date());
                    itemsToUpdate.add(item);
                }
            }
        }

        getItemFacade().batchEdit(itemsToUpdate);
        fillPharmaceuticalLights();
    }

    public void simulateUnoptimizedBulkDelete() {
        if (getSelectedLights() == null || getSelectedLights().isEmpty()) {
            return;
        }
        List<Item> itemsToUpdate = new ArrayList<>();
        for (PharmaceuticalItemLight l : getSelectedLights()) {
            if (l.getId() == null) {
                continue;
            }
            Item i = getItemFacade().find(l.getId());
            if (i == null) {
                continue;
            }
            i.setRetired(true);
            i.setRetirer(getSessionController().getLoggedUser());
            i.setRetiredAt(new Date());
            itemsToUpdate.add(i);
        }
        getItemFacade().batchEdit(itemsToUpdate);
        fillPharmaceuticalLights();
    }
}

public class PharmacyDeleteSelectedLightPerfTest {

    @Test
    public void testBulkDeleteOptimization() {
        MockPerfItemFacade unoptimizedFacade = new MockPerfItemFacade();
        TestPerfPharmacyController unoptimizedController = new TestPerfPharmacyController(unoptimizedFacade);

        MockPerfItemFacade optimizedFacade = new MockPerfItemFacade();
        TestPerfPharmacyController optimizedController = new TestPerfPharmacyController(optimizedFacade);

        int numItems = 1000;
        List<PharmaceuticalItemLight> selectedLights = new ArrayList<>();

        // Setup initial items
        for (long i = 1; i <= numItems; i++) {
            Item item = new Item();
            item.setId(i);
            unoptimizedFacade.items.add(item);
            optimizedFacade.items.add(item);

            PharmaceuticalItemLight light = new PharmaceuticalItemLight();
            light.setId(i);
            selectedLights.add(light);
        }

        unoptimizedController.setSelectedLights(selectedLights);
        optimizedController.setSelectedLights(selectedLights);

        // Measure unoptimized
        long startTimeUnoptimized = System.currentTimeMillis();
        unoptimizedController.simulateUnoptimizedBulkDelete();
        long unoptimizedTime = System.currentTimeMillis() - startTimeUnoptimized;

        // Measure optimized
        long startTimeOptimized = System.currentTimeMillis();
        optimizedController.simulateOptimizedBulkDelete();
        long optimizedTime = System.currentTimeMillis() - startTimeOptimized;

        System.out.println("Time taken for 1000 items:");
        System.out.println("Unoptimized O(N) approach: " + unoptimizedTime + "ms");
        System.out.println("Optimized O(1) approach: " + optimizedTime + "ms");
        System.out.println("Unoptimized DB Calls: " + unoptimizedFacade.findCallCount);
        System.out.println("Optimized DB Calls: " + optimizedFacade.findByJpqlCallCount);

        assertEquals(numItems, unoptimizedFacade.findCallCount, "Unoptimized approach should make N DB calls");
        assertEquals(2, optimizedFacade.findByJpqlCallCount, "Optimized approach should make 2 DB calls for 1000 items (chunk size 500)");

        assertEquals(numItems, unoptimizedFacade.batchEditedItems.size(), "Unoptimized approach should update all items");
        assertEquals(numItems, optimizedFacade.batchEditedItems.size(), "Optimized approach should update all items");
    }
}