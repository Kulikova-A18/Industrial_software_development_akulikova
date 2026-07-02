package com.satellite.tests;

import com.satellite.base.BaseTest;
import com.satellite.models.Satellite;
import com.satellite.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@Epic("Satellite API Tests")
@Feature("Satellite CRUD Operations")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SatelliteControllerTest extends BaseTest {

    private static Long createdSatelliteId;

    @Test
    @Order(1)
    @Story("Get all satellites")
    @Description("Test getting all satellites from the system")
    @Severity(SeverityLevel.NORMAL)
    public void testGetAllSatellites() {
        log.info("TEST Get all satellites");
        
        Response response = sendGetRequest("");
        
        response.then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("size()", greaterThan(0));
        
        List<Satellite> satellites = response.jsonPath().getList("", Satellite.class);
        assertThat(satellites).isNotEmpty();
        
        log.info("Retrieved {} satellites", satellites.size());
        satellites.forEach(sat -> log.debug("Satellite {} (ID {})", sat.getName(), sat.getId()));
    }

    @Test
    @Order(2)
    @Story("Get satellite by ID")
    @Description("Test getting a satellite by its ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetSatelliteById() {
        log.info("TEST Get satellite by ID");
        
        // First get all satellites to get a valid ID
        Response allResponse = sendGetRequest("");
        List<Satellite> satellites = allResponse.jsonPath().getList("", Satellite.class);
        
        if (!satellites.isEmpty()) {
            Long firstId = satellites.get(0).getId();
            log.info("Testing with satellite ID {}", firstId);
            
            Response response = sendGetRequest("/" + firstId);
            
            response.then()
                    .statusCode(200)
                    .body("id", equalTo(firstId.intValue()))
                    .body("name", notNullValue())
                    .body("altitude", notNullValue());
            
            Satellite satellite = response.as(Satellite.class);
            assertThat(satellite.getId()).isEqualTo(firstId);
            log.info("Retrieved satellite {}", satellite.getName());
        } else {
            log.warn("No satellites found, skipping test");
        }
    }

    @Test
    @Order(3)
    @Story("Get non-existent satellite")
    @Description("Test getting a satellite that doesn't exist")
    @Severity(SeverityLevel.NORMAL)
    public void testGetNonExistentSatellite() {
        log.info("TEST Get non-existent satellite");
        
        Long nonExistentId = 999999L;
        Response response = sendGetRequest("/" + nonExistentId);
        
        response.then()
                .statusCode(404);
        
        log.info("Correctly returned 404 for non-existent ID {}", nonExistentId);
    }

    @Test
    @Order(4)
    @Story("Create satellite")
    @Description("Test creating a new satellite")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateSatellite() {
        log.info("TEST Create satellite");
        
        Satellite newSatellite = TestDataGenerator.generateRandomSatellite();
        
        Response response = sendPostRequest("", newSatellite);
        
        response.then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(newSatellite.getName()))
                .body("altitude", equalTo((float) newSatellite.getAltitude()))
                .body("status", equalTo("ACTIVE"));
        
        createdSatelliteId = response.jsonPath().getLong("id");
        log.info("Created satellite with ID {}", createdSatelliteId);
        
        // Verify by getting the created satellite
        Response getResponse = sendGetRequest("/" + createdSatelliteId);
        getResponse.then()
                .statusCode(200)
                .body("id", equalTo(createdSatelliteId.intValue()))
                .body("name", equalTo(newSatellite.getName()));
    }

    @Test
    @Order(5)
    @Story("Update satellite")
    @Description("Test updating an existing satellite")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdateSatellite() {
        log.info("TEST Update satellite");
        
        // First create a satellite
        Satellite toUpdate = TestDataGenerator.generateTestSatellite("ToUpdate-Sat-" + System.currentTimeMillis());
        Response createResponse = sendPostRequest("", toUpdate);
        Long id = createResponse.jsonPath().getLong("id");
        
        // Update the satellite
        toUpdate.setName("Updated-Sat-" + System.currentTimeMillis());
        toUpdate.setAltitude(999.0);
        toUpdate.setSpeed(9.9);
        toUpdate.setStatus("INACTIVE");
        
        Response updateResponse = sendPutRequest("/" + id, toUpdate);
        
        updateResponse.then()
                .statusCode(200)
                .body("id", equalTo(id.intValue()))
                .body("name", equalTo(toUpdate.getName()))
                .body("altitude", equalTo((float) toUpdate.getAltitude()))
                .body("speed", equalTo((float) toUpdate.getSpeed()))
                .body("status", equalTo(toUpdate.getStatus()));
        
        log.info("Updated satellite ID {}", id);
        
        // Clean up
        sendDeleteRequest("/" + id);
    }

    @Test
    @Order(6)
    @Story("Update non-existent satellite")
    @Description("Test updating a satellite that doesn't exist")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateNonExistentSatellite() {
        log.info("TEST Update non-existent satellite");
        
        Long nonExistentId = 999999L;
        Satellite updateSat = TestDataGenerator.generateRandomSatellite();
        updateSat.setId(nonExistentId);
        
        Response response = sendPutRequest("/" + nonExistentId, updateSat);
        
        response.then()
                .statusCode(404);
        
        log.info("Correctly returned 404 for non-existent ID {}", nonExistentId);
    }

    @Test
    @Order(7)
    @Story("Delete satellite")
    @Description("Test deleting a satellite")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteSatellite() {
        log.info("TEST Delete satellite");
        
        // First create a satellite
        Satellite toDelete = TestDataGenerator.generateRandomSatellite();
        Response createResponse = sendPostRequest("", toDelete);
        Long id = createResponse.jsonPath().getLong("id");
        log.info("Created satellite for deletion with ID {}", id);
        
        // Delete it
        Response deleteResponse = sendDeleteRequest("/" + id);
        deleteResponse.then()
                .statusCode(204);
        
        log.info("Deleted satellite ID {}", id);
        
        // Verify it's gone
        Response getResponse = sendGetRequest("/" + id);
        getResponse.then()
                .statusCode(404);
        
        log.info("Verified satellite is deleted");
    }

    @Test
    @Order(8)
    @Story("Delete non-existent satellite")
    @Description("Test deleting a satellite that doesn't exist")
    @Severity(SeverityLevel.NORMAL)
    public void testDeleteNonExistentSatellite() {
        log.info("TEST Delete non-existent satellite");
        
        Long nonExistentId = 999999L;
        Response response = sendDeleteRequest("/" + nonExistentId);
        
        response.then()
                .statusCode(404);
        
        log.info("Correctly returned 404 for non-existent ID {}", nonExistentId);
    }

    @Test
    @Order(9)
    @Story("Health check")
    @Description("Test the health endpoint")
    @Severity(SeverityLevel.NORMAL)
    public void testHealthEndpoint() {
        log.info("TEST Health endpoint");
        
        Response response = sendGetRequest("/health");
        
        response.then()
                .statusCode(200)
                .body(equalTo("Satellite Service is running"));
        
        log.info("Health check passed {}", response.asString());
    }
}