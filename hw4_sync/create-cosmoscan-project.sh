#!/bin/bash

# ============================================================
# Скрипт для создания структуры проекта КосмоСкан
# Версия: 1.0
# ============================================================

set -e  # Остановка при ошибке

echo "=========================================="
echo " Создание проекта КосмоСкан"
echo "=========================================="

# Корневая директория проекта
PROJECT_ROOT="cosmoscan"
echo "Создание корневой директории: $PROJECT_ROOT"
mkdir -p $PROJECT_ROOT
cd $PROJECT_ROOT

# ============================================================
# Функции для создания файлов
# ============================================================

create_file() {
    local file_path="$1"
    local content="$2"
    mkdir -p "$(dirname "$file_path")"
    echo "$content" > "$file_path"
    echo "  ✓ Создан: $file_path"
}

# ============================================================
# 1. API GATEWAY
# ============================================================
echo ""
echo "----------------------------------------"
echo " 1. Создание API Gateway"
echo "----------------------------------------"

# pom.xml
create_file "api-gateway/pom.xml" '<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.cosmoscan</groupId>
    <artifactId>api-gateway</artifactId>
    <version>1.0.0</version>
    <name>api-gateway</name>
    <description>API Gateway for CosmoScan</description>
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>'

# application.yml
create_file "api-gateway/src/main/resources/application.yml" 'server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: file-storing-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/works/**
          filters:
            - name: CircuitBreaker
              args:
                name: fileStoringServiceCB
                fallbackUri: forward:/fallback/file-storing
        - id: file-analysis-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/reports/**
          filters:
            - name: CircuitBreaker
              args:
                name: fileAnalysisServiceCB
                fallbackUri: forward:/fallback/file-analysis
        - id: file-analysis-service-internal
          uri: http://localhost:8082
          predicates:
            - Path=/api/internal/**
          filters:
            - name: CircuitBreaker
              args:
                name: fileAnalysisInternalCB
                fallbackUri: forward:/fallback/file-analysis
      globalcors:
        cors-configurations:
          "[/**]":
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders:
              - "*"
  data:
    redis:
      host: localhost
      port: 6379

resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
    instances:
      fileStoringServiceCB:
        baseConfig: default
      fileAnalysisServiceCB:
        baseConfig: default
      fileAnalysisInternalCB:
        baseConfig: default

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway,circuitbreakers

logging:
  level:
    com.cosmoscan: DEBUG
    org.springframework.cloud.gateway: INFO'

# Основной класс
create_file "api-gateway/src/main/java/com/cosmoscan/apigateway/ApiGatewayApplication.java" 'package com.cosmoscan.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}'

# Конфигурация маршрутов
create_file "api-gateway/src/main/java/com/cosmoscan/apigateway/config/GatewayConfig.java" 'package com.cosmoscan.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("file-storing-service-route", r -> r
                        .path("/api/works/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("fileStoringServiceCB")
                                        .setFallbackUri("forward:/fallback/file-storing")))
                        .uri("http://localhost:8081"))
                .route("file-analysis-service-route", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("fileAnalysisServiceCB")
                                        .setFallbackUri("forward:/fallback/file-analysis")))
                        .uri("http://localhost:8082"))
                .route("file-analysis-internal-block", r -> r
                        .path("/api/internal/**")
                        .filters(f -> f
                                .setResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN))
                        .uri("no://op"))
                .build();
    }
}'

# Логирующий фильтр
create_file "api-gateway/src/main/java/com/cosmoscan/apigateway/filter/LoggingFilter.java" 'package com.cosmoscan.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("[REQUEST] {} {}", exchange.getRequest().getMethod(), 
                 exchange.getRequest().getURI().getPath());
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> log.info("[RESPONSE] Status: {}", 
                         exchange.getResponse().getStatusCode())));
    }
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}'

# Fallback контроллер
create_file "api-gateway/src/main/java/com/cosmoscan/apigateway/controller/FallbackController.java" 'package com.cosmoscan.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {
    @RequestMapping("/fallback/file-storing")
    public ResponseEntity<Map<String, Object>> fileStoringFallback() {
        log.warn("Circuit Breaker: File Storing Service");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "File Storing Service unavailable",
                             "timestamp", LocalDateTime.now().toString()));
    }
    @RequestMapping("/fallback/file-analysis")
    public ResponseEntity<Map<String, Object>> fileAnalysisFallback() {
        log.warn("Circuit Breaker: File Analysis Service");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "File Analysis Service unavailable",
                             "timestamp", LocalDateTime.now().toString()));
    }
    @RequestMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "api-gateway"));
    }
}'

# Dockerfile
create_file "api-gateway/Dockerfile" 'FROM openjdk:17-alpine
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]'

echo "  ✓ API Gateway создан"

# ============================================================
# 2. FILE STORING SERVICE
# ============================================================
echo ""
echo "----------------------------------------"
echo " 2. Создание File Storing Service"
echo "----------------------------------------"

# pom.xml
create_file "file-storing-service/pom.xml" '<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.cosmoscan</groupId>
    <artifactId>file-storing-service</artifactId>
    <version>1.0.0</version>
    <name>file-storing-service</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>'

# application.yml
create_file "file-storing-service/src/main/resources/application.yml" 'server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/file_storing_db
    username: storing_user
    password: storing_pass
  jpa:
    hibernate:
      ddl-auto: update
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

storage:
  upload-dir: ./uploads

analysis:
  service:
    url: http://localhost:8082/api/internal/analyze

logging:
  level:
    com.cosmoscan: DEBUG'

# Основной класс
create_file "file-storing-service/src/main/java/com/cosmoscan/filestoring/FileStoringServiceApplication.java" 'package com.cosmoscan.filestoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileStoringServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileStoringServiceApplication.class, args);
    }
}'

# Entity
create_file "file-storing-service/src/main/java/com/cosmoscan/filestoring/entity/WorkSubmission.java" 'package com.cosmoscan.filestoring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "works")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSubmission {
    @Id
    private UUID id;
    private String studentName;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime submissionTime;
    private String analysisStatus;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (submissionTime == null) submissionTime = LocalDateTime.now();
        if (analysisStatus == null) analysisStatus = "PENDING";
    }
}'

# Repository
create_file "file-storing-service/src/main/java/com/cosmoscan/filestoring/repository/WorkRepository.java" 'package com.cosmoscan.filestoring.repository;

import com.cosmoscan.filestoring.entity.WorkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WorkRepository extends JpaRepository<WorkSubmission, UUID> {
}'

# DTO
create_file "file-storing-service/src/main/java/com/cosmoscan/filestoring/dto/AnalysisRequest.java" 'package com.cosmoscan.filestoring.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    private UUID workId;
    private String filePath;
    private String fileName;
    private Long fileSize;
}'

# Service
create_file "file-storing-service/src/main/java/com/cosmoscan/filestoring/service/WorkService.java" 'package com.cosmoscan.filestoring.service;

import com.cosmoscan.filestoring.dto.AnalysisRequest;
import com.cosmoscan.filestoring.entity.WorkSubmission;
import com.cosmoscan.filestoring.repository.WorkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class WorkService {
    private final WorkRepository workRepository;
    private final RestTemplate restTemplate;
    private final String uploadDir;
    private final String analysisServiceUrl;

    public WorkService(WorkRepository workRepository,
                       @Value("${storage.upload-dir}") String uploadDir,
                       @Value("${analysis.service.url}") String analysisServiceUrl) {
        this.workRepository = workRepository;
        this.uploadDir = uploadDir;
        this.analysisServiceUrl = analysisServiceUrl;
        this.restTemplate = new RestTemplate();
    }

    public WorkSubmission storeWork(String studentName, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String fileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        WorkSubmission work = WorkSubmission.builder()
                .id(UUID.randomUUID())
                .studentName(studentName)
                .fileName(fileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .submissionTime(LocalDateTime.now())
                .analysisStatus("PENDING")
                .build();
        workRepository.save(work);

        try {
            AnalysisRequest request = AnalysisRequest.builder()
                    .workId(work.getId())
                    .filePath(work.getFilePath())
                    .fileName(work.getFileName())
                    .fileSize(work.getFileSize())
                    .build();
            restTemplate.postForEntity(analysisServiceUrl, request, Void.class);
            work.setAnalysisStatus("COMPLETED");
        } catch (Exception e) {
            log.error("Analysis service unavailable: {}", e.getMessage());
            work.setAnalysisStatus("FAILED");
        }
        workRepository.save(work);
        return work;
    }

    public Optional<Resource> getFile(UUID workId) {
        return workRepository.findById(workId).map(work -> {
            try {
                return new UrlResource(Paths.get(work.getFilePath()).toUri());
            } catch (Exception e) {
                return null;
            }
        });
    }
}'

# Controller
create_file "file-storing-service/src/main/java/com/cosmoscan/filestoring/controller/WorkController.java" 'package com.cosmoscan.filestoring.controller;

import com.cosmoscan.filestoring.entity.WorkSubmission;
import com.cosmoscan.filestoring.service.WorkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/works")
@Slf4j
@CrossOrigin(origins = "*")
public class WorkController {
    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitWork(@RequestParam("studentName") String studentName,
                                        @RequestParam("file") MultipartFile file) {
        try {
            WorkSubmission saved = workService.storeWork(studentName, file);
            String message = "FAILED".equals(saved.getAnalysisStatus()) ?
                    "Работа сохранена, но проверка временно недоступна" :
                    "Работа успешно загружена и отправлена на проверку";
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", message, "workId", saved.getId().toString(),
                                 "analysisStatus", saved.getAnalysisStatus()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{workId}/file")
    public ResponseEntity<?> getFile(@PathVariable UUID workId) {
        return workService.getFile(workId)
                .map(resource -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource))
                .orElse(ResponseEntity.notFound().build());
    }
}'

# Dockerfile
create_file "file-storing-service/Dockerfile" 'FROM openjdk:17-alpine
WORKDIR /app
RUN mkdir -p /app/uploads
VOLUME /app/uploads
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]'

echo "  ✓ File Storing Service создан"

# ============================================================
# 3. FILE ANALYSIS SERVICE
# ============================================================
echo ""
echo "----------------------------------------"
echo " 3. Создание File Analysis Service"
echo "----------------------------------------"

# pom.xml
create_file "file-analysis-service/pom.xml" '<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.cosmoscan</groupId>
    <artifactId>file-analysis-service</artifactId>
    <version>1.0.0</version>
    <name>file-analysis-service</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-core</artifactId>
            <version>2.9.1</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>'

# application.yml
create_file "file-analysis-service/src/main/resources/application.yml" 'server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/file_analysis_db
    username: analysis_user
    password: analysis_pass
  jpa:
    hibernate:
      ddl-auto: update

analysis:
  allowed-formats:
    - pdf
    - docx
    - txt
  max-file-size: 1048576

logging:
  level:
    com.cosmoscan: DEBUG'

# Основной класс
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/FileAnalysisServiceApplication.java" 'package com.cosmoscan.fileanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileAnalysisServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileAnalysisServiceApplication.class, args);
    }
}'

# Enum
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/enums/ReportStatus.java" 'package com.cosmoscan.fileanalysis.enums;

public enum ReportStatus {
    ACCEPTED("Принято"),
    NEEDS_REWORK("Требуется доработка"),
    ERROR("Ошибка анализа"),
    PENDING("Ожидает анализа");

    private final String russianName;
    ReportStatus(String russianName) { this.russianName = russianName; }
    public String getRussianName() { return russianName; }
}'

# Entity
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/entity/AnalysisReport.java" 'package com.cosmoscan.fileanalysis.entity;

import com.cosmoscan.fileanalysis.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReport {
    @Id
    private UUID id;
    private UUID workId;
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    private String fileName;
    private Long fileSize;
    private String fileFormat;
    private String detectedFormat;
    private String comment;
    private String issues;
    private Boolean isValidFormat;
    private Boolean isValidSize;
    private LocalDateTime createdAt;
    private Long analysisDurationMs;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ReportStatus.PENDING;
    }
}'

# Repository
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/repository/AnalysisReportRepository.java" 'package com.cosmoscan.fileanalysis.repository;

import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, UUID> {
    List<AnalysisReport> findByWorkId(UUID workId);
    Optional<AnalysisReport> findTopByWorkIdOrderByCreatedAtDesc(UUID workId);
    long countByStatus(ReportStatus status);
}'

# DTO
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/dto/AnalysisRequest.java" 'package com.cosmoscan.fileanalysis.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    @NotNull
    private UUID workId;
    @NotBlank
    private String filePath;
    @NotBlank
    private String fileName;
    @NotNull
    @Positive
    private Long fileSize;
}'

# Validator
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/validator/FileFormatValidator.java" 'package com.cosmoscan.fileanalysis.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@Slf4j
public class FileFormatValidator {
    private final Set<String> allowedFormats;

    public FileFormatValidator(@Value("${analysis.allowed-formats}") List<String> allowedFormats) {
        this.allowedFormats = new HashSet<>(allowedFormats);
    }

    public ValidationResult validate(String fileName) {
        String ext = getExtension(fileName);
        if (ext == null) {
            return new ValidationResult(false, "unknown", "Не удалось определить формат файла");
        }
        if (Set.of("zip","rar","7z","tar","gz").contains(ext)) {
            return new ValidationResult(false, ext, "Архивы не принимаются");
        }
        boolean valid = allowedFormats.contains(ext);
        return new ValidationResult(valid, ext, valid ? null : "Неразрешенный формат: " + ext);
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return null;
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public record ValidationResult(boolean isValid, String detectedFormat, String issue) {}
}'

# Size Validator
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/validator/FileSizeValidator.java" 'package com.cosmoscan.fileanalysis.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileSizeValidator {
    private final long maxSize;

    public FileSizeValidator(@Value("${analysis.max-file-size}") long maxSize) {
        this.maxSize = maxSize;
    }

    public ValidationResult validate(long fileSize) {
        boolean valid = fileSize <= maxSize;
        String issue = valid ? null : String.format("Превышен размер: %s (макс: %s)",
                formatSize(fileSize), formatSize(maxSize));
        return new ValidationResult(valid, fileSize, issue);
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1048576) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / 1048576.0);
    }

    public record ValidationResult(boolean isValid, long actualSize, String issue) {}
}'

# Service
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/service/AnalysisService.java" 'package com.cosmoscan.fileanalysis.service;

import com.cosmoscan.fileanalysis.dto.AnalysisRequest;
import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.enums.ReportStatus;
import com.cosmoscan.fileanalysis.repository.AnalysisReportRepository;
import com.cosmoscan.fileanalysis.validator.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class AnalysisService {
    private final AnalysisReportRepository repository;
    private final FileFormatValidator formatValidator;
    private final FileSizeValidator sizeValidator;

    public AnalysisService(AnalysisReportRepository repository,
                           FileFormatValidator formatValidator,
                           FileSizeValidator sizeValidator) {
        this.repository = repository;
        this.formatValidator = formatValidator;
        this.sizeValidator = sizeValidator;
    }

    @Transactional
    public AnalysisReport analyze(AnalysisRequest request) {
        long start = System.currentTimeMillis();
        List<String> issues = new ArrayList<>();

        FileFormatValidator.ValidationResult formatResult = formatValidator.validate(request.getFileName());
        FileSizeValidator.ValidationResult sizeResult = sizeValidator.validate(request.getFileSize());

        if (!formatResult.isValid()) issues.add(formatResult.issue());
        if (!sizeResult.isValid()) issues.add(sizeResult.issue());

        AnalysisReport report = AnalysisReport.builder()
                .workId(request.getWorkId())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .fileFormat(formatResult.detectedFormat())
                .isValidFormat(formatResult.isValid())
                .isValidSize(sizeResult.isValid())
                .analysisDurationMs(System.currentTimeMillis() - start)
                .build();

        if (issues.isEmpty()) {
            report.setStatus(ReportStatus.ACCEPTED);
            report.setComment("Проверка пройдена");
            report.setIssues("Проблем не обнаружено");
        } else {
            report.setStatus(ReportStatus.NEEDS_REWORK);
            report.setComment("Требуется доработка");
            report.setIssues(String.join("; ", issues));
        }

        return repository.save(report);
    }

    public List<AnalysisReport> getReportsByWorkId(UUID workId) {
        return repository.findByWorkId(workId);
    }
}'

# Controller
create_file "file-analysis-service/src/main/java/com/cosmoscan/fileanalysis/controller/AnalysisController.java" 'package com.cosmoscan.fileanalysis.controller;

import com.cosmoscan.fileanalysis.dto.AnalysisRequest;
import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
public class AnalysisController {
    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping("/api/internal/analyze")
    public ResponseEntity<?> analyze(@Valid @RequestBody AnalysisRequest request) {
        try {
            AnalysisReport report = service.analyze(request);
            return ResponseEntity.ok(Map.of("reportId", report.getId(), "status", report.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/reports/{workId}")
    public ResponseEntity<?> getReports(@PathVariable UUID workId) {
        List<AnalysisReport> reports = service.getReportsByWorkId(workId);
        return reports.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(reports);
    }

    @GetMapping("/api/internal/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("service", "file-analysis-service", "status", "UP"));
    }
}'

# Dockerfile
create_file "file-analysis-service/Dockerfile" 'FROM openjdk:17-alpine
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app.jar"]'

echo "  ✓ File Analysis Service создан"

# ============================================================
# 4. COSMOSCAN UI
# ============================================================
echo ""
echo "----------------------------------------"
echo " 4. Создание CosmoScan UI"
echo "----------------------------------------"

# pom.xml
create_file "cosmoscan-ui/pom.xml" '<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.cosmoscan</groupId>
    <artifactId>cosmoscan-ui</artifactId>
    <version>1.0.0</version>
    <name>cosmoscan-ui</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf</artifactId>
            <version>3.2.5</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>com.cosmoscan.ui.CosmoscanClientApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>'

# application.properties
create_file "cosmoscan-ui/src/main/resources/application.properties" 'app.title=КосмоСкан - Информационная система
app.version=1.0.0
app.window.width=1000
app.window.height=700
api.gateway.url=http://localhost:8080
api.timeout=30000
ui.theme=dark
ui.locale=ru'

# Основной класс
create_file "cosmoscan-ui/src/main/java/com/cosmoscan/ui/CosmoscanClientApplication.java" 'package com.cosmoscan.ui;

import com.cosmoscan.ui.ui.MainFrame;
import com.formdev.flatlaf.FlatDarculaLaf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javax.swing.*;

@SpringBootApplication
@Slf4j
public class CosmoscanClientApplication {
    public static void main(String[] args) {
        FlatDarculaLaf.setup();
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                CosmoscanClientApplication.class).headless(false).run(args);
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = context.getBean(MainFrame.class);
            frame.setVisible(true);
            log.info("CosmoScan UI запущен");
        });
    }
}'

# Config
create_file "cosmoscan-ui/src/main/java/com/cosmoscan/ui/config/AppConfig.java" 'package com.cosmoscan.ui.config;

import com.cosmoscan.ui.service.ApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Value("${api.gateway.url}") private String gatewayUrl;
    @Value("${api.timeout}") private int apiTimeout;

    @Bean
    public ApiService apiService() {
        return new ApiService(gatewayUrl, apiTimeout);
    }
}'

# ApiService
create_file "cosmoscan-ui/src/main/java/com/cosmoscan/ui/service/ApiService.java" 'package com.cosmoscan.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class ApiService {
    private final String gatewayUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public ApiService(String gatewayUrl, int timeout) {
        this.gatewayUrl = gatewayUrl;
        this.httpClient = HttpClients.createDefault();
    }

    public String uploadWork(String studentName, File file) throws IOException {
        HttpPost post = new HttpPost(gatewayUrl + "/api/works");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("studentName", studentName, ContentType.TEXT_PLAIN);
        builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
        post.setEntity(builder.build());
        try (CloseableHttpResponse resp = httpClient.execute(post)) {
            return EntityUtils.toString(resp.getEntity());
        }
    }

    public String getReport(String workId) throws IOException {
        HttpGet get = new HttpGet(gatewayUrl + "/api/reports/" + workId);
        try (CloseableHttpResponse resp = httpClient.execute(get)) {
            return EntityUtils.toString(resp.getEntity());
        }
    }

    public String healthCheck() throws IOException {
        HttpGet get = new HttpGet(gatewayUrl + "/health-check");
        try (CloseableHttpResponse resp = httpClient.execute(get)) {
            return EntityUtils.toString(resp.getEntity());
        }
    }
}'

# Model
create_file "cosmoscan-ui/src/main/java/com/cosmoscan/ui/model/AnalysisReport.java" 'package com.cosmoscan.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisReport {
    private UUID reportId;
    private UUID workId;
    private String fileName;
    private Long fileSize;
    private String fileSizeFormatted;
    private String fileFormat;
    private String detectedFormat;
    private String status;
    private String comment;
    private String issues;
    private Boolean isValidFormat;
    private Boolean isValidSize;
    private LocalDateTime createdAt;
    private Long analysisDurationMs;
    private String recommendations;
}'

# MainFrame
create_file "cosmoscan-ui/src/main/java/com/cosmoscan/ui/ui/MainFrame.java" 'package com.cosmoscan.ui.ui;

import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class MainFrame extends JFrame {
    private final StudentPanel studentPanel;
    private final TeacherPanel teacherPanel;

    public MainFrame(StudentPanel studentPanel, TeacherPanel teacherPanel) {
        this.studentPanel = studentPanel;
        this.teacherPanel = teacherPanel;
    }

    @PostConstruct
    private void init() {
        setTitle("КосмоСкан v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Студент", studentPanel);
        tabs.addTab("Преподаватель", teacherPanel);
        add(tabs, BorderLayout.CENTER);

        JLabel statusBar = new JLabel("Готов к работе");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusBar, BorderLayout.SOUTH);
    }
}'

# StudentPanel
create_file "cosmoscan-ui/src/main/java/com/cosmoscan/ui/ui/StudentPanel.java" 'package com.cosmoscan.ui.ui;

import com.cosmoscan.ui.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;

@Component
@Slf4j
public class StudentPanel extends JPanel {
    private final ApiService apiService;
    private JTextField nameField = new JTextField(30);
    private JTextField fileField = new JTextField(30);
    private JButton chooseBtn = new JButton("Выбрать файл");
    private JButton submitBtn = new JButton("Отправить работу");
    private JTextArea logArea = new JTextArea(8, 40);
    private File selectedFile;

    public StudentPanel(ApiService apiService) {
        this.apiService = apiService;
        setBorder(new EmptyBorder(10,10,10,10));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(new TitledBorder("Загрузка работы"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; topPanel.add(new JLabel("ФИО:"), gbc);
        gbc.gridx=1; topPanel.add(nameField, gbc);

        gbc.gridx=0; gbc.gridy=1; topPanel.add(new JLabel("Файл:"), gbc);
        JPanel filePanel = new JPanel(new BorderLayout());
        fileField.setEditable(false);
        filePanel.add(fileField, BorderLayout.CENTER);
        chooseBtn.addActionListener(e -> chooseFile());
        filePanel.add(chooseBtn, BorderLayout.EAST);
        gbc.gridx=1; topPanel.add(filePanel, gbc);

        gbc.gridx=1; gbc.gridy=2;
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitWork());
        topPanel.add(submitBtn, gbc);

        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new TitledBorder("Лог"));
        logArea.setEditable(false);
        bottomPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileField.setText(selectedFile.getAbsolutePath());
            submitBtn.setEnabled(true);
            log("Выбран файл: " + selectedFile.getName());
        }
    }

    private void submitWork() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ФИО", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        setLoading(true);
        new SwingWorker<Void, String>() {
            protected Void doInBackground() throws Exception {
                try {
                    String resp = apiService.uploadWork(nameField.getText().trim(), selectedFile);
                    publish("✓ " + resp);
                } catch (Exception e) {
                    publish("✗ Ошибка: " + e.getMessage());
                }
                return null;
            }
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(StudentPanel.this::log);
            }
            protected void done() { setLoading(false); }
        }.execute();
    }

    private void setLoading(boolean loading) {
        submitBtn.setEnabled(!loading);
        chooseBtn.setEnabled(!loading);
        submitBtn.setText(loading ? "Отправка..." : "Отправить работу");
    }

    private void log(String msg) {
        logArea.append("[" + java.time.LocalTime.now().toString().substring(0,8) + "] " + msg + "\n");
    }
}'

# TeacherPanel
create_file "cosmoscan-ui/src/main/java/com/cosmoscan/ui/ui/TeacherPanel.java" 'package com.cosmoscan.ui.ui;

import com.cosmoscan.ui.model.AnalysisReport;
import com.cosmoscan.ui.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

@Component
public class TeacherPanel extends JPanel {
    private final ApiService apiService;
    private final ObjectMapper mapper = new ObjectMapper();
    private JTextField workIdField = new JTextField(30);
    private JButton searchBtn = new JButton("Получить отчёт");
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea = new JTextArea(10, 40);

    public TeacherPanel(ApiService apiService) {
        this.apiService = apiService;
        setBorder(new EmptyBorder(10,10,10,10));
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(new TitledBorder("Поиск отчёта"));
        searchPanel.add(new JLabel("ID работы:"));
        searchPanel.add(workIdField);
        searchBtn.addActionListener(e -> searchReport());
        searchPanel.add(searchBtn);
        add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Статус", "Формат", "Размер", "Дата"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reportTable = new JTable(tableModel);
        add(new JScrollPane(reportTable), BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new TitledBorder("Детали"));
        detailsArea.setEditable(false);
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
    }

    private void searchReport() {
        String id = workIdField.getText().trim();
        if (id.isEmpty()) return;

        searchBtn.setEnabled(false);
        new SwingWorker<List<AnalysisReport>, Void>() {
            protected List<AnalysisReport> doInBackground() throws Exception {
                String resp = apiService.getReport(id);
                return mapper.readValue(resp, new TypeReference<List<AnalysisReport>>() {});
            }
            protected void done() {
                try {
                    List<AnalysisReport> reports = get();
                    tableModel.setRowCount(0);
                    for (AnalysisReport r : reports) {
                        tableModel.addRow(new Object[]{
                            r.getReportId().toString().substring(0,8),
                            r.getStatus(), r.getFileFormat(),
                            r.getFileSizeFormatted(), r.getCreatedAt()
                        });
                    }
                    if (!reports.isEmpty()) {
                        AnalysisReport r = reports.get(0);
                        detailsArea.setText("Статус: " + r.getStatus() +
                                "\nКомментарий: " + r.getComment() +
                                "\nЗамечания: " + r.getIssues());
                    }
                } catch (Exception e) {
                    detailsArea.setText("Ошибка: " + e.getMessage());
                }
                searchBtn.setEnabled(true);
            }
        }.execute();
    }
}'

# Dockerfile
create_file "cosmoscan-ui/Dockerfile" 'FROM openjdk:17-alpine
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]'

echo "  ✓ CosmoScan UI создан"

# ============================================================
# 5. DOCKER COMPOSE и README
# ============================================================
echo ""
echo "----------------------------------------"
echo " 5. Создание Docker Compose и README"
echo "----------------------------------------"

# docker-compose.yml
create_file "docker-compose.yml" 'version: "3.8"

services:
  db-storing:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: file_storing_db
      POSTGRES_USER: storing_user
      POSTGRES_PASSWORD: storing_pass
    ports:
      - "5432:5432"
    volumes:
      - pgdata_storing:/var/lib/postgresql/data

  db-analysis:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: file_analysis_db
      POSTGRES_USER: analysis_user
      POSTGRES_PASSWORD: analysis_pass
    ports:
      - "5433:5432"
    volumes:
      - pgdata_analysis:/var/lib/postgresql/data

  file-storing-service:
    build: ./file-storing-service
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db-storing:5432/file_storing_db
      ANALYSIS_SERVICE_URL: http://file-analysis-service:8082/api/internal/analyze
    depends_on:
      - db-storing

  file-analysis-service:
    build: ./file-analysis-service
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db-analysis:5432/file_analysis_db
    depends_on:
      - db-analysis

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      FILE_STORING_URL: http://file-storing-service:8081
      FILE_ANALYSIS_URL: http://file-analysis-service:8082
    depends_on:
      - file-storing-service
      - file-analysis-service

volumes:
  pgdata_storing:
  pgdata_analysis:'

# README.md
create_file "README.md" '# КосмоСкан (CosmoScan)

Информационная система для приёма и автоматической проверки студенческих работ.

## Архитектура

### Микросервисы
- **API Gateway** (порт 8080) - единая точка входа, маршрутизация запросов
- **File Storing Service** (порт 8081) - хранение файлов и метаданных
- **File Analysis Service** (порт 8082) - автоматическая проверка файлов
- **CosmoScan UI** - клиентское Swing-приложение

### Базы данных
- `file_storing_db` (PostgreSQL, порт 5432)
- `file_analysis_db` (PostgreSQL, порт 5433)

## Требования
- Java 17+
- Docker и Docker Compose
- Maven 3.8+
- Linux (протестировано на Ubuntu 22.04)

## Быстрый запуск

### 1. Сборка проекта
```bash
chmod +x create-cosmoscan-project.sh
./create-cosmoscan-project.sh
cd cosmoscan
```

### 2. Сборка каждого сервиса
```bash
cd api-gateway && mvn clean package -DskipTests && cd ..
cd file-storing-service && mvn clean package -DskipTests && cd ..
cd file-analysis-service && mvn clean package -DskipTests && cd ..
cd cosmoscan-ui && mvn clean package -DskipTests && cd ..
```

### 3. Запуск через Docker Compose
```bash
docker-compose up --build
```

### 4. Запуск UI (отдельно)
```bash
java -jar cosmoscan-ui/target/cosmoscan-ui-1.0.0.jar
```

## API Endpoints

| Метод | URL | Описание |
|-------|-----|----------|
| POST | /api/works | Загрузка работы (multipart) |
| GET | /api/works/{id}/file | Скачивание файла |
| GET | /api/reports/{workId} | Получение отчётов |
| GET | /health-check | Проверка здоровья |

## Тестирование

### Загрузка валидного PDF
```bash
curl -X POST http://localhost:8080/api/works \
  -F "studentName=Иванов Иван" \
  -F "file=@test.pdf"
```

### Получение отчёта
```bash
curl http://localhost:8080/api/reports/{workId}
```

### Тест отказоустойчивости
```bash
docker-compose stop file-analysis-service
# Отправить файл - должно вернуться предупреждение
```

## Структура проекта
```
cosmoscan/
├── api-gateway/
├── file-storing-service/
├── file-analysis-service/
├── cosmoscan-ui/
├── docker-compose.yml
└── README.md
```'

echo ""
echo "=========================================="
echo " ✓ Проект КосмоСкан успешно создан!"
echo "=========================================="
echo ""
echo "Структура проекта:"
echo "  ├── api-gateway/"
echo "  ├── file-storing-service/"
echo "  ├── file-analysis-service/"
echo "  ├── cosmoscan-ui/"
echo "  ├── docker-compose.yml"
echo "  └── README.md"
echo ""
echo "Для сборки выполните:"
echo "  cd cosmoscan"
echo "  cd api-gateway && mvn clean package -DskipTests && cd .."
echo "  cd file-storing-service && mvn clean package -DskipTests && cd .."
echo "  cd file-analysis-service && mvn clean package -DskipTests && cd .."
echo "  cd cosmoscan-ui && mvn clean package -DskipTests && cd .."
echo ""
echo "Для запуска:"
echo "  docker-compose up --build"
