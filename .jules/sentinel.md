## 2026-07-27 - Parameterize dynamic string concatenations in RoomCategoryController
**Vulnerability:** JPQL Injection in `RoomCategoryController` due to raw string concatenation (`+ getSelectText().toUpperCase() +`) when building SQL queries.
**Learning:** Raw string concatenations in JPQL for search filters bypass type checks and allow malicious users to inject additional SQL logic or extract unauthorized data. Parameterizing with `:paramName` completely eliminates this class of injection.
**Prevention:** Consistently use the `.findByJpql(String, Map<String, Object>)` method with named parameters instead of raw string concatenations for dynamically generated user filters in JPQL.
