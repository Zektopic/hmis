## 2026-06-09 - SQL Injection via String Concatenation in JPQL
**Vulnerability:** Widespread use of string concatenation to build JPQL/SQL queries (e.g., `like '%" + value + "%'`), leading to SQL injection. Found extensively throughout the codebase for search functionality.
**Learning:** The codebase heavily relies on string building for search queries instead of using the parameterized `findByJpql(String jpql, Map parameters)` method available in the Facades. This makes search forms highly vulnerable to injection if values are unvalidated.
**Prevention:** Always use parameterized queries (`:paramName`) and pass parameters via a `Map` when using `findByJpql` to prevent injection and improve query caching.

## 2026-06-11 - JPQL Injection via String Concatenation in Controllers
**Vulnerability:** Found JPQL injection in `ServiceCategoryController.java` where `getSelectText()` was directly concatenated into the query string for `findByJpql`.
**Learning:** Controller classes in `com.divudi.bean.*` often use string building for `getSelectedItems()` when searching by name, exposing the system to injection vulnerabilities.
**Prevention:** Use parameterized queries (`like :q`) and pass a `Map<String, Object>` to `getFacade().findByJpql(sql, params)` instead of string concatenation.
