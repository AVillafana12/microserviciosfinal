# Multi-stage build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy root pom
COPY pom.xml .

# Copy all modules  
COPY eureka_server/pom.xml ./eureka_server/
COPY gateway/pom.xml ./gateway/
COPY user-service/pom.xml ./user-service/
COPY user-service/src ./user-service/src

COPY appointment-service/pom.xml ./appointment-service/
COPY appointment-service/src ./appointment-service/src

# also copy image-service pom and sources because parent pom declares it as a module
COPY image-service/pom.xml ./image-service/
COPY image-service/src ./image-service/src

# Build appointment-service with dependencies
RUN mvn clean package -DskipTests -am -pl appointment-service

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy built JAR from builder stage
COPY --from=builder /build/appointment-service/target/appointment-service-0.0.1-SNAPSHOT.jar appointment-service.jar

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:9091/actuator/health || exit 1

# Expose ports
EXPOSE 9091 9092

# Run the application
ENTRYPOINT ["java", \
    "-Xms256m", "-Xmx512m", \
    "-Dspring.profiles.active=docker", \
    "-jar", "appointment-service.jar"]
