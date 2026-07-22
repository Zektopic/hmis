package com.divudi.ejb;

import com.divudi.core.data.BillType;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.CancelledBill;
import com.divudi.core.entity.RefundBill;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PharmacyCalculationTest {

    private PharmacyCalculation pharmacyCalculation;

    @BeforeEach
    public void setUp() {
        pharmacyCalculation = new PharmacyCalculation();
    }

    private BillItem createBillItem(double qty, double freeQty, double retailRate, double purchaseRate) {
        BillItem item = new BillItem();
        PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
        ph.setQty(qty);
        ph.setFreeQty(freeQty);
        ph.setRetailRate(retailRate);
        ph.setPurchaseRate(purchaseRate);
        item.setPharmaceuticalBillItem(ph);
        return item;
    }

    @Test
    public void testCalculateValues_RegularBill() {
        Bill bill = new BilledBill();
        bill.setBillType(BillType.PharmacySale);

        List<BillItem> items = new ArrayList<>();
        items.add(createBillItem(10.0, 2.0, 15.0, 10.0)); // sale: (10 + 2) * 15 = 180, free: 2 * 10 = 20
        items.add(createBillItem(5.0, 0.0, 20.0, 12.0));  // sale: (5 + 0) * 20 = 100, free: 0 * 12 = 0
        bill.setBillItems(items);

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(bill);

        assertEquals(280.0, bill.getSaleValue(), 0.001);
        assertEquals(20.0, bill.getFreeValue(), 0.001);
    }

    @Test
    public void testCalculateValues_ReturnBillType() {
        Bill bill = new BilledBill();
        bill.setBillType(BillType.PharmacyGrnReturn);

        List<BillItem> items = new ArrayList<>();
        items.add(createBillItem(10.0, 2.0, 15.0, 10.0));
        bill.setBillItems(items);

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(bill);

        assertEquals(-180.0, bill.getSaleValue(), 0.001);
        assertEquals(-20.0, bill.getFreeValue(), 0.001);
    }

    @Test
    public void testCalculateValues_CancelledBillClass() {
        Bill bill = new CancelledBill();
        bill.setBillType(BillType.PharmacySale);

        List<BillItem> items = new ArrayList<>();
        items.add(createBillItem(5.0, 1.0, 10.0, 5.0)); // sale: (5+1)*10=60, free: 1*5=5
        bill.setBillItems(items);

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(bill);

        assertEquals(-60.0, bill.getSaleValue(), 0.001);
        assertEquals(-5.0, bill.getFreeValue(), 0.001);
    }

    @Test
    public void testCalculateValues_RefundBillClass() {
        Bill bill = new RefundBill();
        bill.setBillType(BillType.PharmacySale);

        List<BillItem> items = new ArrayList<>();
        items.add(createBillItem(5.0, 1.0, 10.0, 5.0));
        bill.setBillItems(items);

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(bill);

        assertEquals(-60.0, bill.getSaleValue(), 0.001);
        assertEquals(-5.0, bill.getFreeValue(), 0.001);
    }

    @Test
    public void testCalculateValues_NullPharmaceuticalBillItem() {
        Bill bill = new BilledBill();
        bill.setBillType(BillType.PharmacySale);

        List<BillItem> items = new ArrayList<>();
        items.add(createBillItem(10.0, 2.0, 15.0, 10.0));

        BillItem emptyItem = new BillItem();
        emptyItem.setPharmaceuticalBillItem(null);
        items.add(emptyItem); // This one should be skipped

        bill.setBillItems(items);

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(bill);

        assertEquals(180.0, bill.getSaleValue(), 0.001);
        assertEquals(20.0, bill.getFreeValue(), 0.001);
    }

    @Test
    public void testCalculateValues_EmptyItemList() {
        Bill bill = new BilledBill();
        bill.setBillType(BillType.PharmacySale);
        bill.setBillItems(new ArrayList<>());

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(bill);

        assertEquals(0.0, bill.getSaleValue(), 0.001);
        assertEquals(0.0, bill.getFreeValue(), 0.001);
    }
}
