# User Management & Authentication Service

A production-grade, stateless authentication and authorization service built using Spring Boot and Spring Security, implementing industry-standard security practices such as JWT authentication, refresh tokens, RBAC, rate limiting, and secure logout.

This project demonstrates how real-world backend systems handle user identity, access control, and security at scale.

## Features
a. Authentication

- User registration with secure password hashing (BCrypt)
- Login with JWT Access Token
- Refresh Token–based session renewal
- Stateless authentication (no HTTP sessions)

b. Authorization
- Role-Based Access Control (RBAC)
- Method-level security using @PreAuthorize
- Role enforcement at service & controller layers

c. Token Management
- Short-lived access tokens
- Long-lived refresh tokens stored server-side
- Refresh token revocation on logout

d. Security Hardening
- Rate limiting on login & registration APIs
- Generic authentication error responses (prevents user enumeration)
- Stateless security configuration
- Centralized exception handling

## Architecture Overview
### High-Level Design
```

Client (Web / Mobile)
|
|  Authorization: Bearer <JWT>
v
JWT Authorization Filter
|
v
Spring Security Context
|
v
Protected Controllers (@PreAuthorize)
|
v
Business Services
|
v
Database (Users, Refresh Tokens)

```
### Token Lifecycle
```
Login
├─ Validate credentials
├─ Issue Access Token (15 min)
└─ Issue Refresh Token (7 days, stored in DB)

Access Token Expires
└─ Client calls /auth/refresh
├─ Validate refresh token
└─ Issue new access token

Logout
└─ Refresh token revoked (cannot issue new tokens)
```

## Tech Stack
- Language: Java
- Framework: Spring Boot
- Security: Spring Security
  - Authentication: JWT (Access Token & Refresh Token)
  - Authorization: Role-Based Access Control (RBAC) 
  - Password Hashing: BCrypt
- Database: MySQL
- API Design: RESTful APIs
- Rate Limiting: In-memory rate limiting (IP-based, configurable)
- Session Management: Stateless (no HTTP sessions)
- API Documentation: Swagger / OpenAPI
- Build Tool: Maven
- Version Control: Git & GitHub

## API Endpoints
**Authentication**
```
Method	Endpoint	Description
POST	/api/v1/auth/register	User registration
POST	/api/v1/auth/login	User login
POST	/api/v1/auth/refresh	Issue new access token
POST	/api/v1/auth/logout	Logout (revoke refresh token)
```
**Authorization**
```
Role	Endpoint
USER	/profile
ADMIN	/admin/users
```

## Security Design

- Stateless JWT authentication for horizontal scalability
- Refresh tokens stored server-side to support logout & revocation
- RBAC enforced at method level to protect business logic
- Rate limiting to prevent brute-force and abuse
- Generic error messages to avoid leaking sensitive information


### Rate Limiting Policy
```
Endpoint	Limit
/auth/login	5 requests / minute / IP
/auth/register	3 requests / minute / IP
```
- Returns HTTP 429 (Too Many Requests) on violation.