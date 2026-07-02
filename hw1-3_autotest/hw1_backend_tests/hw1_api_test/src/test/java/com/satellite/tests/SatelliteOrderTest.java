package com.satellite.tests;

import com.satellite.base.BaseTest;
import com.satellite.models.SatellitesOrderResponse;
import io.qameta.allure.*;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Epic("Satellite API Tests")
@Feature("Satellite Order Operations")
public class SatelliteOrderTest extends BaseTest {

    @Test
    @Story("Check order by altitude")
    @Description("Test checking satellite order by altitude")
    @Severity(SeverityLevel.NORMAL)
    public void testCheckOrderByAltitude() {
        log.info("TEST Check order by altitude");
        
        Response response = sendGetRequestWithParams("/check-order", "sortBy", "altitude");
        
        response.then()
                .statusCode(200)
                .body("sortParameter", org.hamcrest.Matchers.equalTo("altitude"))
                .body("satellites", org.hamcrest.Matchers.notNullValue());
        
        SatellitesOrderResponse orderResponse = response.as(SatellitesOrderResponse.class);
        assertThat(orderResponse.getSortParameter()).isEqualTo("altitude");
        assertThat(orderResponse.getSatellites()).isNotEmpty();
        
        log.info("Order type {}", orderResponse.getOrderType());
        log.info("Is ordered {}", orderResponse.isOrdered());
        log.info("Message {}", orderResponse.getMessage());
    }

    @Test
    @Story("Check order by speed")
    @Description("Test checking satellite order by speed")
    @Severity(SeverityLevel.NORMAL)
    public void testCheckOrderBySpeed() {
        log.info("TEST Check order by speed");
        
        Response response = sendGetRequestWithParams("/check-order", "sortBy", "speed");
        
        response.then()
                .statusCode(200)
                .body("sortParameter", org.hamcrest.Matchers.equalTo("speed"))
                .body("satellites", org.hamcrest.Matchers.notNullValue());
        
        SatellitesOrderResponse orderResponse = response.as(SatellitesOrderResponse.class);
        assertThat(orderResponse.getSortParameter()).isEqualTo("speed");
        assertThat(orderResponse.getSatellites()).isNotEmpty();
        
        log.info("Order type {}", orderResponse.getOrderType());
        log.info("Is ordered {}", orderResponse.isOrdered());
    }

    @Test
    @Story("Check order by inclination")
    @Description("Test checking satellite order by inclination")
    @Severity(SeverityLevel.NORMAL)
    public void testCheckOrderByInclination() {
        log.info("TEST Check order by inclination");
        
        Response response = sendGetRequestWithParams("/check-order", "sortBy", "inclination");
        
        response.then()
                .statusCode(200)
                .body("sortParameter", org.hamcrest.Matchers.equalTo("inclination"))
                .body("satellites", org.hamcrest.Matchers.notNullValue());
        
        SatellitesOrderResponse orderResponse = response.as(SatellitesOrderResponse.class);
        assertThat(orderResponse.getSortParameter()).isEqualTo("inclination");
        assertThat(orderResponse.getSatellites()).isNotEmpty();
        
        log.info("Order type {}", orderResponse.getOrderType());
    }

    @Test
    @Story("Check order by name")
    @Description("Test checking satellite order by name")
    @Severity(SeverityLevel.NORMAL)
    public void testCheckOrderByName() {
        log.info("TEST Check order by name");
        
        Response response = sendGetRequestWithParams("/check-order", "sortBy", "name");
        
        response.then()
                .statusCode(200)
                .body("sortParameter", org.hamcrest.Matchers.equalTo("name"))
                .body("satellites", org.hamcrest.Matchers.notNullValue());
        
        SatellitesOrderResponse orderResponse = response.as(SatellitesOrderResponse.class);
        assertThat(orderResponse.getSortParameter()).isEqualTo("name");
        assertThat(orderResponse.getSatellites()).isNotEmpty();
        
        log.info("Order type {}", orderResponse.getOrderType());
    }

    @Test
    @Story("Check order with default parameter")
    @Description("Test checking satellite order without specifying sort parameter (should default to altitude)")
    @Severity(SeverityLevel.NORMAL)
    public void testCheckOrderWithDefault() {
        log.info("TEST Check order with default parameter");
        
        Response response = sendGetRequest("/check-order");
        
        response.then()
                .statusCode(200)
                .body("sortParameter", org.hamcrest.Matchers.equalTo("altitude"))
                .body("satellites", org.hamcrest.Matchers.notNullValue());
        
        SatellitesOrderResponse orderResponse = response.as(SatellitesOrderResponse.class);
        assertThat(orderResponse.getSortParameter()).isEqualTo("altitude");
        
        log.info("Message {}", orderResponse.getMessage());
    }
}