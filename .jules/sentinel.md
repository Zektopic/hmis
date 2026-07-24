## YYYY-MM-DD - Fix Path Traversal in PharmacyItemExcelManager
**Vulnerability:** Path traversal vulnerability existed because `file.getFileName()` was directly concatenated to paths without sanitization when saving uploaded Excel files in `PharmacyItemExcelManager.java`.
**Learning:** `UploadedFile` from JSF/PrimeFaces does not automatically sanitize file paths, making it possible for attackers to inject `../` sequences into the generated file paths.
**Prevention:** Always use `java.nio.file.Paths.get(filename).getFileName().toString()` to extract only the actual filename component when dealing with user-uploaded files.
