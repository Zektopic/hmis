package com.divudi.ejb;

import com.divudi.core.entity.Item;
import com.divudi.core.entity.clinical.Prescription;
import com.divudi.core.entity.pharmacy.MeasurementUnit;
import com.divudi.ejb.PrescriptionToItemService.PrescriptionToItemResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
