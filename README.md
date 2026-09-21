# User Management & Authentication Service

A production-oriented, stateless user management, authentication, and authorization service built with **Spring Boot and Spring Security**.

The project demonstrates real-world backend security patterns including **JWT authentication, refresh token rotation, RBAC, email verification, password reset, password change, rate limiting, audit logging, and centralized exception handling**.

The goal of this project is to build a secure and maintainable backend that can later be consumed by a web or mobile UI.

---

## Features

### Authentication

* User registration with secure password hashing using BCrypt
* Email verification
* Login with short-lived JWT access tokens
* Refresh token-based session renewal
* Refresh token rotation
* Stateless authentication
* Secure logout through refresh token revocation
* Forgot-password flow
* Password reset using time-limited tokens
* Password change for authenticated users

### Authorization

* Role-Based Access Control (RBAC)
* `USER` and `ADMIN` roles
* Method-level authorization using `@PreAuthorize`
* Admin-only user management APIs
* User APIs restricted to the authenticated user's own account

### User Management

Authenticated users can:

* View their own profile
* Update their profile
* Change their password

Administrators can:

* Create users
* Retrieve users
* Update/manage users
* Delete users

### Token Management

* Short-lived JWT access tokens
* Long-lived refresh tokens stored server-side
* Refresh token rotation on every refresh
* Refresh token revocation on logout
* Refresh token reuse detection
* Revocation of all user refresh tokens when token reuse is detected
* Refresh token revocation after password reset

### Email Verification

* Verification token generated during registration
* Time-limited verification tokens
* Tokens can only be used once
* User remains `PENDING_VERIFICATION` until verification
* Successful verification activates the account

### Password Management

#### Forgot Password

```text
POST /api/v1/auth/forgot-password
```

The API intentionally returns a generic response regardless of whether the email exists to prevent user enumeration.

A time-limited password reset token is sent to the user's email.

#### Reset Password

```text
POST /api/v1/auth/reset-password
```

A valid reset token allows the user to set a new password.

Existing refresh tokens are revoked after a successful password reset.

#### Change Password

```text
POST /api/v1/users/change-password
```

Available to authenticated users who provide their current password and a new password.

---

## Security Hardening

* Stateless JWT-based authentication
* JWT signature and expiration validation
* Current user status checked against the database during JWT authentication
* Disabled, locked, and pending users cannot authenticate using existing JWTs
* BCrypt password hashing
* Server-side refresh token storage
* Refresh token rotation
* Refresh token reuse detection
* Password reset token expiration and single-use validation
* Generic authentication and password-reset responses
* IP-based rate limiting
* Centralized exception handling
* Audit logging for security-sensitive operations
* No sensitive authentication information exposed through error responses

---

# Architecture Overview

## High-Level Design

```text
                    Client
                Web / Mobile / API
                       |
                       |
             Authorization: Bearer JWT
                       |
                       v
              +-------------------+
              |   JWT Auth Filter |
              +-------------------+
                       |
                       v
              +-------------------+
              | Spring Security   |
              | Security Context  |
              +-------------------+
                       |
                       v
             +--------------------+
             | Controllers        |
             | @PreAuthorize      |
             +--------------------+
                       |
                       v
             +--------------------+
             | Business Services  |
             +--------------------+
                       |
             +---------+----------+
             |                    |
             v                    v
        +---------+        +-------------+
        | MySQL   |        | Email       |
        | Database|        | Service     |
        +---------+        +-------------+
```

---

## JWT Authentication Flow

```text
Client
  |
  | Authorization: Bearer <JWT>
  v
JwtAuthFilter
  |
  ├─ Extract JWT
  |
  ├─ Extract user email
  |
  ├─ Load current user from database
  |
  ├─ Check user status
  |     └─ Must be ACTIVE
  |
  ├─ Validate JWT signature
  |
  ├─ Validate expiration
  |
  v
CustomUserPrincipal
  |
  v
Spring SecurityContext
  |
  v
Protected Controller
```

Loading the current user from the database allows changes to account status or role to take effect even if an older JWT has not yet expired.

---

# Token Lifecycle

## Login

```text
Login
 |
 ├─ Validate credentials
 ├─ Verify user is ACTIVE
 ├─ Generate Access Token
 │    └─ 15 minutes
 |
 └─ Generate Refresh Token
      └─ 7 days
          └─ Stored in database
```

## Refresh

```text
Access Token Expires
 |
 v
POST /api/v1/auth/refresh
 |
 ├─ Validate refresh token
 ├─ Check expiration
 ├─ Check revocation status
 |
 ├─ Revoke old refresh token
 |
 ├─ Generate new refresh token
 |
 └─ Generate new access token
```

## Refresh Token Reuse Detection

```text
Previously revoked refresh token used
 |
 v
Possible token theft
 |
 v
Revoke ALL refresh tokens
for the affected user
 |
 v
User must authenticate again
```

## Logout

```text
POST /api/v1/auth/logout
 |
 v
Refresh Token
 |
 v
Revoke user's refresh tokens
 |
 v
Refresh token can no longer
be used to obtain new access tokens
```

> Logout does not immediately invalidate an already-issued JWT access token. Access tokens remain valid until their expiration because JWT authentication is stateless.

---

# User Account Lifecycle

```text
Registration
     |
     v
PENDING_VERIFICATION
     |
     | Email verification
     v
   ACTIVE
     |
     +----------------+
     |                |
     v                v
  LOCKED          DISABLED
```

Available statuses:

```text
ACTIVE
PENDING_VERIFICATION
LOCKED
DISABLED
```
Method	Endpoint	        Description
POST	/api/v1/auth/register	User registration
POST	/api/v1/auth/login	User login
POST	/api/v1/auth/refresh	Issue new access & refresh token
POST	/api/v1/auth/logout	Logout (revoke refresh token)
```
---

# Audit Logging

Security-sensitive operations are recorded through an AOP-based auditing mechanism.

Examples include:

```text
LOGIN_SUCCESS
LOGIN_FAILURE
REGISTRATION
PASSWORD_CHANGE
PASSWORD_RESET
LOGOUT
EMAIL_VERIFICATION
```

Audit records can contain information such as:

```text
User
Event
IP Address
Timestamp
```

Audit logging is implemented separately from business logic using **Spring AOP**, allowing security events to be recorded without adding repetitive logging code throughout every service method.

---

# Rate Limiting

Authentication endpoints are protected using IP-based rate limiting.

| Endpoint                | Limit                     |
| ----------------------- |--------------------------:|
| `/api/v1/auth/login`    |  5 requests / minute / IP |
| `/api/v1/auth/register` |  3 requests / minute / IP |

When the limit is exceeded:

```text
HTTP 429 Too Many Requests
```

The current implementation uses an in-memory rate limiter.

For a distributed deployment with multiple application instances, this can later be replaced with a shared solution such as Redis.

---

# API Endpoints

## Authentication

| Method | Endpoint                       | Description            | Authentication |
| ------ | ------------------------------ | ---------------------- | -------------- |
| POST   | `/api/v1/auth/register`        | Register user          | Public         |
| POST   | `/api/v1/auth/login`           | Authenticate user      | Public         |
| POST   | `/api/v1/auth/refresh`         | Refresh access token   | Public         |
| POST   | `/api/v1/auth/logout`          | Revoke refresh token   | Public         |
| GET    | `/api/v1/auth/verify`          | Verify email           | Public         |
| POST   | `/api/v1/auth/forgot-password` | Request password reset | Public         |
| POST   | `/api/v1/auth/reset-password`  | Reset password         | Public         |

## User

| Method | Endpoint                        | Description                    |
| ------ | ------------------------------- | ------------------------------ |
| GET    | `/api/v1/users/me`              | Get current user's profile     |
| PUT    | `/api/v1/users/me`              | Update current user's profile  |
| POST   | `/api/v1/users/change-password` | Change current user's password |

## Admin

| Method  | Endpoint                   | Description    |
| ------- | -------------------------- | -------------- |
| POST    | `/api/v1/admin/users`      | Create a user  |
| GET     | `/api/v1/admin/users`      | Retrieve users |
| GET/PUT | `/api/v1/admin/users/{id}` | Manage a user  |
| DELETE  | `/api/v1/admin/users/{id}` | Delete a user  |

Admin endpoints require:

```text
ROLE_ADMIN
```

---

# Tech Stack

* **Language:** Java
* **Framework:** Spring Boot
* **Security:** Spring Security
* **Authentication:** JWT
* **Authorization:** Role-Based Access Control (RBAC)
* **Password Hashing:** BCrypt
* **Database:** MySQL
* **Testing Database:** H2
* **API:** REST
* **Rate Limiting:** In-memory IP-based rate limiting
* **Auditing:** Spring AOP
* **Email:** Email service
* **API Documentation:** Swagger / OpenAPI
* **Build Tool:** Maven
* **Containerization:** Docker / Docker Compose
* **Version Control:** Git / GitHub

---

# Testing

The project contains unit and application context tests.

Basic test coverage includes:

* Authentication
* User registration
* Invalid credentials
* Inactive users
* Refresh token rotation
* Refresh token expiration
* Refresh token reuse detection
* Password reset
* Email verification
* Rate limiting
* JWT generation and validation
* User management
* Audit logging

The application context test uses a test profile and H2 database so tests do not depend on the development MySQL database.

Run all tests:

```bash
mvn test
```
---

# Running Locally with Docker

## 1. Environment Variables

Create a `.env` file in the root directory:

```env

DB_URL=jdbc:mysql://mysql:3306/user_mgmt_service
DB_USERNAME=user-name
DB_PASSWORD=your-password

JWT_SECRET=your-secret-key

EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# MySQL Docker image variables
MYSQL_DATABASE=user_mgmt_service
MYSQL_ROOT_PASSWORD=your-password

```


## 2. Build and Start Containers

```bash
docker-compose up -d --build
```

The application will be available at:

```text
http://localhost:2023
```

Swagger UI:

```text
http://localhost:2023/swagger-ui.html
```

MySQL:

```text
localhost:3307
```

---

# Swagger / OpenAPI

Once the application is running, open:

```text
http://localhost:2023/swagger-ui.html
```

Swagger can be used to test the REST APIs before the frontend UI is implemented.

For protected APIs:

```text
Authorization: Bearer <JWT_TOKEN>
```

---

# Example API Requests

## Register

```http
POST /api/v1/auth/register
```

```json
{
  "name": "Sahithi",
  "email": "sahithi@gmail.com",
  "password": "password123"
}
```

The user is initially created with:

```text
status = PENDING_VERIFICATION
emailVerified = false
role = USER
```

---

## Login

```http
POST /api/v1/auth/login
```

```json
{
  "identifier": "sahithi@gmail.com",
  "password": "password123"
}
```

Successful authentication returns:

```json
{
  "accessToken": "<JWT>",
  "refreshToken": "<REFRESH_TOKEN>",
  "expiresIn": 900
}
```

---

## Get Current User

```http
GET /api/v1/users/me
```

Header:

```text
Authorization: Bearer <JWT_TOKEN>
```

---

## Change Password

```http
POST /api/v1/users/change-password
```

Header:

```text
Authorization: Bearer <JWT_TOKEN>
```

---

## Forgot Password

```http
POST /api/v1/auth/forgot-password
```

```json
{
  "email": "sahithi@gmail.com"
}
```

The API returns a generic response:

```text
If an account exists with this email, a password reset link has been sent.
```

This prevents attackers from discovering whether an email address is registered.

---

# Running Without Docker

If MySQL is installed locally, configure:

```env
DB_URL=jdbc:mysql://localhost:3306/user_db
DB_USERNAME=your-user-name
DB_PASSWORD=your-password
```

Then run:

```bash
mvn spring-boot:run
```

The application will be available at:

```text
http://localhost:2023
```

Swagger:

```text
http://localhost:2023/swagger-ui.html
```

---
# Project Goals

This project is being developed incrementally with a focus on production-oriented backend practices.

Current focus areas include:

* Secure authentication
* Authorization
* Token lifecycle management
* User management
* Password security
* Email verification
* Audit logging
* Rate limiting
* Automated testing
* Dockerization
