package com.divudi.ejb;

import com.divudi.core.entity.Item;
import com.divudi.core.entity.clinical.Prescription;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.entity.pharmacy.Atm;
import com.divudi.core.entity.pharmacy.MeasurementUnit;
import com.divudi.core.entity.pharmacy.Vmp;
import com.divudi.ejb.PrescriptionToItemService.PrescriptionToItemResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrescriptionToItemServiceTest {

    // Test subclass to mock calculateItemAndQuantity since Mockito is not used
    private static class TestablePrescriptionToItemService extends PrescriptionToItemService {
        private PrescriptionToItemResult mockResult;

        public void setMockResult(PrescriptionToItemResult mockResult) {
            this.mockResult = mockResult;
        }

        @Override
        public PrescriptionToItemResult calculateItemAndQuantity(Prescription prescription) {
            return mockResult;
        }
    }

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
    public void getCalculationExplanation_insufficientData_returnsMessage() {
        PrescriptionToItemService service = new PrescriptionToItemService();
        Prescription prescription = new Prescription(); // Missing item, dose, etc.

        String explanation = service.getCalculationExplanation(prescription);

        assertEquals("Insufficient data for calculation", explanation);
    }

    @Test
    public void getCalculationExplanation_successResult_returnsCalculationNote() {
        TestablePrescriptionToItemService service = new TestablePrescriptionToItemService();
        Prescription prescription = new Prescription();
        prescription.setItem(new Item());
        prescription.setDose(500.0);
        prescription.setFrequencyUnit(new MeasurementUnit());
        prescription.setDuration(5.0);
        prescription.setDurationUnit(new MeasurementUnit());

        PrescriptionToItemResult successResult = new PrescriptionToItemResult(new Item(), 10.0, "Successful calculation note");
        service.setMockResult(successResult);

        String explanation = service.getCalculationExplanation(prescription);

        assertEquals("Successful calculation note", explanation);
    }

    @Test
    public void getCalculationExplanation_failureResult_returnsErrorMessage() {
        TestablePrescriptionToItemService service = new TestablePrescriptionToItemService();
        Prescription prescription = new Prescription();
        prescription.setItem(new Item());
        prescription.setDose(500.0);
        prescription.setFrequencyUnit(new MeasurementUnit());
        prescription.setDuration(5.0);
        prescription.setDurationUnit(new MeasurementUnit());

        PrescriptionToItemResult failureResult = new PrescriptionToItemResult("Error calculating quantity");
        service.setMockResult(failureResult);

        String explanation = service.getCalculationExplanation(prescription);

        assertEquals("Error calculating quantity", explanation);
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

    private Double invokeCalculateTotalQuantity(Double dose, MeasurementUnit doseUnit,
                                        MeasurementUnit frequencyUnit, Double duration,
                                        MeasurementUnit durationUnit, MeasurementUnit issueUnit,
                                        Amp amp) throws Exception {
        PrescriptionToItemService service = new PrescriptionToItemService();

        // Inject PrescriptionService using reflection
        Field psField = PrescriptionToItemService.class.getDeclaredField("prescriptionService");
        psField.setAccessible(true);
        psField.set(service, new PrescriptionService());

        Method method = PrescriptionToItemService.class.getDeclaredMethod("calculateTotalQuantity",
                Double.class, MeasurementUnit.class, MeasurementUnit.class, Double.class,
                MeasurementUnit.class, MeasurementUnit.class, Amp.class);
        method.setAccessible(true);
        return (Double) method.invoke(service, dose, doseUnit, frequencyUnit, duration, durationUnit, issueUnit, amp);
    }

    @Test
    public void calculateTotalQuantity_durationNull_returnsNull() throws Exception {
        Double dose = 500.0;
        MeasurementUnit doseUnit = new MeasurementUnit();
        doseUnit.setName("mg");
        MeasurementUnit freqUnit = new MeasurementUnit();
        freqUnit.setName("od");
        Double duration = null;
        MeasurementUnit durUnit = new MeasurementUnit();
        durUnit.setName("days");
        Amp amp = new Amp();

        Double result = invokeCalculateTotalQuantity(dose, doseUnit, freqUnit, duration, durUnit, null, amp);
        assertNull(result);
    }

    @Test
    public void calculateTotalQuantity_durationZero_returnsNull() throws Exception {
        Double dose = 500.0;
        MeasurementUnit doseUnit = new MeasurementUnit();
        doseUnit.setName("mg");
        MeasurementUnit freqUnit = new MeasurementUnit();
        freqUnit.setName("od");
        Double duration = 0.0;
        MeasurementUnit durUnit = new MeasurementUnit();
        durUnit.setName("days");
        Amp amp = new Amp();

        Double result = invokeCalculateTotalQuantity(dose, doseUnit, freqUnit, duration, durUnit, null, amp);
        assertNull(result);
    }

    @Test
    public void calculateTotalQuantity_sameUnits_calculatesWithStrength() throws Exception {
        Double dose = 1000.0;
        MeasurementUnit doseUnit = new MeasurementUnit();
        doseUnit.setName("mg");

        MeasurementUnit freqUnit = new MeasurementUnit();
        freqUnit.setName("bd"); // 2 times a day

        Double duration = 5.0;
        MeasurementUnit durUnit = new MeasurementUnit();
        durUnit.setName("days");

        Amp amp = new Amp();
        amp.setStrengthOfAnIssueUnit(500.0);
        MeasurementUnit strengthUnit = new MeasurementUnit();
        strengthUnit.setName("mg");
        amp.setStrengthUnit(strengthUnit);

        Double result = invokeCalculateTotalQuantity(dose, doseUnit, freqUnit, duration, durUnit, null, amp);

        // (1000 / 500) * (2 * 5) = 2 * 10 = 20.0
        assertEquals(20.0, result);
    }

    @Test
    public void calculateTotalQuantity_differentUnits_returnsAdministrations() throws Exception {
        Double dose = 1.0;
        MeasurementUnit doseUnit = new MeasurementUnit();
        doseUnit.setName("tablet");

        MeasurementUnit freqUnit = new MeasurementUnit();
        freqUnit.setName("tds"); // 3 times a day

        Double duration = 7.0;
        MeasurementUnit durUnit = new MeasurementUnit();
        durUnit.setName("days");

        Amp amp = new Amp();
        amp.setStrengthOfAnIssueUnit(500.0);
        MeasurementUnit strengthUnit = new MeasurementUnit();
        strengthUnit.setName("mg");
        amp.setStrengthUnit(strengthUnit);

        Double result = invokeCalculateTotalQuantity(dose, doseUnit, freqUnit, duration, durUnit, null, amp);

        // doseUnit != strengthUnit ("tablet" vs "mg"), so it returns administrations
        // 3 * 7 = 21.0
        assertEquals(21.0, result);
    }

    @Test
    public void calculateTotalQuantity_missingStrength_returnsAdministrations() throws Exception {
        Double dose = 2.0;
        MeasurementUnit doseUnit = new MeasurementUnit();
        doseUnit.setName("ml");

        MeasurementUnit freqUnit = new MeasurementUnit();
        freqUnit.setName("od"); // 1 time a day

        Double duration = 14.0;
        MeasurementUnit durUnit = new MeasurementUnit();
        durUnit.setName("days");

        Amp amp = new Amp();
        // missing strength

        Double result = invokeCalculateTotalQuantity(dose, doseUnit, freqUnit, duration, durUnit, null, amp);

        // No strength, so returns administrations
        // 1 * 14 = 14.0
        assertEquals(14.0, result);
    }
}
