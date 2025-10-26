# E-commerce Monorepo

A comprehensive e-commerce platform built with a microservices architecture. This monorepo contains all components of the platform, designed to work together seamlessly.

## Project Structure

- **/backend** - Spring Boot backend application implementing Domain-Driven Design (DDD)
- **/frontend** - React-based frontend application with Tailwind CSS
- **/payment-service** - Microservice for payment processing with Stripe integration
- **/email-service** - Microservice for email notifications using Thymeleaf templates
- **/ai-agent-service** - Kotlin-based AI agent microservice
- **/infrastructure** - Docker Compose and infrastructure configuration

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security with JWT
- Spring Data JPA
- MySQL 8
- Flyway for database migrations
- RabbitMQ for messaging

### Frontend
- React 18+
- TypeScript
- Tailwind CSS
- React Router
- Redux Toolkit
- Stripe integration

### Microservices
- Payment Service: Spring Boot with Stripe API
- Email Service: Spring Boot with Thymeleaf templates
- AI Agent Service: Kotlin with Spring Boot

### DevOps
- Docker & Docker Compose
- Prometheus & Grafana for monitoring
- MailHog for email testing

## Features

- **User Management**
  - Authentication & Authorization with JWT
  - Role-based access control
  - User profiles

- **Product Management**
  - Product catalog with categories
  - Image handling
  - Inventory management

- **Order Processing**
  - Shopping cart functionality
  - Order creation and management
  - Order status tracking

- **Payment Processing**
  - Stripe integration
  - Payment status tracking
  - Email notifications

- **Admin Dashboard**
  - User management
  - Product management
  - Order management
  - Category management

## Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 20.x
- Docker & Docker Compose
- MySQL 8+ (if running locally)

### Running with Docker Compose (Recommended)

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/ecommerce-monorepo.git
   cd ecommerce-monorepo
   ```

2. **Configure environment**

   Create `.env` file in the infrastructure directory:

   ```bash
   cd infrastructure
   cp .env_.example .env_  # Then edit .env_ with your settings
   ```

   Key environment variables to configure:

   ```properties
   # Application
   SPRING_ACTIVE_PROFILE=dev

   # Database
   DB_NAME=ecommerce_dev
   DB_USERNAME=devuser
   DB_PASSWORD=devpass
   MYSQL_ROOT_PASSWORD=rootpass
   SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/ecommerce_dev

   # JWT Configuration
   JWT_SECRET=your_secure_jwt_secret
   JWT_EXPIRATION=900000

   # RabbitMQ
   RABBITMQ_DEFAULT_USER=guest
   RABBITMQ_DEFAULT_PASS=guest

   # Stripe (for payment service)
   STRIPE_SECRET_KEY=your_stripe_secret_key
   STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
   ```

3. **Start the application**

   ```bash
   docker-compose --profile container up -d
   ```

   The services will be available at:

   - Backend: `http://localhost:8080`
   - Frontend: `http://localhost:3000`
   - Payment Service: `http://localhost:8010`
   - Email Service: `http://localhost:8081`
   - AI Agent Service: `http://localhost:8020`
   - RabbitMQ Management: `http://localhost:15672`
   - MailHog (Email Testing): `http://localhost:8025`
   - Prometheus: `http://localhost:9090`
   - Grafana: `http://localhost:3030`

### Running Services Individually

#### Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring.profiles.active=local
```

#### Frontend

```bash
cd frontend
npm install
npm start
```

## Development

### Backend Development

- API documentation is available at `http://localhost:8080/swagger-ui.html`
- Remote debugging is enabled on port 5005

### Frontend Development

- The React application uses Tailwind CSS for styling
- Component structure follows a feature-based organization

## Monitoring

- Prometheus metrics are available at `http://localhost:9090`
- Grafana dashboards are available at `http://localhost:3030` (admin/admin)

## Testing

- Backend uses JUnit and Mockito for testing
- Frontend uses Jest and React Testing Library
- MailHog provides a UI for testing emails at `http://localhost:8025`

## Architecture

### Microservices Communication

The application uses a microservices architecture with the following communication patterns:

- **Synchronous Communication**: REST APIs between services and frontend
- **Asynchronous Communication**: RabbitMQ for event-driven communication between services

### Data Flow

1. User interacts with the React frontend
2. Frontend communicates with the backend via REST APIs
3. Backend processes business logic and persists data in MySQL
4. For payment processing, backend communicates with the payment service
5. After successful payment, events are published to RabbitMQ
6. Email service consumes events and sends notifications
7. AI agent service provides recommendations and insights

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

Rafał Jankowski - [@OczkoAnielskie](https://x.com/OczkoAnielskie) - rafaljankowski7@gmail.com

Project Link: [https://github.com/yourusername/ecommerce-monorepo](https://github.com/yourusername/ecommerce-monorepo)