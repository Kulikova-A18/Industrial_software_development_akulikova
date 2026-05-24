// WorkServiceTest.java
package com.cosmoscan.filestoring.service;

import com.cosmoscan.filestoring.entity.WorkSubmission;
import com.cosmoscan.filestoring.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock private WorkRepository workRepository;
    @Mock private RestTemplate restTemplate;
    @InjectMocks private WorkService workService;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        workService = new WorkService(workRepository, tempDir.toString(), "http://localhost:8082");
    }

    @Test
    void storeWork_ShouldSaveWorkSubmission() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        when(workRepository.save(any(WorkSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkSubmission result = workService.storeWork("John Doe", file);

        assertNotNull(result);
        assertEquals("John Doe", result.getStudentName());
        assertEquals("test.pdf", result.getFileName());
        verify(workRepository, atLeastOnce()).save(any(WorkSubmission.class));
    }

    @Test
    void getFile_WhenWorkExists_ShouldReturnResource() {
        UUID workId = UUID.randomUUID();
        WorkSubmission work = WorkSubmission.builder()
                .id(workId)
                .filePath(tempDir.resolve("test.pdf").toString())
                .build();
        when(workRepository.findById(workId)).thenReturn(Optional.of(work));

        var result = workService.getFile(workId);

        assertTrue(result.isPresent());
    }

    @Test
    void getFile_WhenWorkNotExists_ShouldReturnEmpty() {
        UUID workId = UUID.randomUUID();
        when(workRepository.findById(workId)).thenReturn(Optional.empty());

        var result = workService.getFile(workId);

        assertTrue(result.isEmpty());
    }
}

