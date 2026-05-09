# telemetry-service/Dockerfile
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts /app/
RUN gradle build --no-daemon || return 0
COPY src /app/src
RUN gradle build --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/telemetry-service-1.0.0.jar /app/app.jar
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "/app/app.jar"]