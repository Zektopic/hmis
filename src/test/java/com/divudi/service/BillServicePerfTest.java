package com.divudi.service;

import com.divudi.core.entity.Bill;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.PaymentMethod;
import com.divudi.core.facade.DepartmentFacade;
import com.divudi.core.facade.InstitutionFacade;
import com.divudi.core.facade.ItemFacade;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BillServicePerfTest {

    @Test
    public void testConvertJsonToBillNPlusOne() throws Exception {
        class MockItemFacade extends ItemFacade {
            int findCalls = 0;
            int findByJpqlCalls = 0;

            @Override
            public Item find(Object id) {
                findCalls++;
                Item item = new Item();
                item.setId((Long) id);
                return item;
            }

            @Override
            public List<Item> findByJpql(String jpql, Map<String, Object> parameters) {
                findByJpqlCalls++;
                List<Item> items = new ArrayList<>();
                List<Long> ids = (List<Long>) parameters.get("ids");
                if (ids != null) {
                    for (Long id : ids) {
                        Item item = new Item();
                        item.setId(id);
                        items.add(item);
                    }
                }
                return items;
            }
        }

        MockItemFacade itemFacade = new MockItemFacade();

        BillService billService = new BillService();

        java.lang.reflect.Field field = BillService.class.getDeclaredField("itemFacade");
        field.setAccessible(true);
        field.set(billService, itemFacade);

        java.lang.reflect.Field fieldDept = BillService.class.getDeclaredField("departmentFacade");
        fieldDept.setAccessible(true);
        fieldDept.set(billService, new DepartmentFacade() {
            @Override
            public Department find(Object id) {
                return new Department();
            }
        });

        java.lang.reflect.Field fieldInst = BillService.class.getDeclaredField("institutionFacade");
        fieldInst.setAccessible(true);
        fieldInst.set(billService, new InstitutionFacade() {
            @Override
            public Institution find(Object id) {
                return new Institution();
            }
        });

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("billTypeAtomic", "PHARMACY_GRN");
        jsonObject.addProperty("paymentMethod", "Cash");

        JsonArray billItemsArray = new JsonArray();
        for (int i = 0; i < 100; i++) {
            JsonObject itemMap = new JsonObject();
            itemMap.addProperty("item_id", i + 1L);
            itemMap.addProperty("batchNo", "B" + i);
            itemMap.addProperty("receivedQty", 10.0);
            itemMap.addProperty("receivedFreeQty", 1.0);
            itemMap.addProperty("purchasePrice", 5.0);
            itemMap.addProperty("salePrice", 10.0);
            billItemsArray.add(itemMap);
        }
        jsonObject.add("billItems", billItemsArray);

        Bill bill = billService.importPharmacyGrnBillFromJson(jsonObject);

        assertNotNull(bill);
        assertEquals(100, bill.getBillItems().size());

        System.out.println("find() calls: " + itemFacade.findCalls);
        System.out.println("findByJpql() calls: " + itemFacade.findByJpqlCalls);
    }
}
