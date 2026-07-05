## 2026-06-09 - SQL Injection via String Concatenation in JPQL
**Vulnerability:** Widespread use of string concatenation to build JPQL/SQL queries (e.g., `like '%" + value + "%'`), leading to SQL injection. Found extensively throughout the codebase for search functionality.
**Learning:** The codebase heavily relies on string building for search queries instead of using the parameterized `findByJpql(String jpql, Map parameters)` method available in the Facades. This makes search forms highly vulnerable to injection if values are unvalidated.
**Prevention:** Always use parameterized queries (`:paramName`) and pass parameters via a `Map` when using `findByJpql` to prevent injection and improve query caching.

## 2026-06-10 - SQL Injection via String Concatenation in JPQL
**Vulnerability:** Widespread use of string concatenation to build JPQL/SQL queries (e.g., `like '%" + value + "%'`), leading to SQL injection. Found extensively throughout the codebase for search functionality.
**Learning:** The codebase heavily relies on string building for search queries instead of using the parameterized `findByJpql(String jpql, Map parameters)` method available in the Facades. This makes search forms highly vulnerable to injection if values are unvalidated. I fixed two vulnerable spots in `StockController.java`.
**Prevention:** Always use parameterized queries (`:paramName`) and pass parameters via a `Map` when using `findByJpql` to prevent injection and improve query caching.

## 2026-06-11 - JPQL Injection via String Concatenation in Controllers
**Vulnerability:** Found JPQL injection in `ServiceCategoryController.java` where `getSelectText()` was directly concatenated into the query string for `findByJpql`.
**Learning:** Controller classes in `com.divudi.bean.*` often use string building for `getSelectedItems()` when searching by name, exposing the system to injection vulnerabilities.
**Prevention:** Use parameterized queries (`like :q`) and pass a `Map<String, Object>` to `getFacade().findByJpql(sql, params)` instead of string concatenation.

## 2026-06-14 - JPQL Injection via String Concatenation in Controllers
**Vulnerability:** Found JPQL injection in `RoomController.java` where `getSelectText()` was directly concatenated into the query string for `findByJpql`.
**Learning:** Similar to the previous entry, controller classes (like `RoomController`) often use string building for `getSelectedItems()` when searching by name, exposing the system to injection vulnerabilities.
**Prevention:** Use parameterized queries (`like :q`) and pass a `Map<String, Object>` to `getFacade().findByJpql(sql, params)` instead of string concatenation.
## 2024-06-15 - Hardcoded Payment Gateway Secrets

**Vulnerability:** Hardcoded payment gateway secrets (Merchant ID, API Username, API Password) were used as default values when fetching configuration settings in `PaymentGatewayController.java`.

**Learning:** When using configuration fetching utilities (e.g., `getLongTextValueByKey`), developers might incorrectly pass actual production/test secrets as fallback defaults instead of empty strings, leading to those secrets being permanently embedded in the source code.

**Prevention:** Ensure that fallback values for secrets in configuration retrievals are strictly empty strings (`""`) or safe dummy values, and never actual keys or passwords. Code reviews should explicitly flag any hardcoded strings that look like credentials, especially in configuration loading methods.
## 2026-06-19 - JPQL Injection via String Concatenation in LIKE clauses
**Vulnerability:** Found critical JPQL injection in `AtmController.java` and `ManufacturerController.java` where user input (`query`, `getSelectText()`, `qry`) was directly concatenated into `LIKE` clauses for searches (e.g., `like '%" + query + "%'`).
**Learning:** Controller classes frequently use string building for `LIKE` clauses instead of parameterized queries, especially when providing autocomplete suggestions or filtering tables. This is a severe vulnerability.
**Prevention:** Always use parameterized queries (`like :query`) and pass parameters securely via a `Map<String, Object>` (e.g., `m.put("query", "%" + query + "%")`) when using `findByJpql`.

## 2026-06-20 - JPQL Injection via String Concatenation in ReportFormatController
**Vulnerability:** Found a critical SQL injection point in `ReportFormatController.java` where user input via `getSelectText()` was directly concatenated into a JPQL string within a LIKE clause.
**Learning:** Similar to past findings, the application's search functionality inside JSF controllers frequently builds JPQL dynamically using string concatenation with `like '%" + <parameter> + "%'`, completely bypassing query parameterization and exposing the system to injection attacks.
**Prevention:** Consistently utilize parameterized queries for ALL user input via `java.util.Map<String, Object>` passed into the `findByJpql` facade methods instead of interpolating strings directly into the query.

## 2025-06-17 - Prevent JPQL Injection and Cache Pollution in ApiMembership
**Vulnerability:** Found string concatenations in JPQL query formations (e.g. `String sql = "Select f from ItemFee f where f.retired=false and f.item.id = " + item.getId();`). While `.getId()` is generally strongly typed to return numeric objects mitigating some injection scenarios, this pattern causes statement cache pollution and is a bad security hygiene.
**Learning:** Found over 100 occurrences of such unparameterized string concatenations.
**Prevention:** Continuously monitor and refactor occurrences to strictly use map parameterized queries with `findByJpql(sql, params)`.

## 2026-06-18 - Parameterized JPQL Query Fixes using Fully Qualified Names
**Vulnerability:** Unparameterized string concatenation in JPQL queries creating SQL injection vulnerabilities.
**Learning:** When applying security fixes to existing Java files to resolve SQL injection, utilizing fully qualified class names like `java.util.Map` and `java.util.HashMap` within the method prevents the need to alter imports at the top of the file, reducing the risk of build failures or conflicting imports.
**Prevention:** Use parameterized queries with a parameter map passed to `findByJpql(sql, map)`, employing fully qualified class names when instantiating the map inline.
## 2024-05-24 - [Fix SQL Injection in ClinicController]
**Vulnerability:** Found a SQL injection vulnerability in `ClinicController.java` (`completeStaff` method) where user input (`query`) and property properties (`speciality.id`) were directly concatenated into JPQL strings, putting the application at risk of critical SQL injection.
**Learning:** JPQL string concatenation is prevalent across multiple controller files. When fixing `like '%" + value + "%'` patterns, remember to also parameterize adjacent fields like IDs (e.g. `speciality.id = ` + getSpeciality().getId()) in the same query string. Using fully qualified names (e.g., `java.util.Map`) for injected data types helps avoid potential compilation issues due to missing imports. Also when converting `query.toUpperCase()` inside a LIKE clause to parameterized queries, use the JPQL `upper()` function around the entity attribute (e.g. `upper(p.person.name) like :q`) to preserve the case-insensitive search logic.
**Prevention:** Always use parameterized JPQL queries with a `Map<String, Object>` rather than string concatenation when passing user inputs or variables into queries.
## 2026-07-05 - SQL Injection in InvestigationItem Controller
**Vulnerability:** Found critical JPQL injection in `InvestigationItemController.java` and `InvestigationItemDynamicLabelController.java` where user input was directly concatenated into `LIKE` clauses.
**Learning:** Similar to past findings, the application's search functionality inside JSF controllers frequently builds JPQL dynamically using string concatenation, completely bypassing query parameterization and exposing the system to injection attacks. The use of `upper(c.name)` helps preserve case-insensitive search logic when migrating to parameterized queries.
**Prevention:** Always use parameterized queries (e.g. `where upper(c.name) like :q`) with a `Map<String, Object>` to pass parameters securely.
