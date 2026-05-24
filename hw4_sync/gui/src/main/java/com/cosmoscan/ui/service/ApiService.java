package com.cosmoscan.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@Slf4j
public class ApiService {
    
    private final String gatewayUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    
    public ApiService(String gatewayUrl, RestTemplate restTemplate) {
        this.gatewayUrl = gatewayUrl;
        this.restTemplate = restTemplate;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        log.info("API Gateway URL: {}", gatewayUrl);
    }
    
    public String uploadWork(String studentName, File file) throws IOException {
        String url = gatewayUrl + "/api/works";
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("studentName", studentName);
        body.add("file", new FileSystemResource(file));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        log.debug("Отправка работы: studentName={}, file={}", studentName, file.getName());
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, requestEntity, String.class
        );
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        throw new IOException("Ошибка сервера: " + response.getStatusCode());
    }
    
    public String getReports(String workId) throws IOException {
        String url = gatewayUrl + "/api/reports/" + workId;
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "[]";
            }
            throw new IOException("Ошибка: " + response.getStatusCode());
        } catch (Exception e) {
            if (e.getMessage().contains("404")) {
                return "[]";
            }
            throw new IOException(e.getMessage(), e);
        }
    }
    
    public String healthCheck() throws IOException {
        String url = gatewayUrl + "/health-check";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }
}
