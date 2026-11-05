# Tasks

## 1. Characterize the auth login contract

- [x] 1.1 Strengthen the valid-login test to assert the documented token, `expiresIn`, and user fields; verify with `mvn test` from `auth/`.
- [x] 1.2 Characterize missing and blank username/password plus invalid credentials, including the documented status and message; verify with `mvn test` from `auth/`.
- [x] 1.3 Cover SQL injection attempts in both username and password and assert HTTP 401, the generic invalid-credentials message, and no token; verify with `mvn test` from `auth/`.

## 2. Apply only a necessary auth-layer correction

- [x] 2.1 Compare characterization results and the current repository/service flow with Issue #26; retain bound username lookup and BCrypt password verification, making only a minimal correction in the existing auth layer if a gap is demonstrated. Verify injection cases and valid login with `mvn test` from `auth/`.

## 3. Validate the auth change

- [x] 3.1 Run the complete auth test suite with `mvn test` from `auth/` and confirm all scenarios in `specs/auth-login/spec.md` pass; if production code changed, review the JaCoCo report and confirm at least 85% coverage for modified code.