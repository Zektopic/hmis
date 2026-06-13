package com.divudi.performance;

import com.divudi.bean.pharmacy.PharmacyBillSearch;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.CancelledBill;
import com.divudi.core.entity.Payment;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.bean.common.SessionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PharmacyCancelBillItemsPerfTest {

    private PharmacyBillSearch pharmacyBillSearch;
    private MockBillFacade billFacade;
    private MockBillItemFacade billItemFacade;
    private Bill mockBill;

    @BeforeEach
    public void setup() {
        pharmacyBillSearch = new PharmacyBillSearch() {
            @Override
            public BillFacade getBillFacade() {
                return billFacade;
            }

            @Override
            public BillItemFacade getBillItemFacede() {
                return billItemFacade;
            }

            @Override
            public SessionController getSessionController() {
                return new SessionController() {
                    @Override
                    public com.divudi.core.entity.WebUser getLoggedUser() {
                        return null;
                    }
                };
            }
        };

        billFacade = new MockBillFacade();
        billItemFacade = new MockBillItemFacade();

        mockBill = new Bill();
        List<BillItem> billItems = new ArrayList<>();
        // 500 items to simulate a large bill
        for (int i = 0; i < 500; i++) {
            BillItem item = new BillItem();
            PharmaceuticalBillItem phItem = new PharmaceuticalBillItem();
            item.setPharmaceuticalBillItem(phItem);
            billItems.add(item);
        }
        mockBill.setBillItems(billItems);
        pharmacyBillSearch.setBill(mockBill);
    }

    @Test
    @DisplayName("Performance of pharmacyCancelBillItems")
    public void testPharmacyCancelBillItemsPerf() {
        CancelledBill can = new CancelledBill();
        can.setBillItems(new ArrayList<>());
        List<Payment> payments = new ArrayList<>();

        long start = System.currentTimeMillis();
        try {
            java.lang.reflect.Method method = PharmacyBillSearch.class.getDeclaredMethod("pharmacyCancelBillItems", CancelledBill.class, List.class);
            method.setAccessible(true);
            method.invoke(pharmacyBillSearch, can, payments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("Execution time: " + time + "ms");

        System.out.println("Creates: " + billItemFacade.createCount);
        System.out.println("Batch creates: " + billItemFacade.batchCreateCount);

        // Ensure no functionality is broken
        assertTrue(can.getBillItems().size() == 500);
    }

    class MockBillFacade extends BillFacade {
        int editCount = 0;
        @Override
        public void edit(Bill entity) {
            editCount++;
        }
    }

    class MockBillItemFacade extends BillItemFacade {
        int createCount = 0;
        int batchCreateCount = 0;
        @Override
        public void create(BillItem entity) {
            createCount++;
            // Simulate network/DB delay
            try { Thread.sleep(2); } catch (Exception e) {}
        }

        @Override
        public void batchCreate(List<BillItem> entities) {
            batchCreateCount++;
            try { Thread.sleep(5); } catch (Exception e) {}
        }

        @Override
        public void batchCreate(List<BillItem> entities, int batchSize) {
            batchCreateCount++;
            try { Thread.sleep(5); } catch (Exception e) {}
        }
    }
}
