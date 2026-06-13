package com.divudi.bean.pharmacy;

import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.CancelledBill;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.WebUser;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.bean.common.SessionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PharmacyBillSearchPerformanceTest {

    private PharmacyBillSearch pharmacyBillSearch;
    private long createCount = 0;
    private long editCount = 0;
    private long batchCreateCount = 0;

    // Mock facades
    private class MockPharmaceuticalBillItemFacade extends PharmaceuticalBillItemFacade {
        @Override
        public void create(PharmaceuticalBillItem entity) {
            createCount++;
        }
        @Override
        public void edit(PharmaceuticalBillItem entity) {
            editCount++;
        }
        @Override
        public void batchCreate(List<PharmaceuticalBillItem> entities) {
            batchCreateCount++;
        }
    }

    private class MockBillItemFacade extends BillItemFacade {
        @Override
        public void create(BillItem entity) {
            createCount++;
        }
        @Override
        public void edit(BillItem entity) {
            editCount++;
        }
        @Override
        public void batchCreate(List<BillItem> entities) {
            batchCreateCount++;
        }
    }

    private class MockBillFacade extends BillFacade {
        @Override
        public void edit(Bill entity) {
            editCount++;
        }
    }

    private class MockSessionController extends SessionController {
        @Override
        public WebUser getLoggedUser() {
            return new WebUser();
        }
    }

    @BeforeEach
    public void setup() {
        pharmacyBillSearch = new PharmacyBillSearch() {
            private MockPharmaceuticalBillItemFacade pharmFacade = new MockPharmaceuticalBillItemFacade();
            private MockBillItemFacade billItemFacade = new MockBillItemFacade();
            private MockBillFacade billFacade = new MockBillFacade();
            private MockSessionController sessionController = new MockSessionController();

            @Override
            public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
                return pharmFacade;
            }

            @Override
            public BillItemFacade getBillItemFacede() {
                return billItemFacade;
            }

            @Override
            public BillFacade getBillFacade() {
                return billFacade;
            }

            @Override
            public SessionController getSessionController() {
                return sessionController;
            }
        };
        createCount = 0;
        editCount = 0;
        batchCreateCount = 0;
    }

    @Test
    public void testPharmacyCancelBillItemsPerformance() throws Exception {
        // Prepare data
        int numItems = 1000;
        Bill originalBill = new Bill();
        List<BillItem> billItems = new ArrayList<>();

        for (int i = 0; i < numItems; i++) {
            BillItem item = new BillItem();
            PharmaceuticalBillItem pharmItem = new PharmaceuticalBillItem();
            item.setPharmaceuticalBillItem(pharmItem);
            billItems.add(item);
        }
        originalBill.setBillItems(billItems);

        pharmacyBillSearch.setBill(originalBill);

        CancelledBill cancelledBill = new CancelledBill();
        cancelledBill.setBillItems(new ArrayList<>());

        // Measure time
        long startTime = System.currentTimeMillis();

        // Use reflection to call the private method
        java.lang.reflect.Method method = PharmacyBillSearch.class.getDeclaredMethod("pharmacyCancelBillItems", CancelledBill.class);
        method.setAccessible(true);
        method.invoke(pharmacyBillSearch, cancelledBill);

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken for " + numItems + " items: " + (endTime - startTime) + "ms");
        System.out.println("Facade Create Calls: " + createCount);
        System.out.println("Facade Edit Calls: " + editCount);
        System.out.println("Facade Batch Create Calls: " + batchCreateCount);
    }
}
