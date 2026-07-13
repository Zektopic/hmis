## 2026-07-13 - [InstitutionLabSumeryController JPQL Injection Fix]
**Vulnerability:** JPQL Injection in `InstitutionLabSumeryController.java` (`searchAll` and `createPatientInvestigaationList`) where `txtSearch` user input was concatenated directly into queries.
**Learning:** JPQL queries using string concatenation to enforce case insensitivity (e.g., `like '%" + txtSearch.toUpperCase() + "%'`) are a recurring SQL injection vector in the codebase.
**Prevention:** Use parameterized map queries (e.g., `findByJpql(sql, m, 50)`) and the JPQL `upper(...)` function for case-insensitive `LIKE` searches (e.g., `upper(p.name) like :q`) to safely maintain expected behavior without string concatenation.
