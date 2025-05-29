package com.example.employees.controller;

import com.example.employees.service.EmployeePairService;
import com.example.employees.model.EmployeePairResult;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private final EmployeePairService employeePairService;

    public FileUploadController(EmployeePairService employeePairService) {
        this.employeePairService = employeePairService;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<EmployeePairResult>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            List<EmployeePairResult> result = employeePairService.processCSV(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
