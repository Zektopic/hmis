package com.divudi.ejb;

import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.PatientReportItemValue;
import com.divudi.core.entity.lab.ReportItem;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.Patient;
import com.divudi.core.entity.PatientEncounter;
import com.divudi.core.entity.lab.InvestigationItem;
import com.divudi.core.data.InvestigationItemType;
import com.divudi.core.data.InvestigationItemValueType;
import com.divudi.core.facade.PatientReportItemValueFacade;
import com.divudi.core.facade.AntibioticFacade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PatientReportBeanTest {

    private static class MockPatientReportItemValueFacade extends PatientReportItemValueFacade {
        private int createCount = 0;
        private int batchCreateCount = 0;
        private List<PatientReportItemValue> allCreated = new ArrayList<>();

        @Override
        public void create(PatientReportItemValue entity) {
            createCount++;
            allCreated.add(entity);
        }

        @Override
        public void batchCreate(List<PatientReportItemValue> entities) {
            batchCreateCount++;
            allCreated.addAll(entities);
        }

        @Override
        public PatientReportItemValue findFirstByJpql(String jpql, Map m) {
            return null; // Return null to simulate no existing values, triggering create
        }

        @Override
        public PatientReportItemValue findFirstByJpql(String jpql) {
            return null;
        }
    }

    private static class MockAntibioticFacade extends AntibioticFacade {
        @Override
        public List findByJpql(String jpql) {
            return new ArrayList(); // no antibiotics
        }
    }

    private static class TestInvestigation extends Investigation {
        private List<InvestigationItem> customReportItems = new ArrayList<>();

        @Override
        public List<InvestigationItem> getReportItems() {
            return customReportItems;
        }

        public void setCustomReportItems(List<InvestigationItem> reportItems) {
            this.customReportItems = reportItems;
        }
    }

    @Test
    public void testAddMicrobiologyReportItemValuesForReport_createsValues() {
        PatientReportBean bean = new PatientReportBean();
        MockPatientReportItemValueFacade facade = new MockPatientReportItemValueFacade();
        MockAntibioticFacade antibioticFacade = new MockAntibioticFacade();
        bean.setPtRivFacade(facade);
        bean.antibioticFacade = antibioticFacade;

        PatientReport report = new PatientReport();
        TestInvestigation inv = new TestInvestigation();
        report.setItem(inv);

        PatientInvestigation pi = new PatientInvestigation();
        pi.setPatient(new Patient());
        pi.setEncounter(new PatientEncounter());
        report.setPatientInvestigation(pi);

        List<InvestigationItem> items = new ArrayList<>();
        // Add 1000 report items
        for (int i=0; i<1000; i++) {
            InvestigationItem item = new InvestigationItem();
            item.setIxItemType(InvestigationItemType.Value);
            item.setIxItemValueType(InvestigationItemValueType.Memo);
            item.setRetired(false);
            items.add(item);
        }
        inv.setCustomReportItems(items);

        long startTime = System.currentTimeMillis();
        bean.addMicrobiologyReportItemValuesForReport(report);
        long endTime = System.currentTimeMillis();

        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("createCount: " + facade.createCount);
        System.out.println("batchCreateCount: " + facade.batchCreateCount);

        // Ensure no single inserts were executed and exactly 1000 items were batch-created
        assertEquals(0, facade.createCount);
        assertEquals(1, facade.batchCreateCount);
        assertEquals(1000, facade.allCreated.size());
    }
}
