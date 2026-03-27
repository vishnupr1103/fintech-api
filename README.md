# Fintech Backend API

A Spring Boot backend simulating a user onboarding and transaction flow for a fintech application.

---

## Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Language     | Java 17                           |
| Framework    | Spring Boot 3.2.4                 |
| Security     | Spring Security + JWT             |
| ORM          | Spring Data JPA / Hibernate       |
| Database     | PostgreSQL                        |
| Docs         | Springdoc OpenAPI (Swagger UI)    |
| Build Tool   | Maven                             |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 13+

---

## Database Setup

```sql
CREATE DATABASE fintechdb;
```

Update `src/main/resources/application.yml` if your credentials differ:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fintechdb
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## Run the Application

```bash
# Clone and enter the project
cd fintech-api

# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

Server starts at: **http://localhost:8080**

---

## Swagger UI

Visit: **http://localhost:8080/swagger-ui.html**


## 🚀 Deployment (Render)

The application is deployed on cloud using Render.

- 🌐 Base URL: https://fintech-api-8fm2.onrender.com
- 📄 Swagger UI: https://fintech-api-8fm2.onrender.com/swagger-ui/index.html

### Tech Details
- Backend deployed as a Web Service
- PostgreSQL database hosted on Render
- Environment variables used for configuration
- Internal database networking for secure connection
- Dockerized using multi-stage build

---

All endpoints are documented and testable directly from the browser.

---

## API Reference

### 1. Register User

**POST** `/api/register`

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "mobile": "9876543210"
}
```

**Response:** Returns userId and status = PENDING (Otp is included in response). 

---

### 2. Verify OTP

**POST** `/api/verify-otp`

```json
{
  "identifier": "9876543210",
  "otp": "123456"
}
```

**Response:** User status = ACTIVE. Account created with default balance ₹1000.

> OTP is valid for **5 minutes**. 

---

---

### 3. Resend OTP

**POST** `/api/resend-otp`

```json
{
  "identifier": "9876543210",
  "otp": "123456"
}
```

**Response:** User status = ACTIVE. Account created with default balance ₹1000.

> OTP is valid for **5 minutes**. Check the application console for the OTP value.

---

### 4. Login

**POST** `/api/login`

```json
{
  "identifier": "9876543210"
}
```

`identifier` can be mobile number **or** email.

**Response:** Returns a JWT Bearer token.

---

### 4. Transfer Money *(Requires JWT)*

**POST** `/api/transfer`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "senderUserId": 1,
  "receiverUserId": 2,
  "amount": 200.00,
  "description": "Rent payment"
}
```

---

### 5. Transaction History *(Requires JWT)*

**GET** `/api/transactions/{userId}`

**Headers:** `Authorization: Bearer <token>`

---

## Project Structure

```
fintech-api/
├── src/
│   ├── main/
│   │   ├── java/com/fintech/app/
│   │   │   ├── FintechApiApplication.java
│   │   │   ├── config/          # SecurityConfig, SwaggerConfig
│   │   │   ├── controller/      # AuthController, TransactionController
│   │   │   ├── dto/
│   │   │   │   ├── request/     # RegisterRequest, LoginRequest, etc.
│   │   │   │   └── response/    # ApiResponse, LoginResponse, etc.
│   │   │   ├── entity/          # User, Account, Transaction + Enums
│   │   │   ├── exception/       # GlobalExceptionHandler + custom exceptions
│   │   │   ├── repository/      # JPA Repositories
│   │   │   ├── security/        # JwtUtil, JwtAuthenticationFilter
│   │   │   ├── service/         # Service interfaces
│   │   │   └── serviceimpl/     # Service implementations
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ── java/com/fintech/app/
│    
└── pom.xml
```

---

## Key Design Decisions

- **Layered Architecture**: Controller → Service Interface → Service Impl → Repository
- **JWT Stateless Auth**: No sessions; every request must carry a Bearer token for protected routes
- **OTP Simulation**: OTP is included in the response (no SMS/email dependency for demo)
- **@Transactional**: Transfer operation is fully atomic — both debit and credit happen or neither does
- **Global Exception Handler**: Single `@RestControllerAdvice` handles all errors uniformly
- **Input Validation**: Bean validation (`@Valid`) on all request DTOs

---

## Postman Collection

Import `fintech-api.postman_collection.json` from the repo root. Steps:
1. Call **Register** → copy OTP from response
2. Call **Verify OTP** → account is activated
3. Call **Resend OTp** → generates new OTP
4. Call **Login** → copy the `token` from response
5. Set `Authorization: Bearer <token>` header for Transfer and Transaction History
