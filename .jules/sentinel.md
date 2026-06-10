## 2026-06-09 - SQL Injection via String Concatenation in JPQL
**Vulnerability:** Widespread use of string concatenation to build JPQL/SQL queries (e.g., `like '%" + value + "%'`), leading to SQL injection. Found extensively throughout the codebase for search functionality.
**Learning:** The codebase heavily relies on string building for search queries instead of using the parameterized `findByJpql(String jpql, Map parameters)` method available in the Facades. This makes search forms highly vulnerable to injection if values are unvalidated.
**Prevention:** Always use parameterized queries (`:paramName`) and pass parameters via a `Map` when using `findByJpql` to prevent injection and improve query caching.
## 2026-06-10 - SQL Injection via String Concatenation in JPQL
**Vulnerability:** Widespread use of string concatenation to build JPQL/SQL queries (e.g., `like '%" + value + "%'`), leading to SQL injection. Found extensively throughout the codebase for search functionality.
**Learning:** The codebase heavily relies on string building for search queries instead of using the parameterized `findByJpql(String jpql, Map parameters)` method available in the Facades. This makes search forms highly vulnerable to injection if values are unvalidated. I fixed two vulnerable spots in `StockController.java`.
**Prevention:** Always use parameterized queries (`:paramName`) and pass parameters via a `Map` when using `findByJpql` to prevent injection and improve query caching.
