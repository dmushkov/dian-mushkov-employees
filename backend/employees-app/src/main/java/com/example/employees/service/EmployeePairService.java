package com.example.employees.service;

import com.example.employees.model.EmployeePairResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class EmployeePairService {

    public List<EmployeePairResult> processCSV(MultipartFile file) throws Exception {
        log.info("Starting CSV parsing");
        CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(file.getInputStream()));

        Map<Integer, List<ProjectEntry>> projectMap = new HashMap<>();
        List<EmployeePairResult> detailedResults = new ArrayList<>();

        for (CSVRecord record : parser) {
            try {
                int empId = Integer.parseInt(record.get("EmpID"));
                int projectId = Integer.parseInt(record.get("ProjectID"));
                LocalDate dateFrom = parseDate(record.get("DateFrom"));
                String rawDateTo = record.get("DateTo");
                LocalDate dateTo = (rawDateTo == null || rawDateTo.equalsIgnoreCase("NULL") || rawDateTo.isBlank())
                        ? LocalDate.now()
                        : parseDate(rawDateTo);

                if (dateFrom == null || dateTo == null) {
                    log.info("Skipping row with invalid date: {}", record);
                    continue;
                }

                projectMap
                        .computeIfAbsent(projectId, k -> new ArrayList<>())
                        .add(new ProjectEntry(empId, projectId, dateFrom, dateTo));
            } catch (Exception e) {
                log.warn("Error processing row: {}, skipping. Reason: {}", record, e.getMessage());
            }
        }

        Map<PairKey, Long> pairDurationMap = new HashMap<>();

        for (Map.Entry<Integer, List<ProjectEntry>> entry : projectMap.entrySet()) {
            int projectId = entry.getKey();
            List<ProjectEntry> participants = entry.getValue();

            for (int i = 0; i < participants.size(); i++) {
                for (int j = i + 1; j < participants.size(); j++) {
                    ProjectEntry e1 = participants.get(i);
                    ProjectEntry e2 = participants.get(j);

                    LocalDate overlapStart = e1.dateFrom.isAfter(e2.dateFrom) ? e1.dateFrom : e2.dateFrom;
                    LocalDate overlapEnd = e1.dateTo.isBefore(e2.dateTo) ? e1.dateTo : e2.dateTo;

                    if (!overlapStart.isAfter(overlapEnd)) {
                        long daysWorked = overlapStart.until(overlapEnd).getDays() + 1;

                        int id1 = Math.min(e1.empId, e2.empId);
                        int id2 = Math.max(e1.empId, e2.empId);

                        PairKey key = new PairKey(id1, id2);
                        pairDurationMap.put(key, pairDurationMap.getOrDefault(key, 0L) + daysWorked);

                        detailedResults.add(new EmployeePairResult(id1, id2, projectId, daysWorked));
                        log.info("Pair found: {} and {} worked on project {} for {} days", id1, id2, projectId, daysWorked);
                    }
                }
            }
        }

        Optional<Map.Entry<PairKey, Long>> maxEntry = pairDurationMap.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        List<EmployeePairResult> finalResult = new ArrayList<>();
        if (maxEntry.isPresent()) {
            PairKey maxPair = maxEntry.get().getKey();
            long totalDays = maxEntry.get().getValue();
            log.info("Most collaborative pair: {} and {} with total {} days", maxPair.empId1, maxPair.empId2, totalDays);

            for (EmployeePairResult result : detailedResults) {
                if ((result.empId1 == maxPair.empId1 && result.empId2 == maxPair.empId2)) {
                    finalResult.add(result);
                }
            }
        } else {
            log.info("No overlapping employee pairs found");
        }

        return finalResult;
    }

    private LocalDate parseDate(String dateStr) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static class ProjectEntry {
        int empId;
        int projectId;
        LocalDate dateFrom;
        LocalDate dateTo;

        ProjectEntry(int empId, int projectId, LocalDate dateFrom, LocalDate dateTo) {
            this.empId = empId;
            this.projectId = projectId;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }
    }

    private static class PairKey {
        int empId1;
        int empId2;

        PairKey(int id1, int id2) {
            this.empId1 = id1;
            this.empId2 = id2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PairKey pairKey)) return false;
            return empId1 == pairKey.empId1 && empId2 == pairKey.empId2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(empId1, empId2);
        }
    }
}
