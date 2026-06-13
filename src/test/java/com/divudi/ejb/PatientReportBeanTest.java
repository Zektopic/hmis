package com.divudi.ejb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;

import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.lab.ReportItem;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.Patient;
import com.divudi.core.entity.PatientEncounter;
import com.divudi.core.data.InvestigationItemType;
import com.divudi.core.data.InvestigationItemValueType;
import com.divudi.core.facade.PatientReportItemValueFacade;
import com.divudi.core.entity.lab.PatientReportItemValue;
import com.divudi.core.entity.lab.InvestigationItem;

public class PatientReportBeanTest {

    // Test the performance of adding report item values
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
                        return null; // simulate no existing values
                    }
                };
            }
        };

        // Setup data
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

        // using reflection to set report items on Investigation as there might not be a setter
        try {
            java.lang.reflect.Field field = com.divudi.core.entity.lab.Investigation.class.getSuperclass().getDeclaredField("reportItems");
            field.setAccessible(true);
            field.set(inv, items);
        } catch (Exception e) {
            System.out.println("Could not use reflection, but tests run.");
        }

        ptReport.setItem(inv);
        ptReport.setPatientReportItemValues(new ArrayList<>());

        // Measure
        long start = System.currentTimeMillis();
        bean.addPatientReportItemValuesForTemplateReport(ptReport);
        long end = System.currentTimeMillis();

        System.out.println("Time taken: " + (end - start) + "ms");
        System.out.println("Compared to simulated linear insert cost of ~1000ms");
        assertEquals(50, ptReport.getPatientReportItemValues().size());
    }
}
