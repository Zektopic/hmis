package com.divudi.performance;

import com.divudi.core.data.InvestigationItemType;
import com.divudi.core.entity.lab.InvestigationItem;
import com.divudi.core.entity.lab.PatientReportItemValue;
import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.PatientSample;
import com.divudi.core.entity.lab.PatientSampleComponant;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.Bill;
import com.divudi.core.facade.PatientReportItemValueFacade;
import com.divudi.core.facade.PatientReportFacade;
import com.divudi.core.facade.PatientInvestigationFacade;
import com.divudi.core.facade.BillFacade;
import com.divudi.ws.lims.LimsMiddlewareController;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

public class LimsMiddlewareControllerPerformanceTest {

    @Test
    @DisplayName("Performance test for addResultToReport")
    public void testAddResultToReportPerformance() throws Exception {
        LimsMiddlewareController controller = new LimsMiddlewareController() {
            @Override
            public PatientSample patientSampleFromId(Long id) {
                return new PatientSample();
            }
            @Override
            public List<PatientSampleComponant> getPatientSampleComponents(PatientSample ps) {
                List<PatientSampleComponant> list = new ArrayList<>();
                PatientSampleComponant psc = new PatientSampleComponant();

                Item comp = new Item();
                psc.setInvestigationComponant(comp);

                PatientInvestigation pi = new PatientInvestigation();
                Investigation ix = new Investigation();
                InvestigationItem ri = new InvestigationItem();
                ri.setIxItemType(InvestigationItemType.Value);
                ri.setResultCode("MATCH_CODE");
                Item t = new Item();
                t.setCode("MATCH_CODE");
                ri.setTest(t);
                ix.getReportItems().add(ri);
                pi.setInvestigation(ix);

                BillItem bi = new BillItem();
                Bill b = new Bill();
                bi.setBill(b);
                pi.setBillItem(bi);
                psc.setPatientInvestigation(pi);

                list.add(psc);
                return list;
            }
            @Override
            public PatientReport getUnapprovedPatientReport(PatientInvestigation pi) {
                PatientReport pr = new PatientReport();
                List<PatientReportItemValue> values = new ArrayList<>();

                Item comp = new Item(); // Component to match
                // We should make the component match one of the items.
                // We'll set the component of the PSC to this same component!
                // Wait, in my getPatientSampleComponents, I created `new Item()`.
                // It won't equal `new Item()` here unless they have same id. Let's set id.

                for (int i = 0; i < 5000; i++) {
                    PatientReportItemValue priv = new PatientReportItemValue();
                    InvestigationItem ii = new InvestigationItem();
                    ii.setIxItemType(InvestigationItemType.Value);
                    Item t = new Item();
                    t.setCode("TEST_CODE_" + i);
                    ii.setTest(t);

                    if (i % 2 == 0) {
                        ii.setResultCode("MATCH_CODE");
                    } else {
                        ii.setResultCode("TEST_CODE_" + i);
                    }

                    // Make them match by setting same ID
                    Item comp2 = new Item();
                    comp2.setId(1L);
                    ii.setSampleComponent(comp2);

                    priv.setInvestigationItem(ii);

                    // alternate new vs existing
                    if (i % 3 == 0) {
                        priv.setId((long) i);
                    }

                    values.add(priv);
                }
                pr.setPatientReportItemValues(values);
                return pr;
            }
        };

        // Inject dummy facades
        DummyPatientReportItemValueFacade facade = new DummyPatientReportItemValueFacade();
        setField(controller, "patientReportItemValueFacade", facade);
        setField(controller, "prFacade", new DummyPatientReportFacade());
        setField(controller, "patientInvestigationFacade", new DummyPatientInvestigationFacade());
        setField(controller, "billFacade", new DummyBillFacade());

        // We need to make sure getPatientSampleComponents returns an item with ID=1L
        // So let's override it again properly
        controller = new LimsMiddlewareController() {
            @Override
            public PatientSample patientSampleFromId(Long id) {
                return new PatientSample();
            }
            @Override
            public List<PatientSampleComponant> getPatientSampleComponents(PatientSample ps) {
                List<PatientSampleComponant> list = new ArrayList<>();
                PatientSampleComponant psc = new PatientSampleComponant();

                Item comp = new Item();
                comp.setId(1L); // Match!
                psc.setInvestigationComponant(comp);

                PatientInvestigation pi = new PatientInvestigation();
                Investigation ix = new Investigation();
                InvestigationItem ri = new InvestigationItem();
                ri.setIxItemType(InvestigationItemType.Value);
                ri.setResultCode("MATCH_CODE");
                Item t = new Item();
                t.setCode("MATCH_CODE");
                ri.setTest(t);
                ix.getReportItems().add(ri);
                pi.setInvestigation(ix);

                BillItem bi = new BillItem();
                Bill b = new Bill();
                bi.setBill(b);
                pi.setBillItem(bi);
                psc.setPatientInvestigation(pi);

                list.add(psc);
                return list;
            }
            @Override
            public PatientReport getUnapprovedPatientReport(PatientInvestigation pi) {
                PatientReport pr = new PatientReport();
                List<PatientReportItemValue> values = new ArrayList<>();
                for (int i = 0; i < 5000; i++) {
                    PatientReportItemValue priv = new PatientReportItemValue();
                    InvestigationItem ii = new InvestigationItem();
                    ii.setIxItemType(InvestigationItemType.Value);
                    Item t = new Item();
                    t.setCode("TEST_CODE_" + i);
                    ii.setTest(t);

                    if (i % 2 == 0) {
                        ii.setResultCode("MATCH_CODE");
                    } else {
                        ii.setResultCode("TEST_CODE_" + i);
                    }

                    Item comp2 = new Item();
                    comp2.setId(1L);
                    ii.setSampleComponent(comp2);
                    priv.setInvestigationItem(ii);

                    if (i % 3 == 0) {
                        priv.setId((long) i);
                    }
                    values.add(priv);
                }
                pr.setPatientReportItemValues(values);
                return pr;
            }
        };

        setField(controller, "patientReportItemValueFacade", facade);
        setField(controller, "prFacade", new DummyPatientReportFacade());
        setField(controller, "patientInvestigationFacade", new DummyPatientInvestigationFacade());
        setField(controller, "billFacade", new DummyBillFacade());

        // Warmup
        for (int i = 0; i < 5; i++) {
            controller.addResultToReport("1", "MATCH_CODE", "12.3", "mg", null);
        }

        facade.resetCounts();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            controller.addResultToReport("1", "MATCH_CODE", "12.3", "mg", null);
        }
        long end = System.currentTimeMillis();
        long duration = end - start;

        System.out.println("Execution time: " + duration + "ms");
        System.out.println("Create calls: " + facade.createCalls);
        System.out.println("Edit calls: " + facade.editCalls);
        System.out.println("BatchCreate calls: " + facade.batchCreateCalls);
        System.out.println("BatchEdit calls: " + facade.batchEditCalls);

        assertTrue(duration < 30000, "Performance is too slow");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    class DummyPatientReportItemValueFacade extends PatientReportItemValueFacade {
        int createCalls = 0;
        int editCalls = 0;
        int batchCreateCalls = 0;
        int batchEditCalls = 0;

        public void resetCounts() {
            createCalls = 0;
            editCalls = 0;
            batchCreateCalls = 0;
            batchEditCalls = 0;
        }

        @Override
        public void create(PatientReportItemValue entity) {
            createCalls++;
            spin(1000);
        }
        @Override
        public void edit(PatientReportItemValue entity) {
            editCalls++;
            spin(1000);
        }
        @Override
        public void batchCreate(List<PatientReportItemValue> entities) {
            batchCreateCalls++;
            spin(1000);
        }
        @Override
        public void batchEdit(List<PatientReportItemValue> entities) {
            batchEditCalls++;
            spin(1000);
        }

        private void spin(int iterations) {
            long sum = 0;
            for(int i=0; i<iterations; i++) sum += i;
        }
    }

    class DummyPatientReportFacade extends PatientReportFacade {
        @Override
        public void edit(PatientReport entity) {}
    }
    class DummyPatientInvestigationFacade extends PatientInvestigationFacade {
        @Override
        public void edit(PatientInvestigation entity) {}
    }
    class DummyBillFacade extends BillFacade {
        @Override
        public void edit(com.divudi.core.entity.Bill entity) {}
    }
}
