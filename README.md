# 🏥 Healthcare Management Platform

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen?style=for-the-badge&logo=springboot)
![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5?style=for-the-badge&logo=kubernetes)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**A production-ready, HIPAA-compliant microservices platform for healthcare management**

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [Services](#-microservices) • [API Docs](#-api-documentation) • [Security](#-security) • [DevOps](#-devops)

</div>

---

## 📋 Overview

The **Healthcare Management Platform** is a comprehensive, enterprise-grade microservices system designed to handle patient management, appointment scheduling, prescription tracking, billing, and more. Built with Spring Boot 4.0 and following cloud-native principles, this platform demonstrates production-ready patterns for healthcare applications.

### 🎯 Key Highlights

- **HIPAA Compliant** - End-to-end encryption, audit logging, and access controls
- **Microservices Architecture** - 8 independently deployable services
- **Event-Driven** - Asynchronous communication via RabbitMQ/Kafka
- **Cloud Native** - Docker + Kubernetes ready with CI/CD pipelines
- **Highly Secure** - OAuth2/OIDC with Keycloak, JWT, and RBAC
- **Observable** - Distributed tracing, centralized logging, and metrics dashboards

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT token-based authentication
- OAuth2/OIDC integration with Keycloak
- Role-Based Access Control (RBAC)
- Multi-factor authentication support
- Automatic session timeout for HIPAA compliance

### 👤 Patient Management
- Complete patient record management
- Medical history tracking
- Emergency contact management
- Insurance information storage
- HIPAA-compliant data encryption at rest

### 📅 Appointment Scheduling
- Real-time appointment booking
- Provider availability management
- Automated SMS/Email reminders
- Recurring appointment support
- Waitlist management

### 💊 Prescription Management
- Electronic prescribing (e-Rx)
- Drug interaction checking
- Refill requests and tracking
- Pharmacy integration
- Medication history

### 💰 Billing & Insurance
- Insurance claims processing
- Payment gateway integration
- Invoice generation
- Payment history tracking
- Insurance eligibility verification

### 📊 Reporting & Analytics
- Real-time dashboards
- Custom report generation
- Patient analytics
- Financial reporting
- Audit trail reports

---

## 🏗️ Architecture

```
                                    ┌─────────────────┐
                                    │   Client Apps   │
                                    │  (Web/Mobile)   │
                                    └────────┬────────┘
                                             │
                                             ▼
                                    ┌─────────────────┐
                                    │   API Gateway   │
                                    │   (Port 8080)   │
                                    └────────┬────────┘
                                             │
              ┌──────────────────────────────┼──────────────────────────────┐
              │                              │                              │
              ▼                              ▼                              ▼
    ┌─────────────────┐            ┌─────────────────┐            ┌─────────────────┐
    │  Auth Service   │            │ Patient Service │            │Appointment Svc  │
    │   (Port 8081)   │            │   (Port 8082)   │            │   (Port 8083)   │
    └─────────────────┘            └─────────────────┘            └─────────────────┘
              │                              │                              │
              │                              │                              │
              ▼                              ▼                              ▼
    ┌─────────────────┐            ┌─────────────────┐            ┌─────────────────┐
    │Prescription Svc │            │ Billing Service │            │Notification Svc │
    │   (Port 8084)   │            │   (Port 8085)   │            │   (Port 8086)   │
    └─────────────────┘            └─────────────────┘            └─────────────────┘
              │                              │                              │
              └──────────────────────────────┼──────────────────────────────┘
                                             │
              ┌──────────────────────────────┼──────────────────────────────┐
              │                              │                              │
              ▼                              ▼                              ▼
    ┌─────────────────┐            ┌─────────────────┐            ┌─────────────────┐
    │  Eureka Server  │            │  Config Server  │            │    RabbitMQ     │
    │   (Port 8761)   │            │   (Port 8888)   │            │   (Port 5672)   │
    └─────────────────┘            └─────────────────┘            └─────────────────┘
```

### Technology Stack

| Layer | Technology |
|-------|------------|
| **Runtime** | Java 17, Spring Boot 4.0 |
| **Security** | Spring Security, OAuth2, Keycloak, JWT |
| **Database** | PostgreSQL 15 (per-service isolation) |
| **Messaging** | RabbitMQ, Apache Kafka |
| **Discovery** | Netflix Eureka |
| **Gateway** | Spring Cloud Gateway |
| **Config** | Spring Cloud Config Server |
| **Resilience** | Resilience4j (Circuit Breaker, Rate Limiting) |
| **Tracing** | Spring Cloud Sleuth, Zipkin |
| **Logging** | ELK Stack (Elasticsearch, Logstash, Kibana) |
| **Monitoring** | Spring Actuator, Prometheus, Grafana |
| **Containerization** | Docker, Docker Compose |
| **Orchestration** | Kubernetes |
| **CI/CD** | GitHub Actions |

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)
- **PostgreSQL 15** (or use Docker)

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/ganainy/healthcare-platform.git
cd healthcare-platform

# Start all services with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Option 2: Local Development

```bash
# Clone the repository
git clone https://github.com/ganainy/healthcare-platform.git
cd healthcare-platform

# Start infrastructure services (PostgreSQL, RabbitMQ, Redis)
docker-compose -f docker-compose.infra.yml up -d

# Start Eureka Server first
cd eureka-server && mvn spring-boot:run &

# Start Config Server
cd ../config-server && mvn spring-boot:run &

# Start remaining microservices in any order
cd ../auth-service && mvn spring-boot:run &
cd ../patient-service && mvn spring-boot:run &
cd ../appointment-service && mvn spring-boot:run &
cd ../prescription-service && mvn spring-boot:run &
cd ../billing-service && mvn spring-boot:run &
cd ../notification-service && mvn spring-boot:run &

# Start API Gateway last
cd ../api-gateway && mvn spring-boot:run &
```

### Verify the Installation

| Service | URL | Description |
|---------|-----|-------------|
| **API Gateway** | http://localhost:8080 | Main entry point |
| **Eureka Dashboard** | http://localhost:8761 | Service discovery |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API Documentation |
| **Keycloak** | http://localhost:8090 | Identity Provider |
| **Zipkin** | http://localhost:9411 | Distributed Tracing |
| **Grafana** | http://localhost:3000 | Metrics Dashboard |
| **Kibana** | http://localhost:5601 | Log Analytics |

---

## 📦 Microservices

### auth-service (Port 8081)
**Authentication & Authorization Service**

Handles all authentication and authorization concerns for the platform.

**Features:**
- User registration and login
- JWT token generation and validation
- OAuth2/OIDC integration
- Role and permission management
- Password reset functionality
- Session management
- Audit logging for access attempts

**Endpoints:**
```
POST   /api/v1/auth/register        - Register new user
POST   /api/v1/auth/login           - User login
POST   /api/v1/auth/refresh         - Refresh access token
POST   /api/v1/auth/logout          - User logout
GET    /api/v1/auth/me              - Get current user
PUT    /api/v1/auth/password        - Change password
POST   /api/v1/auth/password/reset  - Request password reset
```

### patient-service (Port 8082)
**Patient Record Management Service**

Manages all patient-related data with HIPAA compliance.

**Features:**
- Patient CRUD operations
- Medical history management
- Emergency contact management
- Insurance information
- Document upload and storage
- Field-level encryption for PHI

**Endpoints:**
```
POST   /api/v1/patients             - Create patient
GET    /api/v1/patients/{id}        - Get patient by ID
GET    /api/v1/patients             - List patients (paginated)
PUT    /api/v1/patients/{id}        - Update patient
DELETE /api/v1/patients/{id}        - Delete patient (soft delete)
GET    /api/v1/patients/{id}/history - Get medical history
POST   /api/v1/patients/{id}/documents - Upload document
```

### appointment-service (Port 8083)
**Appointment Scheduling Service**

Handles appointment booking, scheduling, and provider management.

**Features:**
- Appointment CRUD operations
- Provider availability management
- Conflict detection
- Recurring appointments
- Waitlist management
- Calendar integration

**Endpoints:**
```
POST   /api/v1/appointments                    - Create appointment
GET    /api/v1/appointments/{id}               - Get appointment
GET    /api/v1/appointments                    - List appointments
PUT    /api/v1/appointments/{id}               - Update appointment
DELETE /api/v1/appointments/{id}               - Cancel appointment
GET    /api/v1/appointments/provider/{id}/slots - Get available slots
POST   /api/v1/appointments/{id}/reschedule    - Reschedule appointment
```

### prescription-service (Port 8084)
**Prescription Management Service**

Manages electronic prescriptions and medication tracking.

**Features:**
- Electronic prescribing
- Drug interaction checking
- Refill management
- Pharmacy integration
- Medication reminders
- Prescription history

**Endpoints:**
```
POST   /api/v1/prescriptions              - Create prescription
GET    /api/v1/prescriptions/{id}         - Get prescription
GET    /api/v1/prescriptions              - List prescriptions
PUT    /api/v1/prescriptions/{id}         - Update prescription
DELETE /api/v1/prescriptions/{id}         - Cancel prescription
POST   /api/v1/prescriptions/{id}/refill  - Request refill
GET    /api/v1/prescriptions/patient/{id} - Get patient prescriptions
```

### billing-service (Port 8085)
**Billing & Insurance Service**

Handles billing, payments, and insurance claims.

**Features:**
- Invoice generation
- Payment processing
- Insurance claims
- Payment plans
- Financial reports
- Insurance eligibility verification

**Endpoints:**
```
POST   /api/v1/billing/invoices                - Create invoice
GET    /api/v1/billing/invoices/{id}           - Get invoice
GET    /api/v1/billing/invoices                - List invoices
POST   /api/v1/billing/payments                - Process payment
GET    /api/v1/billing/payments/{id}           - Get payment
POST   /api/v1/billing/claims                  - Submit insurance claim
GET    /api/v1/billing/claims/{id}             - Get claim status
GET    /api/v1/billing/patient/{id}/balance    - Get patient balance
```

### notification-service (Port 8086)
**Notification Service**

Handles all platform notifications via multiple channels.

**Features:**
- Email notifications
- SMS notifications
- Push notifications
- Notification templates
- Delivery tracking
- User preferences

**Endpoints:**
```
POST   /api/v1/notifications              - Send notification
GET    /api/v1/notifications/{id}         - Get notification
GET    /api/v1/notifications              - List notifications
PUT    /api/v1/notifications/preferences  - Update preferences
GET    /api/v1/notifications/history      - Get notification history
```

### api-gateway (Port 8080)
**API Gateway Service**

Single entry point for all client requests with routing, security, and rate limiting.

**Features:**
- Request routing
- JWT validation
- Rate limiting
- Request/response logging
- Load balancing
- Response caching
- CORS handling

### eureka-server (Port 8761)
**Service Discovery Server**

Netflix Eureka server for service registration and discovery.

### config-server (Port 8888)
**Configuration Server**

Spring Cloud Config Server for centralized configuration management.

---

## 📖 API Documentation

### OpenAPI/Swagger

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Postman Collection

Import our Postman collection for quick testing:
```
docs/postman/Healthcare-Platform.postman_collection.json
```

### Example Requests

**Register a New User:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecureP@ss123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PATIENT"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecureP@ss123"
  }'
```

**Create an Appointment (Authenticated):**
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {your_jwt_token}" \
  -d '{
    "patientId": "123e4567-e89b-12d3-a456-426614174000",
    "providerId": "456e4567-e89b-12d3-a456-426614174000",
    "dateTime": "2024-02-15T10:00:00Z",
    "duration": 30,
    "type": "CONSULTATION",
    "notes": "Annual checkup"
  }'
```

---

## 🔐 Security

### Authentication Flow

```
┌────────┐        ┌─────────────┐        ┌─────────────┐        ┌─────────────┐
│ Client │───────►│ API Gateway │───────►│Auth Service │───────►│  Keycloak   │
└────────┘        └─────────────┘        └─────────────┘        └─────────────┘
     │                   │                      │                      │
     │   1. Login        │                      │                      │
     │──────────────────►│                      │                      │
     │                   │   2. Forward         │                      │
     │                   │─────────────────────►│                      │
     │                   │                      │  3. Validate         │
     │                   │                      │─────────────────────►│
     │                   │                      │  4. User Info        │
     │                   │                      │◄─────────────────────│
     │                   │   5. JWT Token       │                      │
     │                   │◄─────────────────────│                      │
     │   6. JWT Token    │                      │                      │
     │◄──────────────────│                      │                      │
```

### Security Features

| Feature | Implementation |
|---------|----------------|
| **Authentication** | JWT + OAuth2/OIDC |
| **Authorization** | Role-Based Access Control (RBAC) |
| **Encryption** | AES-256 for data at rest |
| **Transport Security** | TLS 1.3 |
| **Rate Limiting** | Redis-backed, configurable limits |
| **Input Validation** | Bean Validation (JSR-380) |
| **SQL Injection** | Parameterized queries via JPA |
| **XSS Prevention** | Content Security Policy headers |
| **CORS** | Configurable allowed origins |
| **Audit Logging** | All data access logged |

### HIPAA Compliance

- ✅ **Access Control** - Role-based permissions
- ✅ **Audit Controls** - Comprehensive logging
- ✅ **Integrity Controls** - Data validation
- ✅ **Transmission Security** - TLS encryption
- ✅ **Authentication** - Multi-factor support
- ✅ **Automatic Logoff** - Session timeout
- ✅ **Encryption** - PHI encrypted at rest

### Default Users

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@healthcare.com | Admin@123 |
| Doctor | doctor@healthcare.com | Doctor@123 |
| Nurse | nurse@healthcare.com | Nurse@123 |
| Patient | patient@healthcare.com | Patient@123 |

> ⚠️ **Warning:** Change default credentials in production!

---

## 🛡️ Resilience

### Circuit Breaker Pattern

All inter-service communication is protected by circuit breakers:

```java
@CircuitBreaker(name = "patientService", fallbackMethod = "getPatientFallback")
@Retry(name = "patientService", fallbackMethod = "getPatientFallback")
@RateLimiter(name = "patientService")
public Patient getPatient(String id) {
    // Service call
}

public Patient getPatientFallback(String id, Exception ex) {
    // Fallback logic
}
```

### Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      patientService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5000
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      patientService:
        max-attempts: 3
        wait-duration: 500ms
  ratelimiter:
    instances:
      patientService:
        limit-for-period: 100
        limit-refresh-period: 1s
```

---

## 🔧 DevOps

### Docker

**Build all services:**
```bash
docker-compose build
```

**Run with profiles:**
```bash
# Development
docker-compose --profile dev up

# Production
docker-compose --profile prod up
```

### Kubernetes

**Deploy to Kubernetes:**
```bash
# Create namespace
kubectl create namespace healthcare

# Apply configurations
kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/
kubectl apply -f k8s/ingress/

# Verify deployment
kubectl get pods -n healthcare
```

### CI/CD Pipeline

The GitHub Actions workflow automatically:
1. Runs tests on every push
2. Builds Docker images
3. Pushes to container registry
4. Deploys to Kubernetes (on main branch)

```yaml
# .github/workflows/ci-cd.yml
name: Build and Deploy
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
```

### Health Checks

All services expose health endpoints:
```
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
```

---

## 📊 Monitoring

### Metrics (Grafana)
- http://localhost:3000
- Default credentials: admin/admin

**Available Dashboards:**
- JVM Metrics
- Spring Boot Statistics
- API Gateway Performance
- Database Connections
- Message Queue Metrics

### Distributed Tracing (Zipkin)
- http://localhost:9411
- Trace requests across all services
- Identify performance bottlenecks

### Centralized Logging (Kibana)
- http://localhost:5601
- Search and filter logs
- Create custom visualizations
- Set up alerts

---

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify -P integration-tests
```

### Run All Tests with Coverage
```bash
mvn verify -P all-tests jacoco:report
```

### Test Coverage Report
After running tests, open:
```
target/site/jacoco/index.html
```

**Target Coverage: 80%+**

---

## 📁 Project Structure

```
healthcare-platform/
├── api-gateway/                 # API Gateway service
├── auth-service/                # Authentication service
│   ├── src/main/java/
│   │   └── com/ganainy/authservice/
│   │       ├── config/          # Configuration classes
│   │       ├── controller/      # REST controllers
│   │       ├── exception/       # Exception handlers
│   │       ├── mapper/          # Entity-DTO mappers
│   │       ├── model/
│   │       │   ├── dto/         # Data Transfer Objects
│   │       │   └── entity/      # JPA entities
│   │       ├── repository/      # Data repositories
│   │       ├── security/        # Security configuration
│   │       └── service/         # Business logic
│   │           └── impl/        # Service implementations
│   └── src/test/                # Tests
├── patient-service/             # Patient management service
├── appointment-service/         # Appointment scheduling service
├── prescription-service/        # Prescription management service
├── billing-service/             # Billing and payments service
├── notification-service/        # Notification service
├── eureka-server/               # Service discovery
├── config-server/               # Configuration server
├── docker-compose.yml           # Docker compose configuration
├── k8s/                         # Kubernetes manifests
│   ├── configmaps/
│   ├── secrets/
│   ├── deployments/
│   ├── services/
│   └── ingress/
├── docs/                        # Documentation
│   ├── architecture/           # Architecture diagrams
│   ├── api/                    # API documentation
│   └── postman/                # Postman collections
└── README.md
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards

- Follow Google Java Style Guide
- Write unit tests for all business logic
- Document public APIs with Javadoc
- Use meaningful commit messages

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Spring Cloud](https://spring.io/projects/spring-cloud) - Microservices patterns
- [Keycloak](https://www.keycloak.org/) - Identity and access management
- [Netflix OSS](https://netflix.github.io/) - Service discovery and resilience
- [RabbitMQ](https://www.rabbitmq.com/) - Message broker

---

<div align="center">

**Made with ❤️ for the healthcare industry**

[⬆ Back to Top](#-healthcare-management-platform)

</div>
