package com.example.employees.controller;

import com.example.employees.service.EmployeePairService;
import com.example.employees.model.EmployeePairResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class FileUploadController {

    private final EmployeePairService employeePairService;

    private final long maxFileSizeBytes;

    public FileUploadController(EmployeePairService employeePairService, @Value("${max-file-size-mb:5}") int maxFileSizeMb) {
        this.employeePairService = employeePairService;
        this.maxFileSizeBytes = maxFileSizeMb * 1024L * 1024L;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<EmployeePairResult>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request: filename='{}', size={} bytes", file.getOriginalFilename(), file.getSize());

        if (file.getSize() > maxFileSizeBytes) {
            log.warn("File size {} exceeds max allowed size {}", file.getSize(), maxFileSizeBytes);
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Collections.emptyList());
        }

        try {
            List<EmployeePairResult> result = employeePairService.processCSV(file);
            log.info("Processed CSV successfully, found {} employee pairs", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing CSV file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
