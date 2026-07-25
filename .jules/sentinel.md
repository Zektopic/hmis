## 2026-07-25 - Fix SQL Injection in ServiceSessionController
**Vulnerability:** JPQL Injection in `ServiceSessionController.java` (`getSelectedItems`, `completeServiceSession`, `completeSession`) due to unsafe string concatenation using `like '%" + getSelectText().toUpperCase() + "%'`.
**Learning:** Using string concatenation to build JPQL queries with user-supplied search text (even with `toUpperCase()`) exposes the application to SQL injection vulnerabilities, as input is not sanitized or escaped.
**Prevention:** Always use parameterized queries (e.g., `upper(c.name) like :qry`) and pass parameters using a `Map<String, Object>` in JPQL queries to securely bind inputs and prevent injection.
