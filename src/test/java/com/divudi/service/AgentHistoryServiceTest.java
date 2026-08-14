package com.divudi.service;

import com.divudi.core.entity.AgentHistory;
import com.divudi.core.entity.WebUser;
import com.divudi.core.facade.AgentHistoryFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AgentHistoryServiceTest {

    private AgentHistoryService agentHistoryService;
    private MockAgentHistoryFacade mockFacade;

    @BeforeEach
    public void setUp() {
        agentHistoryService = new AgentHistoryService();
        mockFacade = new MockAgentHistoryFacade();
        agentHistoryService.agentHistoryFacade = mockFacade;
    }

    @Test
    public void testSave_WithNullAgentHistory() {
        agentHistoryService.save(null);

        assertFalse(mockFacade.createCalled);
        assertFalse(mockFacade.editCalled);
    }

    @Test
    public void testSave_WithNewAgentHistory_Creates() {
        AgentHistory ahx = new AgentHistory();
        WebUser user = new WebUser();

        agentHistoryService.save(ahx, user);

        assertTrue(mockFacade.createCalled);
        assertFalse(mockFacade.editCalled);
        assertEquals(ahx, mockFacade.createdEntity);
        assertEquals(user, ahx.getCreater());
        assertNotNull(ahx.getCreatedAt());
    }

    @Test
    public void testSave_WithExistingAgentHistory_Edits() {
        AgentHistory ahx = new AgentHistory();
        ahx.setId(1L); // Simulating existing entity

        agentHistoryService.save(ahx, null);

        assertFalse(mockFacade.createCalled);
        assertTrue(mockFacade.editCalled);
        assertEquals(ahx, mockFacade.editedEntity);
        assertNull(ahx.getCreater());
        assertNull(ahx.getCreatedAt());
    }

    private static class MockAgentHistoryFacade extends AgentHistoryFacade {
        boolean createCalled = false;
        boolean editCalled = false;
        AgentHistory createdEntity = null;
        AgentHistory editedEntity = null;

        @Override
        public void create(AgentHistory entity) {
            createCalled = true;
            createdEntity = entity;
        }

        @Override
        public void edit(AgentHistory entity) {
            editCalled = true;
            editedEntity = entity;
        }
    }
}
