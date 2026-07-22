package com.divudi.bean.hr;

import com.divudi.core.entity.hr.PaysheetComponent;
import com.divudi.core.facade.PaysheetComponentFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaysheetComponentSystemControllerTest {

    private PaysheetComponentSystemController controller;
    private MockPaysheetComponentFacade facade;
    private String capturedJpql;
    private Map<String, Object> capturedParams;

    class MockPaysheetComponentFacade extends PaysheetComponentFacade {
        @Override
        public List<PaysheetComponent> findByJpql(String jpql, Map<String, Object> parameters) {
            capturedJpql = jpql;
            capturedParams = parameters;
            return new ArrayList<>();
        }
    }

    @BeforeEach
    public void setUp() {
        controller = new PaysheetComponentSystemController();
        facade = new MockPaysheetComponentFacade();
        controller.setEjbFacade(facade);
        capturedJpql = null;
        capturedParams = null;
    }

    @Test
    public void testGetSelectedItemsUsesParameterizedQuery() {
        controller.setSelectText("testComponent");
        controller.getSelectedItems();

        assertNotNull(capturedJpql, "Query should not be null");
        assertTrue(capturedJpql.contains("like :q"), "Query should be parameterized to prevent SQL Injection");
        assertNotNull(capturedParams, "Parameters should be passed to the query");
        assertEquals("%TESTCOMPONENT%", capturedParams.get("q"));
    }

    @Test
    public void testCompletePaysheetComponentUsesParameterizedQuery() {
        controller.completePaysheetComponent("testComponent");

        assertNotNull(capturedJpql, "Query should not be null");
        assertTrue(capturedJpql.contains("like :q"), "Query should be parameterized to prevent SQL Injection");
        assertNotNull(capturedParams, "Parameters should be passed to the query");
        assertEquals("%TESTCOMPONENT%", capturedParams.get("q"));
    }
}
