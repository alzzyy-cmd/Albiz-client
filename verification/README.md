# verification/

The scripts used for the checks in `TEST_REPORT.md`, kept so the method can be inspected.
They contain paths from the sandbox they ran in (`/home/user/tools`, a JDK from the `jdk4py`
PyPI wheel, and the Eclipse compiler `ecj.jar` 3.39.0, which is not included) and need those
paths adjusted to run elsewhere. On a normal machine use Gradle instead: `bash gradlew test`.

- `mytests.sh <project>` — compiles the pure-Java classes added in this work together with their
  tests and runs them. Lists exactly which files are covered.
- `unit.sh` — the compile-and-run helper it calls.
- `junitstub/` — a minimal stand-in for `org.junit.jupiter.api.Test` and `Assertions`
  (real JUnit could not be downloaded). `runner/RunTests.java` invokes every `@Test` method.
- `check.sh <project> <files>` — compiles files with ECJ against the project sources and reports
  errors that are not "unresolved Minecraft/Fabric type".
- `cmp.sh <files>` — compares a file's ECJ error profile with the same file in the original
  source, to show an edit introduced no new error.
