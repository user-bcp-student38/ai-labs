# Design

## Context

See `proposal.md` for the motivation and `specs/auth-login/spec.md` for the required behavior. The affected flow is contained in `auth/`: `AuthResource` maps login exceptions to HTTP responses, `AuthService` validates credentials and checks the stored BCrypt hash, and `UserRepository.findByUsername` selects a user using a `PreparedStatement` with a bound username parameter.

The Issue references `LegacyDatabase.findByCredentials`, which is absent from this checkout. The current source already separates username lookup from password verification. Treat the current branch as the implementation baseline; do not recreate the missing class or move password verification into SQL.

## Goals / Non-Goals

**Goals:**
- Ensure untrusted username and password values cannot bypass authentication.
- Preserve the documented `POST /auth/login` request validation and response contract.
- Make the smallest auth-only change justified by characterization results.

**Non-Goals:**
- Change `api/`, `web/`, JWT creation or verification, database schema, or login request/response formats.
- Introduce a new persistence abstraction, dependency, or `LegacyDatabase` class.
- Refactor unrelated authentication behavior.

## Protected Contracts

- Valid seeded credentials return HTTP 200 with `token`, `expiresIn` equal to 3600, and `user.username`, `user.displayName`, and `user.role`.
- Missing or blank username/password return HTTP 400 with their documented messages.
- Unknown users, incorrect passwords, and SQL injection attempts return HTTP 401 with `Invalid username or password`; rejected responses contain no token.
- Unexpected errors return HTTP 500 with the documented generic message and no internal details.
- The optional `client` request field remains accepted and unvalidated. JWT claims and token format remain unchanged.

## Components Affected

- `auth/src/test/java/com/bcp/workshop/auth/AuthResourceTest.java`: characterize the endpoint contract and injection outcomes before any production behavior change.
- `auth/src/main/java/com/bcp/workshop/auth/UserRepository.java`: only if characterization or review finds a gap in the current parameter binding.
- `auth/src/main/java/com/bcp/workshop/auth/AuthService.java`: only if characterization shows credential verification does not reject invalid inputs as specified.
- `AuthResource` response mapping is protected, not a planned refactor target.

No files under `api/` or `web/` are affected. `docs/ENDPOINTS.md` remains the baseline and does not need unrelated edits.

## Decisions

### Keep username lookup and password verification separate

Keep the current flow: bind the username in the repository query, then verify the candidate password against the stored BCrypt hash in the service. This avoids comparing plaintext passwords in SQL and matches the current data model.

Alternatives considered:
- Recreate the issue's cited `LegacyDatabase.findByCredentials`: rejected because that class is absent from the current checkout and would duplicate the existing repository.
- Put username and password in a SQL credential query: rejected because passwords are stored as BCrypt hashes and are verified by the service; it would expand the change and weaken separation of responsibilities.

### Let characterization determine whether production code changes

First strengthen endpoint tests for the specified contract, including injection in both credential fields and absence of a token on rejection. Then run the focused auth tests. Since the current repository already uses a bound parameter and BCrypt verification, do not make a production change unless a characterization test or code review demonstrates a gap.

Alternatives considered:
- Change production code immediately to match the issue's class and method names: rejected because those names do not describe the current implementation.
- Treat the existing username-injection test as sufficient: rejected because it does not cover password injection or assert the response body and absence of a token.

## Incremental Strategy

1. In `AuthResourceTest`, characterize successful login fields and expiration, missing/blank credential validation, invalid-credential response, and rejection of injection in username and password with no token. Do not modify production code in this step.
2. Run the auth test suite from `auth/` and confirm the characterization tests pass against the current flow. If a test exposes a behavior mismatch, make only the smallest change in the owning auth layer and rerun the same tests.
3. Review the repository query and service verification to confirm user-controlled values remain bound or compared as credential data, never interpolated into SQL. Run the complete auth suite after any production change.

## Tests and Characterization

The existing `AuthResourceTest` already covers a valid login, missing username, missing password, wrong password, and username injection. Before changing production behavior, extend or refine it to assert the documented bodies, blank-field cases, `expiresIn`, absence of a token on 401, and injection in the password. Keep these tests at the HTTP boundary so they protect both the service and repository flow. Do not create tests or fixtures in other components.

## Risks / Trade-offs

- **Issue path differs from the current source** -> Use the checked-out `UserRepository`/`AuthService` flow as the target and require evidence from tests or review before changing production code.
- **A 401 assertion alone could miss accidental token output** -> Assert both the generic error body and that no token is present for every rejected credential scenario.
- **Persistent local SQLite data could affect test repeatability** -> Use the existing isolated auth test configuration and seeded test accounts; do not use production data or secrets.
- **Overcorrecting a behavior already implemented could add risk without benefit** -> Land characterization first and retain the current implementation when it satisfies the approved spec.

## Migration and Rollback

No database migration, API migration, or deployment configuration change is expected. If a production change is needed and causes an auth regression, revert only that implementation change in `auth/`, keep the contract tests, and rerun the auth suite. Do not roll back changes in `api/` or `web/`.