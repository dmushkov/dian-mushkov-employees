package com.example.employees.service;

import com.example.employees.model.EmployeePairResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EmployeePairServiceTest {

    @Autowired
    private EmployeePairService employeePairService;

    @Test
    public void testProcessCSV_withValidOverlap_multiplePairs() throws Exception {
        String csvContent =
                """
                        EmpID,ProjectID,DateFrom,DateTo
                        1,100,2023-01-01,2023-01-10
                        2,100,2023-01-05,2023-01-15
                        3,101,2023-02-01,2023-02-20
                        4,101,2023-02-10,2023-02-25
                        1,102,2023-03-01,2023-03-10
                        2,102,2023-03-05,2023-03-15
                        5,103,2023-04-01,2023-04-10
                        6,103,2023-04-05,2023-04-12""";

        MockMultipartFile file = new MockMultipartFile("file", "multi_pairs.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> results = employeePairService.processCSV(file);

        // резултати за двойките (1,2), (3,4) и (5,6)
        assertEquals(3, results.size());

        // Проверка на общите дни за двойка (1,2)
        EmployeePairResult pair12 = results.stream()
                .filter(r -> (r.empId1 == 1 && r.empId2 == 2) || (r.empId1 == 2 && r.empId2 == 1))
                .findFirst().orElse(null);
        assertNotNull(pair12);
        // Заедно: проект 100: 6 дни (5-10 януари), проект 102: 6 дни (5-10 март)
        // Общо = 12 дни
        assertEquals(12, pair12.daysWorked);

        // Проверка на общите дни за двойка (3,4)
        EmployeePairResult pair34 = results.stream()
                .filter(r -> (r.empId1 == 3 && r.empId2 == 4) || (r.empId1 == 4 && r.empId2 == 3))
                .findFirst().orElse(null);
        assertNotNull(pair34);
        // Заедно: проект 101: 11 дни (10-20 февруари)
        assertEquals(11, pair34.daysWorked);

        // Проверка на общите дни за двойка (5,6)
        EmployeePairResult pair56 = results.stream()
                .filter(r -> (r.empId1 == 5 && r.empId2 == 6) || (r.empId1 == 6 && r.empId2 == 5))
                .findFirst().orElse(null);
        assertNotNull(pair56);
        // Заедно: проект 103: 6 дни (5-10 април)
        assertEquals(6, pair56.daysWorked);
    }

    @Test
    public void testProcessCSV_withMultipleProjectsSamePair() throws Exception {
        String csvContent =
                """
                        EmpID,ProjectID,DateFrom,DateTo
                        1,100,2023-01-01,2023-01-10
                        2,100,2023-01-05,2023-01-15
                        1,101,2023-02-01,2023-02-10
                        2,101,2023-02-05,2023-02-20""";

        MockMultipartFile file = new MockMultipartFile("file", "multi_proj_same_pair.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> results = employeePairService.processCSV(file);

        assertEquals(1, results.size());
        EmployeePairResult pair12 = results.get(0);
        assertTrue((pair12.empId1 == 1 && pair12.empId2 == 2) || (pair12.empId1 == 2 && pair12.empId2 == 1));

        // Заедно: проект 100 (5-10 януари): 6 дни, проект 101 (5-10 февруари): 6 дни, общо 12 дни
        assertEquals(12, pair12.daysWorked);
    }

    @Test
    public void testProcessCSV_withNoOverlaps() throws Exception {
        String csvContent =
                """
                        EmpID,ProjectID,DateFrom,DateTo
                        1,100,2023-01-01,2023-01-10
                        2,100,2023-02-01,2023-02-10
                        3,101,2023-03-01,2023-03-05
                        4,101,2023-04-01,2023-04-05""";

        MockMultipartFile file = new MockMultipartFile("file", "no_overlaps.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> results = employeePairService.processCSV(file);

        // Очакваме празен списък, защото няма припокриване на дати
        assertTrue(results.isEmpty(), "We expect no pairs due to lack of overlapping dates");
    }

    @Test
    public void testProcessCSV_withNullDateTo() throws Exception {
        String csvContent =
                """
                        EmpID,ProjectID,DateFrom,DateTo
                        1,100,2023-01-01,NULL
                        2,100,2023-01-01,
                        3,101,2023-01-01,2023-01-10
                        4,101,2023-01-05,2023-01-15""";

        MockMultipartFile file = new MockMultipartFile("file", "null_date_to.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> results = employeePairService.processCSV(file);

        // Двойки: (1,2) общи дни от 1 януари до днес (поне 1 ден), (3,4) припокриване 5-10 януари = 6 дни
        assertTrue(results.size() >= 2);

        EmployeePairResult pair12 = results.stream()
                .filter(r -> (r.empId1 == 1 && r.empId2 == 2) || (r.empId1 == 2 && r.empId2 == 1))
                .findFirst()
                .orElse(null);
        assertNotNull(pair12);
        assertTrue(pair12.daysWorked >= 1);

        EmployeePairResult pair34 = results.stream()
                .filter(r -> (r.empId1 == 3 && r.empId2 == 4) || (r.empId1 == 4 && r.empId2 == 3))
                .findFirst()
                .orElse(null);
        assertNotNull(pair34);
        assertEquals(6, pair34.daysWorked);
    }

    @Test
    public void testProcessCSV_withInvalidOrNullDateFrom() throws Exception {
        String csvContent =
                """
                EmpID,ProjectID,DateFrom,DateTo
                1,100,,2023-01-10
                2,100,NULL,2023-01-15
                3,101,invalid-date,2023-01-10
                4,101,2023-01-05,2023-01-15
                5,101,2023-01-10,2023-01-20
                """;

        MockMultipartFile file = new MockMultipartFile("file", "invalid_date_from.csv", "text/csv", csvContent.getBytes());
        List<EmployeePairResult> results = employeePairService.processCSV(file);

         assertFalse(results.isEmpty(), "Expected non-empty results due to valid overlapping records");

        for (EmployeePairResult r : results) {
            assertTrue(r.empId1 != 1 && r.empId2 != 1, "Invalid empId 1 should be skipped");
            assertTrue(r.empId1 != 2 && r.empId2 != 2, "Invalid empId 2 should be skipped");
            assertTrue(r.empId1 != 3 && r.empId2 != 3, "Invalid empId 3 should be skipped");
        }

        EmployeePairResult pair45 = results.stream()
                .filter(r -> (r.empId1 == 4 && r.empId2 == 5) || (r.empId1 == 5 && r.empId2 == 4))
                .findFirst()
                .orElse(null);
        assertNotNull(pair45, "Expected pair (4,5) to be present");
        assertEquals(6, pair45.daysWorked, "Expected overlap of 6 days between employees 4 and 5");
    }
}
