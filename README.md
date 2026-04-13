🚀 TaskFlow — Scalable Backend API (Spring Boot 21 + Docker)
📌 Overview

TaskFlow is a secure, production-grade backend system for managing projects and tasks with authentication and role-based access.

Designed with clean architecture, strong security, and test coverage, this API delivers a reliable foundation for real-world task management systems.

⚡ Tech Stack
Java 21
Spring Boot
Spring Security + JWT
PostgreSQL
JUnit + Mockito
Docker & Docker Compose
Maven
🔐 Core Features
🔑 JWT-based authentication (24h expiry)
🔒 Secure password hashing using bcrypt
👥 Role-based authorization (owner & assignee rules)
📁 Full CRUD for Projects & Tasks
⚠️ Centralized exception handling
🧪 Fully tested APIs (Auth, Security, Project, Task)
🧱 Clean layered architecture
🧠 Architecture Decisions
🔹 Layered Design
```
Controller → Service → Repository → Database
```
🔹 Key Design Choices
Stateless Authentication (JWT) → scalable & cloud-ready
DTO Pattern → clean API contracts, avoids entity leakage
Global Exception Handler → consistent error responses
Validation Layer → early failure & clean logic
🔹 Trade-offs
Chose Spring Boot over Go for faster delivery and ecosystem strength
Focused on correctness + security over adding non-core features
Pagination & analytics kept minimal to prioritize core flows

🐳 Run with Docker (Recommended)
✅ Prerequisites
Docker
Docker Compose
🚀 Steps to Run

```
git clone https://github.com/your-username/taskflow
cd taskflow

# setup environment
cp .env.example .env

# run full stack (DB + API)
docker compose up --build
``
🌐 Services
Service	URL
Backend API	http://localhost:8080

PostgreSQL	localhost:5432
⚙️ Docker Setup Details
📦 docker-compose.yml includes:
PostgreSQL container
Spring Boot API container
Network + environment configuration
🧩 Backend Dockerfile (multi-stage)
Build stage → Maven + JDK
Runtime stage → lightweight JDK image
Optimized for smaller image size
🗄️ Database & Migrations
PostgreSQL used as primary DB
Schema handled via migrations (Flyway/Liquibase)
✅ Auto-run on startup

OR manually:

mvn flyway:migrate
🔑 Test Credentials
Email:    test@example.com
Password: password123
📡 API Reference
🔐 Auth
POST /auth/register
POST /auth/login
📁 Projects
GET /projects
POST /projects
GET /projects/{id}
PATCH /projects/{id}
DELETE /projects/{id}
✅ Tasks
GET /projects/{id}/tasks?status=&assignee=
POST /projects/{id}/tasks
PATCH /tasks/{id}
DELETE /tasks/{id}
🔒 Authorization Header
Authorization: Bearer <JWT_TOKEN>
🧪 Test Coverage
Module	Status
Auth	🟢 Passed
Security	🟢 Passed
Projects	🟢 Passed
Tasks	🟢 Passed

✔ Covers edge cases
✔ Validates authorization
✔ Ensures API correctness

⚠️ Error Handling
Example Response
{
  "error": "validation failed",
  "fields": {
    "email": "is required"
  }
}
Status Codes
400 → Validation Error
401 → Unauthorized
403 → Forbidden
404 → Not Found
📈 What I’d Improve With More Time
📄 Pagination & sorting support
📊 Project statistics endpoint
🧪 Integration tests with Testcontainers
📚 Swagger/OpenAPI documentation
⚡ Redis caching for performance
🔐 Rate limiting & audit logging
💡 Highlights
Production-ready API design
Strong security implementation
High-quality test coverage
Dockerized for easy setup
Clean, maintainable codebase
📎 Assignment Context

This project fulfills TaskFlow backend requirements including authentication, relational modeling, REST APIs, and testing