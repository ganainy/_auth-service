# Spring Boot Microservices Learning Roadmap

## Project 1: E-Commerce Microservices Platform
## Project 3: Healthcare Management System

---

# 🎯 Phase 1: Foundation (Weeks 1-2)
**Goal**: Build a monolithic version with core security

## Week 1: Setup & Basic Security

### Tasks
- [x] Initialize Spring Boot projects (start.spring.io)
- [x] Setup PostgreSQL/MySQL databases
- [x] Create basic entity models (User, Product/Patient, Order/Appointment)
- [x] Implement basic CRUD operations

### Security Implementation
- [x] **Spring Security basic setup**
  - Configure SecurityFilterChain
  - Password encoding with BCryptPasswordEncoder
  - In-memory user details for testing
  
- [x] **Input Validation**
  - Add @Valid and @Validated annotations
  - Create custom validators
  - Bean Validation (JSR-380) with constraints

- [x] **SQL Injection Prevention**
  - Use Spring Data JPA (automatic parameterization)
  - Never concatenate SQL strings
  - Use @Query with named parameters

### API Design
- [x] **RESTful Endpoints**
  ```
  POST   /api/users          - Create
  GET    /api/users/{id}     - Read
  GET    /api/users          - List
  PUT    /api/users/{id}     - Update
  DELETE /api/users/{id}     - Delete
  ```

- [x] **Request/Response DTOs**
  - Create separate DTOs for requests and responses
  - Use MapStruct for entity-DTO mapping
  - Add validation annotations (@NotNull, @Email, @Size)

- [x] **Exception Handling**
  - Create @ControllerAdvice class
  - Handle common exceptions (ResourceNotFoundException, ValidationException)
  - Return consistent error format

### Code Quality
- [x] **Project Structure**
  ```
  src/main/java/com/project/
  ├── controller/
  ├── service/
  ├── repository/
  ├── model/entity/
  ├── model/dto/
  ├── mapper/
  ├── exception/
  └── config/
  ```

- [x] **Lombok Setup**
  - Add @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
  - Use @Slf4j for logging

**Deliverable**: Working monolithic app with basic CRUD + authentication

---

## Week 2: JWT Authentication & Advanced API Design

### Security Implementation
- [x] **JWT Token Authentication**
  - Create JWT utility class (generate, validate, extract claims)
  - Implement JwtAuthenticationFilter
  - Add login endpoint returning JWT
  - Secure endpoints with @PreAuthorize

- [ ] **CORS Configuration**
  ```java
  @Configuration
  public class CorsConfig {
      @Bean
      public CorsFilter corsFilter() {
          // Configure allowed origins, methods, headers
      }
  }
  ```

- [ ] **XSS Protection**
  - Implement input sanitization filter
  - Use OWASP Java HTML Sanitizer
  - Set security headers (X-XSS-Protection, Content-Security-Policy)

### API Design
- [ ] **HTTP Status Codes**
  - 200 OK, 201 Created, 204 No Content
  - 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
  - 500 Internal Server Error

- [ ] **RFC 7807 Problem Details**
  ```java
  {
    "type": "https://api.example.com/errors/validation",
    "title": "Validation Failed",
    "status": 400,
    "detail": "Email is required",
    "instance": "/api/users",
    "timestamp": "2024-01-15T10:30:00Z"
  }
  ```

- [ ] **Pagination & Filtering**
  - Use Pageable in controller methods
  - Return Page<DTO> with metadata
  - Implement @FilterBy and @SortBy custom annotations

- [ ] **OpenAPI/Swagger Documentation**
  - Add springdoc-openapi dependency
  - Configure Swagger UI
  - Add @Operation, @ApiResponse annotations
  - Document security requirements

### Code Quality
- [ ] **Unit Tests**
  - Service layer tests with Mockito
  - Controller tests with MockMvc
  - Aim for 70%+ code coverage

- [ ] **Actuator Setup**
  - Enable health, info, metrics endpoints
  - Secure actuator endpoints
  - Add custom health indicators

**Deliverable**: Secure monolithic app with JWT, comprehensive API docs

---

# 🏗️ Phase 2: Microservices Decomposition (Weeks 3-4)

## Week 3: Service Extraction & Discovery

### Microservices Architecture
- [ ] **Extract Services**
  
  **E-Commerce:**
  - auth-service (port 8081)
  - product-service (port 8082)
  - order-service (port 8083)
  
  **Healthcare:**
  - auth-service (port 8081)
  - patient-service (port 8082)
  - appointment-service (port 8083)

- [ ] **Service Discovery with Eureka**
  - Create eureka-server (port 8761)
  - Add @EnableEurekaServer
  - Configure each microservice as Eureka client
  - Add @EnableDiscoveryClient to services

- [ ] **Database Per Service**
  - Separate database for each service
  - Configure multiple DataSource beans
  - Use schema-based or database-based isolation

### API Design
- [ ] **API Versioning**
  ```
  /api/v1/products
  /api/v2/products  (or use Accept: application/vnd.api.v2+json)
  ```

- [ ] **Service-to-Service Communication**
  - Implement OpenFeign clients
  - Add @FeignClient annotations
  - Handle Feign exceptions

### Security Implementation
- [ ] **API Key for Service-to-Service**
  - Generate API keys for each service
  - Create ApiKeyAuthFilter
  - Validate API keys in gateway/inter-service calls

- [ ] **Centralized Authentication**
  - Auth service issues JWT tokens
  - Other services validate JWT (shared secret or public key)
  - Pass JWT in Authorization header between services

### Code Quality
- [ ] **Clean Architecture**
  ```
  service/
  ├── domain/         (entities, business logic)
  ├── application/    (use cases, DTOs)
  ├── infrastructure/ (repositories, external APIs)
  └── presentation/   (controllers, mappers)
  ```

**Deliverable**: 3+ microservices with service discovery

---

## Week 4: API Gateway & Configuration Management

### Microservices Architecture
- [ ] **Spring Cloud Gateway**
  - Create api-gateway service (port 8080)
  - Configure routes to all services
  - Implement request routing by path

  ```yaml
  spring:
    cloud:
      gateway:
        routes:
          - id: product-service
            uri: lb://PRODUCT-SERVICE
            predicates:
              - Path=/api/products/**
  ```

- [ ] **Gateway-Level Security**
  - JWT validation in gateway
  - Rate limiting with Redis
  - Request/response logging filter

- [ ] **Spring Cloud Config Server**
  - Create config-server (port 8888)
  - Store configs in Git repository
  - Configure services to fetch config on startup
  - Implement @RefreshScope for dynamic updates

### Security Implementation
- [ ] **Rate Limiting**
  ```java
  @Bean
  public RedisRateLimiter redisRateLimiter() {
      return new RedisRateLimiter(10, 20); // 10 requests per second
  }
  ```

- [ ] **Request Throttling**
  - Implement bucket4j or Resilience4j rate limiter
  - Different limits for authenticated vs anonymous users

### API Design
- [ ] **Gateway Response Transformation**
  - Add correlation IDs to all requests
  - Standardize error responses across services
  - Add response time headers

### Code Quality
- [ ] **Integration Tests with TestContainers**
  - Test with real PostgreSQL container
  - Test with real Redis container
  - Test service-to-service communication

**Deliverable**: API Gateway routing all requests, centralized configuration

---

# 🔒 Phase 3: Advanced Security (Week 5)

## Week 5: OAuth2/OIDC & Advanced Authentication

### Security Implementation
- [ ] **Keycloak Setup**
  - Run Keycloak in Docker
  - Create realm, clients, roles
  - Configure redirect URIs

- [ ] **OAuth2 Resource Server**
  - Configure services as OAuth2 resource servers
  - Validate JWT tokens from Keycloak
  - Extract roles and permissions from token

- [ ] **OAuth2 Client**
  - Implement OAuth2 login flow
  - Store tokens securely
  - Implement token refresh

- [ ] **Role-Based Access Control (RBAC)**
  ```java
  @PreAuthorize("hasRole('ADMIN')")
  @PreAuthorize("hasAuthority('SCOPE_read:products')")
  @PreAuthorize("@securityService.canAccessResource(#id)")
  ```

- [ ] **Method-Level Security**
  - Enable @EnableGlobalMethodSecurity
  - Use @Secured, @PreAuthorize, @PostAuthorize
  - Create custom security expressions

### Healthcare-Specific Security
- [ ] **Data Encryption**
  - Encrypt sensitive fields (SSN, medical records) at rest
  - Use JPA AttributeConverter for transparent encryption
  - Implement field-level encryption with AES-256

- [ ] **Audit Logging**
  - Log all data access (who, what, when)
  - Use Spring Data JPA Auditing (@CreatedBy, @LastModifiedBy)
  - Store audit logs in separate database

- [ ] **HIPAA Compliance Features**
  - Implement automatic session timeout
  - Add "break the glass" emergency access
  - Create audit reports

**Deliverable**: Full OAuth2/OIDC integration with advanced security

---

# 🚀 Phase 4: Resilience & Observability (Week 6)

## Week 6: Fault Tolerance & Monitoring

### Microservices Architecture
- [ ] **Circuit Breaker with Resilience4j**
  ```java
  @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProduct")
  @Retry(name = "productService")
  @RateLimiter(name = "productService")
  public Product getProduct(Long id) { }
  ```

- [ ] **Load Balancing**
  - Configure Spring Cloud LoadBalancer
  - Implement custom load balancing strategies
  - Test failover scenarios

- [ ] **Distributed Tracing**
  - Add Spring Cloud Sleuth
  - Setup Zipkin server (Docker)
  - Configure trace sampling
  - Trace requests across services

- [ ] **Centralized Logging (ELK Stack)**
  - Run Elasticsearch, Logstash, Kibana in Docker
  - Configure Logstash to collect logs
  - Add correlation IDs to logs
  - Create Kibana dashboards

### API Design
- [ ] **HATEOAS Implementation**
  ```json
  {
    "id": 1,
    "name": "Product",
    "_links": {
      "self": {"href": "/api/products/1"},
      "orders": {"href": "/api/products/1/orders"},
      "reviews": {"href": "/api/products/1/reviews"}
    }
  }
  ```

- [ ] **GraphQL Alternative**
  - Add GraphQL Spring Boot Starter
  - Create GraphQL schemas
  - Implement resolvers
  - Compare REST vs GraphQL performance

### Code Quality
- [ ] **DDD Principles**
  - Define bounded contexts
  - Create aggregates and value objects
  - Implement domain events
  - Use repositories for aggregate roots only

- [ ] **SOLID Principles Review**
  - Single Responsibility: One class, one purpose
  - Open/Closed: Open for extension, closed for modification
  - Liskov Substitution: Subtypes must be substitutable
  - Interface Segregation: Many specific interfaces
  - Dependency Inversion: Depend on abstractions

**Deliverable**: Resilient services with comprehensive monitoring

---

# 📬 Phase 5: Async Communication (Week 7)

## Week 7: Event-Driven Architecture

### Microservices Architecture
- [ ] **RabbitMQ Setup**
  - Run RabbitMQ in Docker
  - Create exchanges, queues, bindings
  - Configure dead letter queues

- [ ] **Event Publishing**
  ```java
  @Service
  public class OrderService {
      private final RabbitTemplate rabbitTemplate;
      
      public void createOrder(Order order) {
          // Save order
          rabbitTemplate.convertAndSend("order.exchange", 
              "order.created", 
              new OrderCreatedEvent(order));
      }
  }
  ```

- [ ] **Event Consumers**
  - Create @RabbitListener methods
  - Implement idempotency (deduplication)
  - Handle message failures and retries

- [ ] **Kafka Alternative** (Optional)
  - Setup Kafka broker
  - Implement producers and consumers
  - Use Kafka Streams for processing

### E-Commerce Events
- OrderCreated → Inventory Service (reserve stock)
- OrderCreated → Notification Service (email confirmation)
- PaymentProcessed → Order Service (update status)

### Healthcare Events
- AppointmentScheduled → Notification Service (SMS reminder)
- PrescriptionCreated → Pharmacy Service (notify pharmacy)
- LabResultAvailable → Patient Service (notify patient)

### Code Quality
- [ ] **Event Sourcing Pattern** (Advanced)
  - Store events instead of current state
  - Rebuild state from events
  - Implement event store

**Deliverable**: Async event-driven communication between services

---

# 🐳 Phase 6: Containerization & DevOps (Weeks 8-9)

## Week 8: Docker & Docker Compose

### DevOps
- [ ] **Dockerize Each Service**
  ```dockerfile
  FROM eclipse-temurin:17-jdk-alpine
  WORKDIR /app
  COPY target/*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

- [ ] **Multi-Stage Builds**
  - Build stage with Maven
  - Runtime stage with JRE only
  - Minimize image size

- [ ] **Docker Compose**
  ```yaml
  services:
    postgres:
      image: postgres:15
    redis:
      image: redis:7
    eureka-server:
      build: ./eureka-server
    api-gateway:
      build: ./api-gateway
      depends_on:
        - eureka-server
  ```

- [ ] **Health Checks**
  ```yaml
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
  ```

- [ ] **Environment Variables**
  - Externalize all configuration
  - Use .env files for local development
  - Never hardcode credentials

### Code Quality
- [ ] **Build Optimization**
  - Use Maven wrapper
  - Configure multi-module Maven project
  - Implement dependency caching in Docker

**Deliverable**: All services running in Docker Compose

---

## Week 9: Kubernetes & CI/CD

### DevOps
- [ ] **Kubernetes Manifests**
  - Create Deployments for each service
  - Create Services (ClusterIP, LoadBalancer)
  - Create ConfigMaps and Secrets
  - Create Ingress for external access

- [ ] **K8s Deployment Example**
  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: product-service
  spec:
    replicas: 3
    template:
      spec:
        containers:
        - name: product-service
          image: product-service:latest
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
  ```

- [ ] **CI/CD Pipeline (GitHub Actions)**
  ```yaml
  name: Build and Deploy
  on: [push]
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - name: Set up JDK 17
          uses: actions/setup-java@v3
        - name: Build with Maven
          run: mvn clean package
        - name: Run Tests
          run: mvn test
        - name: Build Docker Image
          run: docker build -t myapp:${{ github.sha }} .
        - name: Push to Registry
          run: docker push myapp:${{ github.sha }}
        - name: Deploy to K8s
          run: kubectl apply -f k8s/
  ```

- [ ] **Monitoring & Alerts**
  - Deploy Prometheus for metrics
  - Deploy Grafana for visualization
  - Create alerting rules
  - Monitor service health, latency, error rates

**Deliverable**: Production-ready K8s deployment with CI/CD

---

# 📊 Phase 7: Advanced Features & Polish (Week 10)

## Week 10: Final Integration & Testing

### Additional Microservices
**E-Commerce:**
- [ ] Payment Service (Stripe/PayPal sandbox)
- [ ] Notification Service (Email via SendGrid)
- [ ] Review Service (product reviews)

**Healthcare:**
- [ ] Billing Service (insurance claims)
- [ ] Prescription Service (medication tracking)
- [ ] Lab Results Service

### Advanced Features
- [ ] **API Gateway Enhancements**
  - Request/response caching
  - Response compression
  - API versioning strategies
  - Custom filters for analytics

- [ ] **Advanced Monitoring**
  - Custom Micrometer metrics
  - Business metrics (orders/min, active users)
  - Performance profiling
  - Cost tracking per service

### Testing
- [ ] **End-to-End Tests**
  - Test complete user workflows
  - Use Selenium/Playwright for UI tests
  - Test failure scenarios

- [ ] **Performance Testing**
  - Use JMeter or Gatling
  - Load testing (1000+ concurrent users)
  - Stress testing to find breaking points
  - Identify bottlenecks

- [ ] **Security Testing**
  - OWASP ZAP scan
  - SQL injection testing
  - XSS testing
  - Penetration testing checklist

### Documentation
- [ ] **Architecture Diagrams**
  - C4 model diagrams
  - Sequence diagrams for key flows
  - ER diagrams for each service

- [ ] **README Files**
  - Setup instructions
  - API documentation
  - Architecture decisions (ADRs)
  - Troubleshooting guide

**Deliverable**: Production-ready, fully tested microservices system

---

# 🎓 Learning Resources

## Must-Read Documentation
- Spring Boot Official Docs
- Spring Security Reference
- Spring Cloud Documentation
- Resilience4j User Guide
- OAuth 2.0 and OIDC specs

## Recommended Books
- "Microservices Patterns" by Chris Richardson
- "Spring Microservices in Action" by John Carnell
- "Building Microservices" by Sam Newman
- "Domain-Driven Design" by Eric Evans

## Practice Platforms
- Baeldung.com tutorials
- Spring Academy courses
- GitHub repositories with examples

---

# ✅ Completion Checklist

## Security ✓
- [x] Spring Security with JWT
- [x] OAuth2/OIDC with Keycloak
- [x] API key authentication
- [x] Rate limiting
- [x] Input validation
- [x] CORS configuration
- [x] SQL injection prevention
- [x] XSS protection

## API Design ✓
- [x] RESTful principles
- [x] API versioning
- [x] HATEOAS
- [x] RFC 7807 error responses
- [x] Pagination & filtering
- [x] DTOs with validation
- [x] OpenAPI/Swagger docs
- [x] GraphQL endpoints

## Microservices ✓
- [x] Service Discovery (Eureka)
- [x] API Gateway
- [x] Config Server
- [x] Load Balancing
- [x] Circuit Breaker
- [x] Distributed Tracing
- [x] Centralized Logging
- [x] Message Brokers
- [x] Database per Service

## Code Quality ✓
- [x] Clean Architecture
- [x] DDD principles
- [x] SOLID principles
- [x] Unit tests
- [x] Integration tests
- [x] Exception handling
- [x] Lombok
- [x] MapStruct
- [x] Actuator monitoring

## DevOps ✓
- [x] Docker containerization
- [x] Docker Compose
- [x] Kubernetes manifests
- [x] CI/CD pipeline
- [x] Health checks

---

# 💼 Resume Highlights

When complete, you can showcase:
- ✅ Production-ready microservices architecture
- ✅ Enterprise security implementation (OAuth2, JWT, RBAC)
- ✅ Cloud-native patterns (service mesh, distributed tracing)
- ✅ Event-driven architecture with message brokers
- ✅ DevOps automation (Docker, K8s, CI/CD)
- ✅ 80%+ test coverage
- ✅ Comprehensive documentation

**GitHub Profile**: Make your repositories public with excellent README files!

---

# 📅 Time Commitment
- **Total Duration**: 10 weeks
- **Hours per Week**: 15-20 hours
- **Total Hours**: 150-200 hours

**Pro Tip**: Commit code daily, write good commit messages, and maintain a development journal documenting challenges and solutions!