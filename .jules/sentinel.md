## 2026-07-27 - Parameterize dynamic string concatenations in RoomCategoryController
**Vulnerability:** JPQL Injection in `RoomCategoryController` due to raw string concatenation (`+ getSelectText().toUpperCase() +`) when building SQL queries.
**Learning:** Raw string concatenations in JPQL for search filters bypass type checks and allow malicious users to inject additional SQL logic or extract unauthorized data. Parameterizing with `:paramName` completely eliminates this class of injection.
**Prevention:** Consistently use the `.findByJpql(String, Map<String, Object>)` method with named parameters instead of raw string concatenations for dynamically generated user filters in JPQL.
## 2026-08-31 - Fix Path Traversal in File Uploads
**Vulnerability:** Path Traversal via un-sanitized `file.getFileName()` concatenation in `AttendanceUploadController.java`.
**Learning:** When generating a temporary file based on a user's uploaded filename, simply concatenating a timestamp and the original filename (e.g., `new File(timestamp + file.getFileName())`) is vulnerable to path traversal. An attacker could specify a file name like `../../../malicious.txt`, overriding the intended directory constraints.
**Prevention:** Always extract only the base file name from the uploaded file payload using `java.nio.file.Paths.get(file.getFileName()).getFileName().toString()` before concatenating or saving it.
## 2026-09-08 - Parameterize JPQL queries in TheatreServiceController
**Vulnerability:** JPQL Injection in `TheatreServiceController` due to raw string concatenation when building search filters with user input.
**Learning:** Raw string concatenations for `LIKE` clauses in JPQL (e.g., `(c.name) like '%" + val + "%'`) are highly vulnerable to injection. Also, if applying `.toUpperCase()` to user input, the JPQL column should be explicitly transformed via `upper()` (e.g., `upper(c.name) like :param`) to correctly mimic the case-insensitive search logic.
**Prevention:** Replace string concatenations with parameterized queries (using `:paramName`) and pass parameters using a `Map` to the facade's `findByJpql` method.

