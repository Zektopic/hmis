package com.divudi.service;

import com.divudi.core.entity.AgentHistory;
import com.divudi.core.entity.WebUser;
import com.divudi.core.facade.AgentHistoryFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentHistoryServiceTest {

    private AgentHistoryService service;
    private MockAgentHistoryFacade mockFacade;

    @BeforeEach
    public void setUp() {
        service = new AgentHistoryService();
        mockFacade = new MockAgentHistoryFacade();
        service.agentHistoryFacade = mockFacade;
    }

    @Test
    public void testSaveNullHistory() {
        service.save(null, new WebUser());
        assertNull(mockFacade.created);
        assertNull(mockFacade.edited);
    }

    @Test
    public void testSaveNewHistoryWithUser() {
        AgentHistory ahx = new AgentHistory();
        WebUser user = new WebUser();

        service.save(ahx, user);

        assertNotNull(mockFacade.created);
        assertNull(mockFacade.edited);
        assertEquals(ahx, mockFacade.created);
        assertEquals(user, ahx.getCreater());
        assertNotNull(ahx.getCreatedAt());
    }

    @Test
    public void testSaveNewHistoryWithoutUser() {
        AgentHistory ahx = new AgentHistory();

        service.save(ahx);

        assertNotNull(mockFacade.created);
        assertNull(mockFacade.edited);
        assertEquals(ahx, mockFacade.created);
        assertNull(ahx.getCreater());
        assertNotNull(ahx.getCreatedAt());
    }

    @Test
    public void testSaveNewHistoryWithPreexistingCreaterAndCreatedAt() {
        AgentHistory ahx = new AgentHistory();
        WebUser initialUser = new WebUser();
        Date initialDate = new Date(1000000000L);
        ahx.setCreater(initialUser);
        ahx.setCreatedAt(initialDate);

        WebUser newUser = new WebUser();

        service.save(ahx, newUser);

        assertNotNull(mockFacade.created);
        assertNull(mockFacade.edited);
        assertEquals(ahx, mockFacade.created);
        assertEquals(initialUser, ahx.getCreater());
        assertEquals(initialDate, ahx.getCreatedAt());
    }

    @Test
    public void testSaveExistingHistory() {
        AgentHistory ahx = new AgentHistory();
        ahx.setId(1L);
        WebUser user = new WebUser();

        service.save(ahx, user);

        assertNull(mockFacade.created);
        assertNotNull(mockFacade.edited);
        assertEquals(ahx, mockFacade.edited);
        assertNull(ahx.getCreater()); // user shouldn't be set because id is not null
        assertNull(ahx.getCreatedAt()); // createdAt shouldn't be set because id is not null
    }

    private static class MockAgentHistoryFacade extends AgentHistoryFacade {
        AgentHistory created;
        AgentHistory edited;

        @Override
        public void create(AgentHistory entity) {
            this.created = entity;
        }

        @Override
        public void edit(AgentHistory entity) {
            this.edited = entity;
        }
    }
}
