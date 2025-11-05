# Multi-stage build for production
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first to leverage layer caching (no test deps)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy source code and build
COPY src ./src
# Run unit tests and build (skip integration tests and coverage check - they require Docker and Testcontainers)
# Integration tests and coverage checks should be run in CI/CD pipeline where Docker is available
RUN mvn -B clean package -DskipITs=true -Djacoco.skip=true

# Production stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Install curl for HEALTHCHECK
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/feature/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
