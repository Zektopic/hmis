package com.divudi.ws.lims;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.PatientReportItemValue;
import com.divudi.core.entity.lab.InvestigationItem;
import com.divudi.core.entity.Item;
import com.divudi.core.data.InvestigationItemType;
import com.divudi.core.entity.lab.PatientSample;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.lab.PatientSampleComponant;
import com.divudi.core.facade.PatientReportItemValueFacade;
import com.divudi.core.facade.PatientReportFacade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LimsMiddlewareControllerPerformanceTest {

    // A dummy subclass to mock dependencies since Mockito is not available.
    class DummyPatientReportItemValueFacade extends PatientReportItemValueFacade {
        int createCalls = 0;
        int editCalls = 0;
        int batchEditCalls = 0;
        int batchCreateCalls = 0;

        @Override
        public void create(PatientReportItemValue entity) {
            createCalls++;
        }

        @Override
        public void edit(PatientReportItemValue entity) {
            editCalls++;
        }

        @Override
        public void batchCreate(List<PatientReportItemValue> entities, int batchSize) {
            batchCreateCalls += entities.size();
        }

        @Override
        public void batchEdit(List<PatientReportItemValue> entities, int batchSize) {
            batchEditCalls += entities.size();
        }

        @Override
        public void batchCreate(List<PatientReportItemValue> entities) {
            batchCreateCalls += entities.size();
        }

        @Override
        public void batchEdit(List<PatientReportItemValue> entities) {
            batchEditCalls += entities.size();
        }
    }

    class DummyPatientReportFacade extends PatientReportFacade {
        int editCalls = 0;

        @Override
        public void edit(PatientReport entity) {
            editCalls++;
        }
    }

    @Test
    public void testPerformanceOfResultAdding() {
        DummyPatientReportItemValueFacade privFacade = new DummyPatientReportItemValueFacade();
        DummyPatientReportFacade prFacade = new DummyPatientReportFacade();

        Item comp = new Item();

        PatientReport testReport = new PatientReport();
        List<PatientReportItemValue> privs = new ArrayList<>();

        int itemCount = 100000;

        for (int i = 0; i < itemCount; i++) {
            PatientReportItemValue priv = new PatientReportItemValue();
            InvestigationItem ii = new InvestigationItem();
            ii.setIxItemType(InvestigationItemType.Value);
            ii.setResultCode("TESTCODE");
            ii.setSampleComponent(comp);
            priv.setInvestigationItem(ii);
            // new items have null ID
            privs.add(priv);
        }
        testReport.setPatientReportItemValues(privs);

        PatientSample ps = new PatientSample();
        ps.setInvestigationComponant(comp);

        long start = System.currentTimeMillis();

        // Simulating the optimized loop inside LimsMiddlewareController.addResultToReportPrevious (around line 1311)
        boolean valueToSave = false;

        List<PatientReportItemValue> createBatch = new ArrayList<>();
        List<PatientReportItemValue> editBatch = new ArrayList<>();

        for (PatientReportItemValue priv : testReport.getPatientReportItemValues()) {

            // To be accurate, let's just run the block of code inside the real thing directly here
            if (ps.getInvestigationComponant() == null || priv.getInvestigationItem().getSampleComponent() == null) {
                // branch 1
            } else if (priv.getInvestigationItem().getSampleComponent().equals(ps.getInvestigationComponant())) {
                String result = "12.3";
                priv.setStrValue(result);

                Double dbl = 0d;
                try {
                    dbl = Double.parseDouble(result);
                } catch (Exception e) {
                }
                priv.setDoubleValue(dbl);
                if (priv.getId() == null) {
                    createBatch.add(priv);
                } else {
                    editBatch.add(priv);
                }
                valueToSave = true;
            }
        }

        if (!createBatch.isEmpty()) {
            privFacade.batchCreate(createBatch);
        }
        if (!editBatch.isEmpty()) {
            privFacade.batchEdit(editBatch);
        }

        if (valueToSave) {
            testReport.setDataEntered(true);
            testReport.setDataEntryAt(new Date());
            testReport.setDataEntryComments("Initial Results were taken from Analyzer through Middleware");
            prFacade.edit(testReport);
        }

        long end = System.currentTimeMillis();

        System.out.println("Optimized Processing took " + (end - start) + " ms");
        System.out.println("Create calls: " + privFacade.createCalls);
        System.out.println("Batch create calls: " + privFacade.batchCreateCalls);
    }
}
