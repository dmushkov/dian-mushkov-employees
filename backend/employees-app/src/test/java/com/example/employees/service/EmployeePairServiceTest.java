package com.example.employees.service;

import com.example.employees.model.EmployeePairResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EmployeePairServiceTest {

    @Autowired
    private EmployeePairService employeePairService;

    @Test
    public void testProcessCSV_withValidOverlap() throws Exception {
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                "1,100,2023-01-01,2023-01-10\n" +
                "2,100,2023-01-05,2023-01-15";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> result = employeePairService.processCSV(file);
        assertFalse(result.isEmpty());
        EmployeePairResult r = result.get(0);
        assertEquals(6, r.daysWorked); // Jan 5 to Jan 10 inclusive
    }

    @Test
    public void testProcessCSV_withInvalidDate() throws Exception {
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                "1,100,invalid-date,2023-01-10";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> result = employeePairService.processCSV(file);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testProcessCSV_withNullDateTo() throws Exception {
        String today = LocalDate.now().toString();
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                "1,100,2023-01-01,\n" +
                "2,100,2023-01-01,NULL";
        MockMultipartFile file = new MockMultipartFile("file", "null.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> result = employeePairService.processCSV(file);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).daysWorked >= 1); // at least 1 day from Jan 1 to now
    }

    @Test
    public void testProcessCSV_withUnorderedEmpIDs() throws Exception {
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                "5,200,2023-01-01,2023-01-10\n" +
                "3,200,2023-01-01,2023-01-10";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> result = employeePairService.processCSV(file);
        assertFalse(result.isEmpty());
        assertEquals(3, result.get(0).empId1);
        assertEquals(5, result.get(0).empId2);
    }

    @Test
    public void testProcessCSV_withMultipleProjects() throws Exception {
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                "1,101,2023-01-01,2023-01-10\n" +
                "2,101,2023-01-01,2023-01-10\n" +
                "1,102,2023-02-01,2023-02-05\n" +
                "2,102,2023-02-01,2023-02-05";
        MockMultipartFile file = new MockMultipartFile("file", "multi.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> result = employeePairService.processCSV(file);
        assertEquals(2, result.size());
        long totalDays = result.stream().mapToLong(r -> r.daysWorked).sum();
        assertEquals(15, totalDays); // 10 + 5 days
    }

    @Test
    public void testProcessCSV_withNoOverlaps() throws Exception {
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                "1,200,2023-01-01,2023-01-10\n" +
                "2,200,2023-02-01,2023-02-10";
        MockMultipartFile file = new MockMultipartFile("file", "nooverlap.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> result = employeePairService.processCSV(file);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testProcessCSV_withMixedValidInvalidRows() throws Exception {
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                "1,100,2023-01-01,2023-01-10\n" +
                "x,100,2023-01-01,2023-01-10\n" + // invalid empId
                "2,100,2023-01-01,2023-01-10";
        MockMultipartFile file = new MockMultipartFile("file", "mixed.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> result = employeePairService.processCSV(file);
        assertEquals(1, result.size());
    }
}
