# syntax=docker/dockerfile:1

#
# STAGE 1: Build stage
#
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

#
# STAGE 2: Run stage
#
FROM eclipse-temurin:17-jre-alpine AS run

# Create non-privileged user
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Clean up unnecessary files
RUN rm -rf /var/cache/apk/*

# Switch to non-root user
USER appuser

# Expose internal port
EXPOSE 8081

# Environment variables with defaults
ENV SERVER_URL=http://server:8080/api
ENV SERVER_PORT=8081
ENV JAVA_OPTS="-Xms128m -Xmx256m"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]