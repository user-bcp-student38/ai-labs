# Proposal

## Why

GitHub Issue [#26](https://github.com/user-bcp-student38/ai-labs/issues/26) reports that SQL assembled from login credentials can allow authentication without valid credentials. The change formalizes rejection of such attempts while preserving the documented `POST /auth/login` contract.

## What Changes

- Define the observable security requirement that SQL input in either credential cannot bypass authentication.
- Preserve the request validation and response behavior documented in `docs/ENDPOINTS.md`.
- Keep the change limited to the `auth` login capability.

## Capabilities

### New Capabilities

- `auth-login`: observable behavior and security contract for `POST /auth/login`.

### Modified Capabilities

None. The repository has no existing main specs; this change introduces the first spec for the login capability.

## Impact

- Component: `auth/`; endpoint: `POST /auth/login`.
- No changes to `api/`, `web/`, request/response formats, JWT behavior, or authorization.
- The issue cites `LegacyDatabase.findByCredentials`, which is not present at the cited path in the current checkout. The current source uses `UserRepository`; the implementation target must be reconciled during design without changing this proposal's behavioral scope.

## Verifiable Requirements

- SQL injection input in the username or password cannot authenticate a user and returns the documented invalid-credentials response without a token.
- Valid seeded credentials continue to return HTTP 200 with the documented token, expiration, and user fields.
- Missing credentials, invalid credentials, and unexpected server errors retain the documented status codes and response messages.

## Observable Scenarios

- A valid seeded login returns HTTP 200 and the response fields documented in `docs/ENDPOINTS.md`.
- A SQL injection attempt in either credential returns HTTP 401, the generic invalid-credentials message, and no token.
- Missing username/password return HTTP 400; wrong credentials return HTTP 401; unexpected errors return HTTP 500 with the documented generic message.