// WorkControllerTest.java
package com.cosmoscan.filestoring.controller;

import com.cosmoscan.filestoring.entity.WorkSubmission;
import com.cosmoscan.filestoring.service.WorkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkControllerTest {

    @Mock private WorkService workService;
    @InjectMocks private WorkController controller;

    @Test
    void submitWork_WithValidData_ShouldReturnCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        WorkSubmission saved = WorkSubmission.builder()
                .id(UUID.randomUUID())
                .studentName("John Doe")
                .fileName("test.pdf")
                .fileSize(1024L)
                .analysisStatus("PENDING")
                .build();
        when(workService.storeWork(any(), any())).thenReturn(saved);

        var response = controller.submitWork("John Doe", file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void submitWork_WithEmptyStudentName_ShouldReturnBadRequest() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        var response = controller.submitWork("", file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void health_ShouldReturnOk() {
        var response = controller.health();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}