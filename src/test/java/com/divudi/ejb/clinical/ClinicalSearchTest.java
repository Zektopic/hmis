package com.divudi.ejb.clinical;

import com.divudi.core.entity.Patient;
import com.divudi.core.entity.PatientEncounter;
import com.divudi.core.facade.PatientEncounterFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClinicalSearchTest {

    private ClinicalSearch clinicalSearch;
    private MockPatientEncounterFacade mockFacade;
    private List<PatientEncounter> returnedEncounters;

    @BeforeEach
    public void setUp() {
        clinicalSearch = new ClinicalSearch();
        mockFacade = new MockPatientEncounterFacade();
        clinicalSearch.setPeFacade(mockFacade);

        returnedEncounters = new ArrayList<>();
        PatientEncounter pe1 = new PatientEncounter();
        pe1.setId(10L);
        PatientEncounter pe2 = new PatientEncounter();
        pe2.setId(20L);

        returnedEncounters.add(pe1);
        returnedEncounters.add(pe2);

        mockFacade.setReturnList(returnedEncounters);
    }

    @Test
    public void testListPatientEncounters() {
        Patient pt = new Patient();
        pt.setId(1L);

        List<PatientEncounter> result = clinicalSearch.listPatientEncounters(pt);

        assertEquals(returnedEncounters, result, "Should return the expected list of encounters");

        // Assert the executed states on the mock
        assertEquals("select pe from PatientEncounter pe where pe.retired=false and pe.patient=:pt order by pe.id desc",
            mockFacade.getCapturedJpql(), "SQL should match expected");

        Map<String, Object> m = mockFacade.getCapturedParameters();
        assertNotNull(m, "Parameter map should not be null");
        assertEquals(1, m.size(), "Parameter map should have 1 item");
        assertEquals(pt, m.get("pt"), "pt parameter should be our patient");

        // As a bonus check that it actually sets its own fields too (though this is unusual design it does do it)
        assertEquals(clinicalSearch.getSql(), mockFacade.getCapturedJpql());
        assertEquals(clinicalSearch.getM(), mockFacade.getCapturedParameters());
    }

    // Manual Mock class for facade
    private static class MockPatientEncounterFacade extends PatientEncounterFacade {
        private List<PatientEncounter> returnList;
        private String capturedJpql;
        private Map<String, Object> capturedParameters;

        public void setReturnList(List<PatientEncounter> returnList) {
            this.returnList = returnList;
        }

        public String getCapturedJpql() {
            return capturedJpql;
        }

        public Map<String, Object> getCapturedParameters() {
            return capturedParameters;
        }

        @Override
        public List<PatientEncounter> findByJpql(String jpql, Map<String, Object> parameters) {
            this.capturedJpql = jpql;
            this.capturedParameters = parameters;
            return returnList;
        }
    }
}
