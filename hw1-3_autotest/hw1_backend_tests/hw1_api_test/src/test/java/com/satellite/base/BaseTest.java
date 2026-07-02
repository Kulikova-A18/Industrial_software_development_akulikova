package com.satellite.base;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.given;

@Slf4j
public abstract class BaseTest {
    
    protected static final String BASE_URL = "http://localhost:8080";
    protected static final String BASE_PATH = "/api/satellites";
    
    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = BASE_PATH;
        
        log.info("Base URL {}", BASE_URL);
        log.info("Base Path {}", BASE_PATH);
    }
    
    @Step("Send GET request to {endpoint}")
    protected Response sendGetRequest(String endpoint) {
        log.info("Sending GET request to {}{}", BASE_URL, endpoint);
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }
    
    @Step("Send GET request to {endpoint} with parameters {params}")
    protected Response sendGetRequestWithParams(String endpoint, String... params) {
        RequestSpecification spec = given().contentType(ContentType.JSON);
        
        for (int i = 0; i < params.length; i += 2) {
            spec.param(params[i], params[i + 1]);
        }
        
        log.info("Sending GET request to {}{} with params {}", BASE_URL, endpoint, params);
        return spec
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }
    
    @Step("Send POST request to {endpoint} with body {body}")
    protected Response sendPostRequest(String endpoint, Object body) {
        log.info("Sending POST request to {}{}", BASE_URL, endpoint);
        log.info("Request body {}", body);
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }
    
    @Step("Send PUT request to {endpoint} with body {body}")
    protected Response sendPutRequest(String endpoint, Object body) {
        log.info("Sending PUT request to {}{}", BASE_URL, endpoint);
        log.info("Request body {}", body);
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }
    
    @Step("Send DELETE request to {endpoint}")
    protected Response sendDeleteRequest(String endpoint) {
        log.info("Sending DELETE request to {}{}", BASE_URL, endpoint);
        return given()
                .contentType(ContentType.JSON)
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }
}