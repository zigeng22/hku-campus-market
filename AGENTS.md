# AGENTS.md

## Source of Truth

- Read `E:\7506_project\project_docs\7506_MASTER_DEVELOPMENT_PLAN.md` before implementing any project task.
- Every coding change must reference one task ID from the master plan.
- The contract in sections 5 and 6 is frozen after `CONTRACT-01`; do not change public model fields, repository signatures, Intent extras, state values, layout names, or View IDs unless the assigned task explicitly authorizes a contract change.

## Project Boundaries

- Course materials and planning stay under `E:\7506_project`.
- Android code stays under `E:\7506_project\Android_Studio_files`.
- Use Java 11 + XML, AppCompat/Material, SQLiteOpenHelper, and SharedPreferences.
- Do not convert to Kotlin or Compose.
- Do not add Firebase, remote backends, payments, maps, push notifications, chat, recommendation systems, or admin tools unless the user explicitly changes scope.
- UI code must not execute SQL. Data access goes through `MarketRepository`.
- Monetary values use integer cents. SharedPreferences stores only session identity, never passwords.

## Work Discipline

- Implement one verifiable task at a time and stay within its declared file scope.
- Before editing, inspect `git status` and read every relevant Java, XML, Gradle, contract, and handoff file.
- Preserve user and teammate changes. Do not rewrite unrelated files or silently alter frozen contracts.
- Prefer existing project patterns and official Android dependencies. Avoid new frameworks during P0.
- Run `assembleDebug`, relevant tests, and `git diff --check` when feasible.
- Report changed files, verification results, manual test steps, remaining risks, and whether any frozen contract changed.

## Priority

1. P0: authentication, item publish/search/detail, offers, atomic deal confirmation, management center.
2. P1 only after the complete Alice/Bob flow is stable.
3. P2 remains documentation-only unless explicitly approved.

## File Safety

- Never bulk-delete files, folders, or directories. Empty folders may be deleted.
- Never use `del /s`, `rd /s`, `rmdir /s`, `Remove-Item -Recurse`, or `rm -rf`.
- Delete at most one explicitly named file per operation.
- Do not modify the original course PDF or PRD DOCX unless explicitly asked.
