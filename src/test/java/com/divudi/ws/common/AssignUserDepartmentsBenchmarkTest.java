package com.divudi.ws.common;

import com.divudi.bean.common.ApiKeyController;
import com.divudi.core.entity.ApiKey;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.WebUser;
import com.divudi.core.entity.WebUserDepartment;
import com.divudi.core.facade.DepartmentFacade;
import com.divudi.core.facade.WebUserDepartmentFacade;
import com.divudi.core.facade.WebUserFacade;
import com.divudi.core.facade.WebUserPrivilegeFacade;
import org.junit.jupiter.api.Test;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssignUserDepartmentsBenchmarkTest {

    static class MockHttpServletRequest implements HttpServletRequest {
        public String getAuthType() { return null; }
        public javax.servlet.http.Cookie[] getCookies() { return null; }
        public long getDateHeader(String name) { return 0; }
        public String getHeader(String name) { return "valid-key"; }
        public Enumeration<String> getHeaders(String name) { return null; }
        public Enumeration<String> getHeaderNames() { return null; }
        public int getIntHeader(String name) { return 0; }
        public String getMethod() { return null; }
        public String getPathInfo() { return null; }
        public String getPathTranslated() { return null; }
        public String getContextPath() { return null; }
        public String getQueryString() { return null; }
        public String getRemoteUser() { return null; }
        public boolean isUserInRole(String role) { return false; }
        public java.security.Principal getUserPrincipal() { return null; }
        public String getRequestedSessionId() { return null; }
        public String getRequestURI() { return null; }
        public StringBuffer getRequestURL() { return null; }
        public String getServletPath() { return null; }
        public javax.servlet.http.HttpSession getSession(boolean create) { return null; }
        public javax.servlet.http.HttpSession getSession() { return null; }
        public String changeSessionId() { return null; }
        public boolean isRequestedSessionIdValid() { return false; }
        public boolean isRequestedSessionIdFromCookie() { return false; }
        public boolean isRequestedSessionIdFromURL() { return false; }
        public boolean isRequestedSessionIdFromUrl() { return false; }
        public boolean authenticate(javax.servlet.http.HttpServletResponse response) { return false; }
        public void login(String username, String password) {}
        public void logout() {}
        public java.util.Collection<javax.servlet.http.Part> getParts() { return null; }
        public javax.servlet.http.Part getPart(String name) { return null; }
        public <T extends javax.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
        public Object getAttribute(String name) { return null; }
        public Enumeration<String> getAttributeNames() { return null; }
        public String getCharacterEncoding() { return null; }
        public void setCharacterEncoding(String env) {}
        public int getContentLength() { return 0; }
        public long getContentLengthLong() { return 0; }
        public String getContentType() { return null; }
        public javax.servlet.ServletInputStream getInputStream() { return null; }
        public String getParameter(String name) { return null; }
        public Enumeration<String> getParameterNames() { return null; }
        public String[] getParameterValues(String name) { return null; }
        public java.util.Map<String, String[]> getParameterMap() { return null; }
        public String getProtocol() { return null; }
        public String getScheme() { return null; }
        public String getServerName() { return null; }
        public int getServerPort() { return 0; }
        public java.io.BufferedReader getReader() { return null; }
        public String getRemoteAddr() { return null; }
        public String getRemoteHost() { return null; }
        public void setAttribute(String name, Object o) {}
        public void removeAttribute(String name) {}
        public Locale getLocale() { return null; }
        public Enumeration<Locale> getLocales() { return null; }
        public boolean isSecure() { return false; }
        public javax.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
        public String getRealPath(String path) { return null; }
        public int getRemotePort() { return 0; }
        public String getLocalName() { return null; }
        public String getLocalAddr() { return null; }
        public int getLocalPort() { return 0; }
        public javax.servlet.ServletContext getServletContext() { return null; }
        public javax.servlet.AsyncContext startAsync() { return null; }
        public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) { return null; }
        public boolean isAsyncStarted() { return false; }
        public boolean isAsyncSupported() { return false; }
        public javax.servlet.AsyncContext getAsyncContext() { return null; }
        public javax.servlet.DispatcherType getDispatcherType() { return null; }
    }

    static class MockApiKeyController extends ApiKeyController {
        @Override
        public ApiKey findApiKey(String key) {
            ApiKey apiKey = new ApiKey();
            WebUser user = new WebUser();
            user.setId(1L);
            user.setActivated(true);
            user.setRetired(false);
            apiKey.setWebUser(user);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            apiKey.setDateOfExpiary(cal.getTime());
            return apiKey;
        }
    }

    static class MockWebUserPrivilegeFacade extends WebUserPrivilegeFacade {
        @Override
        public List findByJpql(String jpql, Map parameters) {
            return Collections.singletonList(new Object()); // Return non-empty to pass isAdmin
        }
    }

    static class MockDepartmentFacade extends DepartmentFacade {
        int findCount = 0;
        int findByJpqlCount = 0;

        @Override
        public Department find(Object id) {
            findCount++;
            Department d = new Department();
            d.setId((Long)id);
            return d;
        }

        @Override
        public List<Department> findByJpql(String jpql, Map<String, Object> parameters) {
            findByJpqlCount++;
            List<Department> res = new ArrayList<>();
            Collection<Long> ids = (Collection<Long>)parameters.get("ids");
            if (ids != null) {
                for(Long id : ids) {
                    Department d = new Department();
                    d.setId(id);
                    res.add(d);
                }
            }
            return res;
        }
    }

    static class MockWebUserDepartmentFacade extends WebUserDepartmentFacade {
        int findByJpqlCount = 0;
        int createCount = 0;

        @Override
        public List<WebUserDepartment> findByJpql(String jpql, Map<String, Object> parameters) {
            findByJpqlCount++;
            return new ArrayList<>();
        }

        @Override
        public void create(WebUserDepartment entity) {
            createCount++;
        }
    }

    static class TestUserManagementApi extends UserManagementApi {
        @Override
        public Response listUserDepartments(Long id) {
            return null; // By-pass ResponseBuilder classloader issue in test
        }
    }

    @Test
    public void testPerformance() throws Exception {
        TestUserManagementApi api = new TestUserManagementApi();

        MockDepartmentFacade deptFacade = new MockDepartmentFacade();
        MockWebUserDepartmentFacade webUserDeptFacade = new MockWebUserDepartmentFacade();

        Field dfField = UserManagementApi.class.getDeclaredField("departmentFacade");
        dfField.setAccessible(true);
        dfField.set(api, deptFacade);

        Field wudfField = UserManagementApi.class.getDeclaredField("webUserDepartmentFacade");
        wudfField.setAccessible(true);
        wudfField.set(api, webUserDeptFacade);

        Field wufField = UserManagementApi.class.getDeclaredField("webUserFacade");
        wufField.setAccessible(true);
        wufField.set(api, new WebUserFacade() {
            @Override
            public WebUser find(Object id) {
                WebUser u = new WebUser();
                u.setId((Long)id);
                return u;
            }
        });

        Field wupfField = UserManagementApi.class.getDeclaredField("webUserPrivilegeFacade");
        wupfField.setAccessible(true);
        wupfField.set(api, new MockWebUserPrivilegeFacade());

        Field akcField = UserManagementApi.class.getDeclaredField("apiKeyController");
        akcField.setAccessible(true);
        akcField.set(api, new MockApiKeyController());

        Field reqField = UserManagementApi.class.getDeclaredField("requestContext");
        reqField.setAccessible(true);
        reqField.set(api, new MockHttpServletRequest());

        List<Long> dids = new ArrayList<>();
        for(int i=0; i<100; i++) dids.add((long)i);

        String body = "{\"departmentIds\": " + dids.toString() + "}";

        api.assignUserDepartments(100L, body);

        System.out.println("DepartmentFacade.find() count: " + deptFacade.findCount);
        System.out.println("WebUserDepartmentFacade.findByJpql() count: " + webUserDeptFacade.findByJpqlCount);

        assertTrue(deptFacade.findCount > 0 || webUserDeptFacade.findByJpqlCount > 0);
    }
}
