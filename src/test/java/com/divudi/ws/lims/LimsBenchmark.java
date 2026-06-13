package com.divudi.ws.lims;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.divudi.core.entity.Patient;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.lab.InvestigationItem;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.PatientReportItemValue;
import com.divudi.core.entity.lab.PatientSample;
import com.divudi.core.entity.lab.PatientSampleComponant;
import com.divudi.core.data.InvestigationItemType;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.data.lab.PatientInvestigationStatus;

import com.divudi.core.facade.PatientReportItemValueFacade;
import com.divudi.core.facade.PatientReportFacade;
import com.divudi.core.facade.PatientInvestigationFacade;
import com.divudi.core.facade.BillFacade;

public class LimsBenchmark {

    @Test
    public void testAddResultToReportPerformance() {
        LimsMiddlewareController controller = new LimsMiddlewareController() {

            public PatientSample patientSampleFromId(Long id) {
                PatientSample ps = new PatientSample();
                ps.setId(id);
                return ps;
            }


            public List<PatientSampleComponant> getPatientSampleComponents(PatientSample ps) {
                List<PatientSampleComponant> pscs = new ArrayList<>();
                PatientSampleComponant psc = new PatientSampleComponant();

                Item ic = new Item() {

                    public boolean equals(Object o) { return true; }
                };
                psc.setInvestigationComponant(ic);
                pscs.add(psc);
                return pscs;
            }


            public List<PatientInvestigation> getPatientInvestigations(List<PatientSampleComponant> pscs) {
                List<PatientInvestigation> ptixs = new ArrayList<>();
                PatientInvestigation pi = new PatientInvestigation();
                Investigation ix = new Investigation();

                List<InvestigationItem> items = new ArrayList<>();
                for (int i = 0; i < 1000; i++) {
                    InvestigationItem ii = new InvestigationItem();
                    ii.setIxItemType(InvestigationItemType.Value);
                    Item ti = new Item();
                    ti.setCode("TEST_CODE");
                    ii.setTest(ti);
                    ii.setResultCode("TEST_CODE");

                    Item sampleComp = new Item() {

                        public boolean equals(Object o) { return true; }
                    };
                    ii.setSampleComponent(sampleComp);

                    items.add(ii);
                }
                ix.getReportItems().addAll(items);
                pi.setInvestigation(ix);

                BillItem bi = new BillItem();
                Bill b = new Bill();
                bi.setBill(b);
                pi.setBillItem(bi);

                ptixs.add(pi);
                return ptixs;
            }


            public PatientReport getUnapprovedPatientReport(PatientInvestigation pi) {
                PatientReport pr = new PatientReport();
                List<PatientReportItemValue> privs = new ArrayList<>();

                for (int i = 0; i < 1000; i++) {
                    PatientReportItemValue priv = new PatientReportItemValue();
                    priv.setId((long) i);
                    InvestigationItem ii = new InvestigationItem();
                    ii.setIxItemType(InvestigationItemType.Value);
                    Item ti = new Item();
                    ti.setCode("TEST_CODE");
                    ii.setTest(ti);
                    ii.setResultCode("TEST_CODE");

                    Item sampleComp = new Item() {

                        public boolean equals(Object o) { return true; }
                    };
                    ii.setSampleComponent(sampleComp);

                    priv.setInvestigationItem(ii);
                    privs.add(priv);
                }

                pr.setPatientReportItemValues(privs);
                return pr;
            }
        };

        controller.patientReportItemValueFacade = new PatientReportItemValueFacade() {

            public void create(PatientReportItemValue priv) { }

            public void edit(PatientReportItemValue priv) { }

            public void batchCreate(List<PatientReportItemValue> privs) { }

            public void batchEdit(List<PatientReportItemValue> privs) { }
        };

        controller.prFacade = new PatientReportFacade() {

            public void edit(PatientReport pr) { }
        };

        controller.patientInvestigationFacade = new PatientInvestigationFacade() {

            public void edit(PatientInvestigation pi) { }
        };

        controller.billFacade = new BillFacade() {

            public void edit(Bill b) { }
        };


        long startTime = System.currentTimeMillis();

        // Run test iterations
        for (int i = 0; i < 100; i++) {
            controller.addResultToReport("123", "TEST_CODE", "5.5", "unit", "error");
        }

        long endTime = System.currentTimeMillis();

        System.out.println("=================================================");
        System.out.println("BASELINE TIME: " + (endTime - startTime) + " ms");
        System.out.println("=================================================");
    }
}