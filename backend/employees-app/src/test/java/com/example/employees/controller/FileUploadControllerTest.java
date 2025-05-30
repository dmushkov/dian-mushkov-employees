package com.example.employees.controller;

import com.example.employees.model.EmployeePairResult;
import com.example.employees.service.EmployeePairService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeePairService employeePairService;

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public EmployeePairService employeePairService() {
            return Mockito.mock(EmployeePairService.class);
        }
    }

    @Test
    void testHandleFileUpload_success() throws Exception {
        // Arrange
        String csv = """
                EmpID,ProjectID,DateFrom,DateTo
                1,100,2023-01-01,2023-01-10
                2,100,2023-01-05,2023-01-15""";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        List<EmployeePairResult> mockResult = List.of(
                new EmployeePairResult(1, 2,  6)
        );

        when(employeePairService.processCSV(file)).thenReturn(mockResult);

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].empId1").value(1))
                .andExpect(jsonPath("$[0].empId2").value(2))
                .andExpect(jsonPath("$[0].daysWorked").value(6));
    }

    @Test
    void testHandleFileUpload_failure() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "invalid.csv", "text/csv", new byte[0]);
        when(employeePairService.processCSV(file)).thenThrow(new RuntimeException("Parse error"));

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHandleFileUpload_fileTooLarge() throws Exception {
        // Arrange
        // Create a file larger than the configured max size (default 5 MB here)
        byte[] largeContent = new byte[7 * 1024 * 1024]; // 7 MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.csv", "text/csv", largeContent);

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(largeFile))
                .andExpect(status().isPayloadTooLarge()); // HTTP 413
    }
}
