# Spec Delta

## Purpose

The auth-login capability defines the observable login contract and ensures that untrusted credential input cannot bypass authentication.

## ADDED Requirements

### Requirement: Login rejects SQL injection attempts

The login endpoint MUST treat username and password as credential values only. SQL injection input MUST NOT bypass authentication and MUST be rejected as invalid credentials.

#### Scenario: SQL injection attempt in username
- **WHEN** a login request contains SQL injection input in the username
- **THEN** the response is HTTP 401 with `{"message":"Invalid username or password"}` and contains no token

#### Scenario: SQL injection attempt in password
- **WHEN** a login request contains SQL injection input in the password
- **THEN** the response is HTTP 401 with `{"message":"Invalid username or password"}` and contains no token

### Requirement: Login preserves the documented endpoint contract

The login endpoint MUST preserve the request validation, response status codes, and response fields documented for `POST /auth/login` in `docs/ENDPOINTS.md`.

#### Scenario: Valid seeded credentials
- **WHEN** `student` submits the valid password `Workshop123!` to `POST /auth/login`
- **THEN** the response is HTTP 200 with a token, `expiresIn` equal to 3600, and a user object containing `username`, `displayName`, and `role`

#### Scenario: Username is missing or blank
- **WHEN** a login request has a missing or blank username
- **THEN** the response is HTTP 400 with `{"message":"Username is required"}`

#### Scenario: Password is missing or blank
- **WHEN** a login request has a missing or blank password
- **THEN** the response is HTTP 400 with `{"message":"Password is required"}`

#### Scenario: Credentials are invalid
- **WHEN** a login request has an unknown username or an incorrect password
- **THEN** the response is HTTP 401 with `{"message":"Invalid username or password"}`

#### Scenario: Unexpected server error
- **WHEN** an unexpected error prevents processing a login request
- **THEN** the response is HTTP 500 with `{"message":"No se pudo procesar la solicitud"}` and no internal error details