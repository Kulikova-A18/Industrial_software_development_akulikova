package com.cosmoscan.ui.service;

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
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class ApiService {
    
    private final String gatewayUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper mapper;
    
    public ApiService(String gatewayUrl, int timeout) {
        this.gatewayUrl = gatewayUrl;
        this.httpClient = HttpClients.createDefault();
        this.mapper = new ObjectMapper();
    }
    
    /**
     * Загрузка работы на сервер
     */
    public String uploadWork(String studentName, File file) throws IOException {
        String url = gatewayUrl + "/api/works";
        
        HttpPost post = new HttpPost(url);
        
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("studentName", studentName, ContentType.TEXT_PLAIN);
        builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
        
        HttpEntity multipart = builder.build();
        post.setEntity(multipart);
        
        log.debug("Отправка работы: studentName={}, file={}", studentName, file.getName());
        
        return executePostRequest(post);
    }
    
    /**
     * Получение отчёта по ID работы
     */
    public String getReport(String workId) throws IOException {
        String url = gatewayUrl + "/api/reports/" + workId;
        HttpGet get = new HttpGet(url);
        log.debug("Получение отчёта для работы: {}", workId);
        return executeGetRequest(get);
    }
    
    /**
     * Получение последнего отчёта
     */
    public String getLatestReport(String workId) throws IOException {
        String url = gatewayUrl + "/api/reports/" + workId + "/latest";
        HttpGet get = new HttpGet(url);
        log.debug("Получение последнего отчёта для работы: {}", workId);
        return executeGetRequest(get);
    }
    
    /**
     * Проверка здоровья системы
     */
    public String healthCheck() throws IOException {
        String url = gatewayUrl + "/health-check";
        HttpGet get = new HttpGet(url);
        return executeGetRequest(get);
    }
    
    /**
     * Выполнение POST запроса
     */
    private String executePostRequest(HttpPost request) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return handleResponse(response);
        }
    }
    
    /**
     * Выполнение GET запроса
     */
    private String executeGetRequest(HttpGet request) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return handleResponse(response);
        }
    }
    
    /**
     * Обработка HTTP ответа
     */
    private String handleResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        
        try {
            String result = EntityUtils.toString(entity);
            
            int statusCode = response.getCode();
            
            if (statusCode == 404) {
                throw new IOException("Ресурс не найден (404)");
            }
            
            if (statusCode >= 500) {
                throw new IOException("Ошибка сервера: " + statusCode + " - " + result);
            }
            
            if (statusCode >= 400) {
                throw new IOException("Ошибка запроса: " + statusCode + " - " + result);
            }
            
            return result;
            
        } catch (ParseException e) {
            throw new IOException("Ошибка парсинга ответа: " + e.getMessage(), e);
        }
    }
}
