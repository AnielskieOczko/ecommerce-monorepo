# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an e-commerce platform built as a **Spring Boot monolith** using Kotlin. The architecture has been migrated from a microservices setup (payment-service, notification-service) into a single backend application for better performance and reduced complexity.

**Key Tech Stack:**
- **Backend**: Kotlin + Spring Boot 3.4.5, Java 21
- **Database**: MySQL 8 with Flyway migrations
- **Security**: Spring Security with JWT authentication
- **Payments**: Stripe integration (direct API calls)
- **Notifications**: Email via Thymeleaf templates, SMTP support (MailHog for dev)
- **Monitoring**: Prometheus metrics, Grafana dashboards, Spring Actuator
- **Build**: Maven with profile-specific configuration
- **CI/CD**: GitHub Actions with quality gates, OWASP dependency check, Trivy security scanning

## Architecture

### Backend Domain Modules

The backend (`backend/`) is organized into feature-based packages:

- **`user/`** - User management, authentication, role-based access control
- **`security/`** - JWT handling, security context, authentication services
- **`product/`** - Product catalog, categories, inventory, image management
- **`cart/`** - Shopping cart functionality
- **`order/`** - Order processing, status tracking, order history
- **`payment/`** - Stripe payment processing, webhooks, payment sessions
- **`notification/`** - Email notifications, template rendering (merged from notification-service)
- **`api/shared/`** - Shared DTOs, value objects, enums (absorbed from shared-api)
- **`config/`** - Application configuration, Jackson setup
- **`events/`** - Domain events (OrderStatusChanged, PaymentSucceeded)
- **`storage/`** - File storage for product images
- **`testdata/`** - Test data loader

### Key Architecture Patterns

1. **Layered Architecture**: Controllers → Services → Repositories → Domain
2. **DDD-inspired**: Domain entities, value objects, repositories
3. **Mapper Pattern**: DTO ↔ Entity mapping (see `*/mapper/` packages)
4. **Use Cases**: Some features use explicit use case classes (`usecases/` directories)
5. **Specification Pattern**: Search queries in `*/search/` packages

## Common Development Commands

### Build & Run

```bash
# Build the entire project
mvn clean install

# Build only backend
cd backend && mvn clean package

# Run backend with specific profile
./mvnw spring-boot:run -Dspring.profiles.active=local

# Run with Docker Compose (recommended for full stack)
cd infrastructure
docker-compose up -d

# Remote debugging (port 5005)
./mvnw spring-boot:run -Dspring.profiles.active=local -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

### Testing

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=ProductServiceImplTest

# Run tests matching pattern
mvn test -Dtest="*Controller*Test"

# Run with CI profile
mvn test -Dspring.profiles.active=ci
```

**Test Locations:**
- `backend/src/test/java/` - Java tests (currently empty - will be recreated)
- `backend/src/test/kotlin/` - Kotlin tests (currently empty - will be recreated)
- Uses JUnit 5, Mockito, H2 in-memory database
- All tests removed during migration - will be recreated from scratch

### Database Management

```bash
# Flyway migration (auto-runs on startup)
# Manual migration
mvn flyway:migrate -Dflyway.configFiles=src/main/resources/application-local.yml

# Clean and migrate (dev only!)
mvn flyway:clean flyway:migrate

# Check migration status
mvn flyway:info
```

**Database Migrations**: `backend/src/main/resources/db/migration/`
- Current version: V14__Sync_Entities_With_Schema.sql
- Follow Flyway naming: V{version}__{description}.sql

### Profile-Specific Configuration

The application uses Spring profiles for environment-specific settings:

- **local**: Local development with H2/MySQL on localhost, debug logging
- **dev**: Development environment
- **container**: Docker Compose environment with MySQL, MailHog, Prometheus
- **prod**: Production with external services (SendGrid, Twilio, CDN)
- **ci**: CI/CD environment with temporary test database

**Config Files:**
- `backend/src/main/resources/application.yml` - Base configuration
- `backend/src/main/resources/application-local.yml` - Local overrides
- `backend/src/main/resources/application-ci.yml` - CI overrides
- `backend/src/main/resources/application-prod.yml` - Production overrides
- `backend/src/main/resources/aplication-container.yml` - Container overrides (Docker Compose)

**Key Environment Variables:**
```bash
# Required for all environments
JWT_SECRET=your_jwt_secret
STORAGE_SECRET_SALT=your_storage_salt

# Database
MYSQL_USER=devuser
MYSQL_PASSWORD=devpass
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/ecommerce_dev

# Stripe
STRIPE_API_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Email (prod)
SENDGRID_API_KEY=SG...
SMTP_HOST=...
SMTP_USERNAME=...
SMTP_PASSWORD=...

# Storage (prod)
PROD_STORAGE_PATH=/var/data/ecommerce
CDN_BASE_URL=https://cdn.example.com
```

### API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Actuator Endpoints**: http://localhost:8080/actuator/health, /actuator/metrics, /actuator/prometheus

### Service Dependencies

When running locally, ensure these services are running:

```bash
# MySQL 8 (required)
# MailHog (optional, for email testing) - http://localhost:8025
```

**Or use Docker Compose from `infrastructure/` directory** which sets up:
- MySQL (port 3306)
- MailHog SMTP (port 1025, UI at 8025)
- Prometheus (9090)
- Grafana (3030)

**Note:** RabbitMQ has been removed - the monolith uses direct method calls and domain events

## Key Files & Locations

### Configuration
- `backend/pom.xml` - Maven configuration, dependencies
- `backend/src/main/resources/application.yml` - Base app config
- `backend/src/main/kotlin/com/rj/ecommerce_backend/config/` - Config classes

### Main Application
- `backend/src/main/kotlin/com/rj/ecommerce_backend/EcommerceBackendApplication.kt` - Main entry point

### Domain Examples
- **Order Flow**: `order/controller/OrderController.kt` → `order/service/OrderServiceImpl.kt` → `order/usecases/CreateOrderUseCase.kt`
- **Payment Flow**: `payment/controller/PaymentController.kt` → `payment/provider/StripePaymentProvider.kt`
- **User Auth**: `security/controller/AuthController.kt` → `security/service/AuthenticationServiceImpl.kt`

### Database
- Migrations: `backend/src/main/resources/db/migration/`
- Domain Entities: `backend/src/main/kotlin/com/rj/ecommerce_backend/*/domain/`

## Migration History

This project recently underwent a complete architecture migration from microservices to a Spring Boot monolith (visible in git history):

### Phase 1: Core Migration
1. **shared-api module** → Absorbed into backend (`api/shared/`)
2. **notification-service** → Merged into backend (`notification/`)
3. **payment-service** → Merged into backend with synchronous Stripe calls
4. **frontend React app** → Removed from monorepo

### Phase 2: Infrastructure Cleanup
5. **RabbitMQ messaging** → Removed (all async messaging eliminated)
6. **Legacy test suites** → Removed (will be recreated from scratch)
7. **Microservice CI/CD** → Consolidated to root-level workflows

### Phase 3: CI/CD Restructuring
8. **Workflow consolidation** → Single CI/CD at `.github/workflows/`
9. **Quality gates** → Added OWASP dependency check, Trivy scanning
10. **Java version** → Updated to Java 21 throughout

**Recent Commits (feature/monolith-migration branch):**
- `refactor(monorepo): Remove obsolete microservices modules from monorepo`
- `refactor(monorepo): Remove frontend React application module`
- `refactor(backend): Remove RabbitMQ messaging infrastructure`
- `feat(build): Update Maven dependencies for monolith architecture`
- `feat(config): Add profile-specific configuration files`
- `refactor(backend): Final code cleanup for monolith migration`
- `feat(ci-cd): Restructure CI/CD workflows for Spring Boot monolith`
- `fix(ci-cd): Fix workflow syntax and add best practices`

**Migration Status:** ✅ **COMPLETE** - All microservices successfully merged into monolith

## Important Implementation Details

### Payment Processing
- **Old**: Microservice with RabbitMQ messaging
- **Current**: Direct Stripe API calls in `payment/provider/StripePaymentProvider.kt`
- Webhooks handled in `payment/controller/WebhookController.kt`
- See recent commit `abe6aea refactor(payment): Convert payment flow to synchronous direct calls`

### Notification System
- **Old**: Separate notification-service
- **Current**: Merged into `notification/` package
- Email templates in `backend/src/main/resources/templates/`
- Uses Thymeleaf for templating
- Providers: SMTP, SendGrid, Twilio

### Image Storage
- Product images stored in `storage-local/` (local) or `/app/product-images` (container)
- Thumbnail generation using Thumbnailator library
- Image cleanup scheduled via cron: `app.storage.cleanup-schedule`

### Security
- JWT-based authentication
- Role-based access control (ADMIN, USER roles)
- Session management with blacklisted tokens
- Password encryption

### Data Flow Example: Order Creation
1. User adds items to cart (`cart/`)
2. User creates order (`order/controller/OrderController.kt`)
3. Validates stock, creates order entity (`order/usecases/CreateOrderUseCase.kt`)
4. Initiates Stripe payment (`payment/provider/StripePaymentProvider.kt`)
5. On success: publishes OrderStatusChangedEvent
6. Notification service sends confirmation email (`notification/service/NotificationServiceImpl.kt`)

### Monitoring & Observability
- **Prometheus metrics**: `/actuator/prometheus`
- **Health checks**: `/actuator/health`
- **Grafana dashboard**: http://localhost:3030 (admin/admin)
- Structured logging with Kotlin-logging
- UUID-based correlation IDs for tracing

## Testing Strategy

**Test Structure (to be implemented):**
- Controller tests: MockMvc with @WebMvcTest
- Service tests: @SpringBootTest with @MockBean
- Repository tests: @DataJpaTest
- Unit tests: Pure Kotlin/Java unit tests with Mockito

**Current Status:**
All test suites have been removed during the monolith migration and will be recreated from scratch to align with the new architecture.

**Recommended Test Structure (future):**
- Unit tests for domain entities and value objects
- Integration tests for controllers
- Service layer tests with mocked dependencies
- Repository tests with test containers (MySQL)
- End-to-end tests for critical user flows

## Development Tips

1. **Profile Selection**: Always specify `-Dspring.profiles.active=` when running
2. **Database**: Use Docker Compose MySQL or local MySQL on port 3306
3. **Email Testing**: Access MailHog at http://localhost:8025
4. **Debugging**: Connect debugger on port 5005
5. **API Testing**: Use Swagger UI at http://localhost:8080/swagger-ui.html
6. **Migration Safety**: Never run `flyway:clean` on production
7. **Environment Variables**: Copy from infrastructure/.env.example to infrastructure/.env

## Extending the Application

**Adding a New Domain:**
1. Create package: `backend/src/main/kotlin/com/rj/ecommerce_backend/{domain}/`
2. Add subpackages: `domain/`, `controller/`, `service/`, `repository/`, `mapper/`
3. Create entity in `domain/`
4. Add JPA repository
5. Create DTOs in `api/shared/dto/`
6. Implement service and controller
7. Add Flyway migration if needed
8. Write tests

**Adding a New Profile:**
1. Create `application-{profile}.yml` in `backend/src/main/resources/`
2. Add profile to `pom.xml` profiles section
3. Override necessary properties

## Service Ports (When Using Docker Compose)

- Backend: 8080
- MySQL: 3306
- MailHog: 8025 (UI), 1025 (SMTP)
- Prometheus: 9090
- Grafana: 3030
- Remote Debug: 5005

## CI/CD Configuration

### GitHub Actions Workflows

Located in `.github/workflows/`:

- **qodana_code_quality.yml** - Static code analysis (runs on PR and push)
- **backend-ci-cd.yml** - Complete CI/CD pipeline:
  - Quality gates with OWASP dependency check
  - Maven build with Java 21
  - MySQL integration tests
  - Docker image build & publish
  - Trivy security scanning (on releases)

### Required GitHub Secrets

```bash
QODANA_TOKEN              # For code quality scanning
DATABASE_PASSWORD=testpassword
MYSQL_ROOT_PASSWORD=root
```

### CI/CD Best Practices Applied

- ✅ Fail-fast error handling
- ✅ OWASP dependency vulnerability scanning
- ✅ Trivy container security scanning
- ✅ Artifact uploads (test results, JAR files)
- ✅ MySQL service integration for tests
- ✅ Quality gates before build
- ✅ Java 21 throughout pipeline
- ✅ Profile-based configuration (ci, local, prod, container)

### Branch Protection Rules

Configure on `main` branch:
- Require PR reviews (2 approvals)
- Require status checks: `qodana`, `quality-gate`, `build-and-test`
- Dismiss stale reviews
- Require branches to be up to date

## Known Considerations

1. **Migration Complete**: All microservices have been successfully merged into the backend monolith
2. **No Frontend**: React frontend was removed - requires separate frontend implementation
3. **No Async Messaging**: RabbitMQ removed - uses direct method calls and domain events
4. **API Versioning**: Currently uses `/api/v1/` paths
5. **File Storage**: Local filesystem storage (configure CDN for production)
6. **Test Coverage**: All tests removed - will be recreated from scratch for monolith architecture
7. **Java 21**: Ensure local development uses Java 21 (matches CI/CD)
8. **Profile Configuration**: Use correct Spring profile (`local`, `ci`, `prod`, `container`)
9. **Secrets Required**: Set up GitHub secrets for CI/CD (QODANA_TOKEN, DATABASE_PASSWORD, MYSQL_ROOT_PASSWORD)

## References

- README.md - Initial project documentation (outdated regarding microservices)
- Pom files - Source of truth for dependencies and profiles
- Migration files - Database schema history
- Recent git commits - Architecture changes
- `.github/workflows/` - CI/CD pipeline configuration
- Profile configuration files - Environment-specific settings

## Current Status

**Architecture:** ✅ **Spring Boot Monolith** (migrated from microservices)
**Codebase:** ✅ **Cleaned and Optimized** (500+ files removed, 50,000+ deletions)
**CI/CD:** ✅ **Production-Ready** (quality gates, security scanning, best practices)
**Tests:** ⚠️ **To Be Recreated** (all removed, will be built from scratch)
**Documentation:** ✅ **Updated** (this file reflects current state)

**Next Steps:**
1. Create comprehensive test suite
2. Implement new frontend (separate repository recommended)
3. Deploy to production environment
4. Configure monitoring and alerting
5. Set up CDN for production file storage

When in doubt, check the most recent commits to understand current architecture decisions.
