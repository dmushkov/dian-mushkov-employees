package com.example.employees.service;

import com.example.employees.model.EmployeePairResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;

@Service
public class EmployeePairService {

    private static final Logger log = LoggerFactory.getLogger(EmployeePairService.class);

    public List<EmployeePairResult> processCSV(MultipartFile file) throws Exception {
        log.info("Starting CSV processing...");
        CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(file.getInputStream()));

        Map<Integer, List<ProjectEntry>> projectMap = new HashMap<>();
        Map<PairKey, Long> pairTotalDuration = new HashMap<>();

        int validRows = 0;

        for (CSVRecord record : parser) {
            int empId = Integer.parseInt(record.get("EmpID"));
            int projectId = Integer.parseInt(record.get("ProjectID"));
            LocalDate dateFrom = parseDateFrom(record.get("DateFrom"));
            LocalDate dateTo = parseDateTo(record.get("DateTo"));

            if (dateFrom == null || dateTo == null) {
                log.warn("Skipping row with invalid dates: {}", record);
                continue;
            }

            projectMap.computeIfAbsent(projectId, k -> new ArrayList<>())
                    .add(new ProjectEntry(empId, projectId, dateFrom, dateTo));
            validRows++;
        }

        log.info("Parsed {} valid records across {} projects", validRows, projectMap.size());

        // Calculate total overlaps per pair across projects
        for (Map.Entry<Integer, List<ProjectEntry>> entry : projectMap.entrySet()) {
            int projectId = entry.getKey();
            List<ProjectEntry> participants = entry.getValue();

            log.debug("Processing project {} with participants: {}", projectId,
                    participants.stream().map(pe -> pe.empId).toList());

            for (int i = 0; i < participants.size(); i++) {
                for (int j = i + 1; j < participants.size(); j++) {
                    ProjectEntry e1 = participants.get(i);
                    ProjectEntry e2 = participants.get(j);

                    log.debug("Comparing employees {} and {} in project {}", e1.empId, e2.empId, projectId);

                    LocalDate overlapStart = e1.dateFrom.isAfter(e2.dateFrom) ? e1.dateFrom : e2.dateFrom;
                    LocalDate overlapEnd = e1.dateTo.isBefore(e2.dateTo) ? e1.dateTo : e2.dateTo;

                    if (!overlapStart.isAfter(overlapEnd)) {
                        long days = overlapStart.until(overlapEnd).getDays() + 1;
                        PairKey key = new PairKey(e1.empId, e2.empId);

                        pairTotalDuration.put(key, pairTotalDuration.getOrDefault(key, 0L) + days);
                        log.debug("Pair {} & {} worked together for {} days (project {})",
                                key.empId1, key.empId2, days, projectId);
                    } else {
                        log.debug("No overlap between employees {} and {} in project {}", e1.empId, e2.empId, projectId);
                    }
                }
            }
        }

        // Compose final result: one row per pair with total days worked together
        List<EmployeePairResult> results = new ArrayList<>();
        for (Map.Entry<PairKey, Long> entry : pairTotalDuration.entrySet()) {
            PairKey pair = entry.getKey();
            long totalDays = entry.getValue();
            results.add(new EmployeePairResult(pair.empId1, pair.empId2, totalDays));
            log.info("Pair {} & {} total days worked together: {}", pair.empId1, pair.empId2, totalDays);
        }

        return results;
    }

    private LocalDate parseDateFrom(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.trim().equalsIgnoreCase("null")) {
            // Invalid dateFrom, do NOT default to today, treat as invalid
            return null;
        }
        return parseDateWithFormats(dateStr);
    }

    private LocalDate parseDateTo(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.trim().equalsIgnoreCase("null")) {
            return LocalDate.now();
        }
        return parseDateWithFormats(dateStr);
    }

    private LocalDate parseDateWithFormats(String dateStr) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd").withResolverStyle(ResolverStyle.SMART),
                DateTimeFormatter.ofPattern("MM/dd/yyyy").withResolverStyle(ResolverStyle.SMART),
                DateTimeFormatter.ofPattern("dd-MM-yyyy").withResolverStyle(ResolverStyle.SMART),
                DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.SMART),
                DateTimeFormatter.ofPattern("MM-dd-yyyy").withResolverStyle(ResolverStyle.SMART)
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
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
            if (id1 < id2) {
                this.empId1 = id1;
                this.empId2 = id2;
            } else {
                this.empId1 = id2;
                this.empId2 = id1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PairKey that)) return false;
            return empId1 == that.empId1 && empId2 == that.empId2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(empId1, empId2);
        }
    }
}
