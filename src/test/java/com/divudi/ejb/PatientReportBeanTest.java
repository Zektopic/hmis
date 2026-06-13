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
            return null;
        }

        @Override
        public PatientReportItemValue findFirstByJpql(String jpql) {
            return null;
        }
    }

    private static class MockAntibioticFacade extends AntibioticFacade {
        @Override
        public List findByJpql(String jpql) {
            return new ArrayList();
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

        assertEquals(0, facade.createCount);
        assertEquals(1, facade.batchCreateCount);
        assertEquals(1000, facade.allCreated.size());
    }

    @Test
    public void testPerformance() {
        PatientReportBean bean = new PatientReportBean() {
            @Override
            public PatientReportItemValueFacade getPtRivFacade() {
                return new PatientReportItemValueFacade() {
                    int idCounter = 1;
                    @Override
                    public void create(PatientReportItemValue entity) {
                        try { Thread.sleep(1); } catch (Exception e) {}
                        entity.setId((long) idCounter++);
                    }
                    @Override
                    public void batchCreate(List<PatientReportItemValue> entities) {
                        try { Thread.sleep(1); } catch (Exception e) {}
                        for (PatientReportItemValue entity : entities) {
                            entity.setId((long) idCounter++);
                        }
                    }
                    @Override
                    public PatientReportItemValue findFirstByJpql(String jpql) {
                        return null;
                    }
                };
            }
        };

        PatientReport ptReport = new PatientReport();
        ptReport.setId(0L);

        PatientInvestigation pi = new PatientInvestigation();
        Patient p = new Patient();
        pi.setPatient(p);
        PatientEncounter pe = new PatientEncounter();
        pi.setEncounter(pe);
        ptReport.setPatientInvestigation(pi);

        Investigation inv = new Investigation();
        List<ReportItem> items = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            InvestigationItem item = new InvestigationItem();
            item.setIxItemType(InvestigationItemType.Value);
            item.setIxItemValueType(InvestigationItemValueType.Varchar);
            items.add(item);
        }

        try {
            java.lang.reflect.Field field = com.divudi.core.entity.lab.Investigation.class.getSuperclass().getDeclaredField("reportItems");
            field.setAccessible(true);
            field.set(inv, items);
        } catch (Exception e) {
            System.out.println("Could not use reflection, but tests run.");
        }

        ptReport.setItem(inv);
        ptReport.setPatientReportItemValues(new ArrayList<>());

        long start = System.currentTimeMillis();
        bean.addPatientReportItemValuesForTemplateReport(ptReport);
        long end = System.currentTimeMillis();

        System.out.println("Time taken: " + (end - start) + "ms");
        System.out.println("Compared to simulated linear insert cost of ~1000ms");
        assertEquals(50, ptReport.getPatientReportItemValues().size());
    }
}
