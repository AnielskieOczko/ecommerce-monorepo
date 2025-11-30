# backend/Dockerfile

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

# Install Maven directly (avoids wrapper complexity)
RUN apk add --no-cache maven

WORKDIR /app

# Copy the entire monorepo context
COPY . .

# Build all modules
RUN mvn clean package -DskipTests

# Verify build output
RUN ls -l /app/backend/target

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for product images
RUN mkdir -p /app/product-images && chmod 777 /app/product-images

# Copy the JAR from build stage
COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Verify JAR is present
RUN ls -l /app

EXPOSE 8080
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/app.jar"]