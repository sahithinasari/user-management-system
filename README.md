# User Management & Authentication Service

A production-grade, stateless authentication and authorization service built using Spring Boot and Spring Security, implementing industry-standard security practices such as JWT authentication, refresh tokens, RBAC, rate limiting, and secure logout.

This project demonstrates how real-world backend systems handle user identity, access control, and security at scale.

## Features
a. Authentication

- User registration with secure password hashing (BCrypt)
- Email verification
- Login with JWT Access Token
- Refresh Token–based session renewal with token rotation
- Stateless authentication (no HTTP sessions)

b. Authorization
- Role-Based Access Control (RBAC)
- Method-level security using @PreAuthorize
- Role enforcement at service & controller layers

c. Token Management
- Short-lived JWT access tokens
- Long-lived refresh tokens stored server-side
- Refresh token rotation on every refresh
- Automatic refresh token revocation on logout
- Refresh token reuse detection (theft prevention)

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
   ├─ Revoke old refresh token
   ├─ Issue new refresh token
   └─ Issue new access token

Logout
└─ Refresh token revoked (cannot issue new tokens)

Refresh Token Reuse Detected
└─ All refresh tokens for user revoked
   → User must re-authenticate
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
Method	Endpoint	        Description
POST	/api/v1/auth/register	User registration
POST	/api/v1/auth/login	User login
POST	/api/v1/auth/refresh	Issue new access & refresh token
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
- Rotating refresh tokens with reuse detection
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

## Running Locally (with Docker)

1. Environment Variables

    Create a `.env` file in the root directory:
    ```
    SPRING_PROFILES_ACTIVE=default
   
    DB_URL=jdbc:mysql://mysql:3306/user_db
    DB_USERNAME=user-name
    DB_PASSWORD=password
   
    JWT_SECRET=your-secret-key
   
    EMAIL_USERNAME=your-email@gmail.com
    EMAIL_PASSWORD=your-app-password
    ```

2. Build the JAR:
    ``` 
    mvn clean package -DskipTests
    ```

3. Build & Start Containers:
   ```
    docker-compose up -d --build
    ```
   

- Application: http://localhost:2023
- Swagger UI: http://localhost:2023/swagger-ui.html
- MySQL (Docker): localhost:3307 (root / root)

### API Endpoints

Once running, visit:

Swagger UI: http://localhost:2023/swagger-ui.html

Sample Endpoints:

POST /api/v1/auth/register

Request payload:
```
{
"username": "sahithi",
"email": "sahithi@gmail.com",
"password": "password123"
}
```

POST /api/v1/auth/login

Request payload:
```
{
"username": "sahithi",
"password": "password123"
}
```

GET /api/users/me

Header:
Authorization: Bearer <JWT_TOKEN>

Stopping the Containers
```
docker-compose down
```


## Running Without Docker

If you have MySQL installed locally

Update database configuration:
```
DB_URL=jdbc:mysql://localhost:3306/user_db
DB_USERNAME=your-user-name
DB_PASSWORD=your-password
```


Run the Spring Boot app directly:
```
mvn spring-boot:run
```


The app will be available on http://localhost:2023.
