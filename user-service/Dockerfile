# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom and source
COPY pom.xml ../pom.xml
COPY user-service/pom.xml .
COPY user-service/src ./src
COPY user-service/src/main/proto ./src/main/proto

# Build the application
RUN mvn -pl :user-service -am clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder
COPY --from=builder /build/target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Expose ports (HTTP and gRPC)
EXPOSE 8081 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
