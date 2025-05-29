package com.example.employees.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeePairResult {
    public int empId1;
    public int empId2;
    public int projectId;
    public long daysWorked;
}
