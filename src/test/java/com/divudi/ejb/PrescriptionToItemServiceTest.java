package com.divudi.ejb;

import com.divudi.core.entity.clinical.Prescription;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.entity.pharmacy.Atm;
import com.divudi.core.entity.pharmacy.MeasurementUnit;
import com.divudi.core.entity.pharmacy.Vmp;
import com.divudi.ejb.PrescriptionToItemService.PrescriptionToItemResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrescriptionToItemServiceTest {

    private Double invokeCalculateDosesPerDay(MeasurementUnit unit) throws Exception {
        PrescriptionToItemService service = new PrescriptionToItemService();
        Method method = PrescriptionToItemService.class.getDeclaredMethod("calculateDosesPerDay", MeasurementUnit.class);
        method.setAccessible(true);
        return (Double) method.invoke(service, unit);
    }

    @Test
    public void calculateDosesPerDay_nullName_returnsDefault() throws Exception {
        MeasurementUnit unit = new MeasurementUnit();
        unit.setName(null);
        assertEquals(1.0, invokeCalculateDosesPerDay(unit));
    }

    @Test
    public void calculateDosesPerDay_trimsAndLowercases() throws Exception {
        MeasurementUnit unit = new MeasurementUnit();
        unit.setName("  Twice daily  ");
        assertEquals(2.0, invokeCalculateDosesPerDay(unit));
    }

    @Test
    public void calculateDosesPerDay_normalizesInternalSpaces() throws Exception {
        MeasurementUnit unit = new MeasurementUnit();
        unit.setName("once    daily");
        assertEquals(1.0, invokeCalculateDosesPerDay(unit));
    }

    @Test
    public void calculateItemAndQuantity_nullPrescription_returnsError() {
        PrescriptionToItemService service = new PrescriptionToItemService();
        PrescriptionToItemResult result = service.calculateItemAndQuantity(null);
        assertFalse(result.isSuccess());
        assertEquals("Prescription cannot be null", result.getErrorMessage());
    }

    @Test
    public void calculateItemAndQuantity_nullItem_returnsError() {
        PrescriptionToItemService service = new PrescriptionToItemService();
        Prescription prescription = new Prescription();
        prescription.setItem(null);

        PrescriptionToItemResult result = service.calculateItemAndQuantity(prescription);
        assertFalse(result.isSuccess());
        assertEquals("No medicine selected in prescription", result.getErrorMessage());
    }

    @Test
    public void calculateItemAndQuantity_ampItem_delegatesToAmpCalculation() {
        PrescriptionToItemService service = new PrescriptionToItemService();
        Prescription prescription = new Prescription();
        prescription.setItem(new Amp());

        PrescriptionToItemResult result = service.calculateItemAndQuantity(prescription);
        assertFalse(result.isSuccess());
        assertEquals("Incomplete prescription: dose, frequency, duration and duration unit are required", result.getErrorMessage());
    }

    @Test
    public void calculateItemAndQuantity_vmpItem_delegatesToVmpCalculation() {
        PrescriptionToItemService service = new PrescriptionToItemService();
        Prescription prescription = new Prescription();
        prescription.setItem(new Vmp());

        PrescriptionToItemResult result = service.calculateItemAndQuantity(prescription);
        // findAmpsForVmp will catch the NullPointerException from itemFacade and return empty list.
        // Then calculateFromVmp will return a success result with "VMP calculation (no specific AMP found)"
        assertTrue(result.isSuccess());
        assertEquals("VMP calculation (no specific AMP found)", result.getCalculationNote());
        assertEquals(30.0, result.getQuantity());
    }

    @Test
    public void calculateItemAndQuantity_genericItem_delegatesToGenericCalculation() {
        PrescriptionToItemService service = new PrescriptionToItemService();
        Prescription prescription = new Prescription();
        Atm atm = new Atm();
        atm.setName("Generic Item");
        prescription.setItem(atm);

        PrescriptionToItemResult result = service.calculateItemAndQuantity(prescription);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().startsWith("Error finding suitable item:") ||
                   result.getErrorMessage().startsWith("No suitable specific items found for "));
    }
}
