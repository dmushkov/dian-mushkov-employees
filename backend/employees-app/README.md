# Employees Overlap Application

A Spring Boot application that processes CSV files containing employee project assignments to identify pairs of employees 
who have worked together on common projects for the longest total time.

---

## Features

- Upload CSV files with employee project data.
- Supports multiple date formats:  
  `yyyy-MM-dd`, `MM/dd/yyyy`, `dd-MM-yyyy`, `dd/MM/yyyy`, `MM-dd-yyyy`.
- Calculates overlapping days per employee pair, summed across all shared projects.
- Returns all pairs with total days worked together.
- Robust handling of `NULL` or empty end dates (treated as current date).
- Configurable CORS origin to enable React or other frontend integration.
- Detailed logging of processing steps and skipped invalid rows.
- Comprehensive unit tests covering diverse scenarios.

---

## CSV File Format

The CSV file must have these columns with a header row:

| Column    | Description                         | Example         |
| --------- | --------------------------------- | --------------- |
| EmpID     | Employee ID (integer)              | 1               |
| ProjectID | Project ID (integer)               | 100             |
| DateFrom  | Start date of work period          | 2023-01-01      |
| DateTo    | End date of work period or `NULL` | 2023-01-10 or NULL |

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven

### Build and Run

```bash
./mvnw clean package spring-boot:run
