# Udasecurity – Home Security System

A modular Java-based home security system that tracks sensors, monitors camera input, and manages alarm states.  
This project was part of training and focused on applying real-world software engineering practices.

> 🛠️ I did not build this project from scratch. My contributions focused on **modularizing the codebase, writing unit tests, integrating static code analysis, and improving testability**.

---

## 🔍 Project Overview

The Udasecurity system simulates a smart home environment by:
- Tracking motion and entry sensors
- Analyzing image data to detect cats (simulated image service)
- Managing alarm states (ARMED_HOME, ARMED_AWAY, DISARMED)
- Triggering alarms based on sensor or image activity

---

## ✅ My Contributions

### 📦 1. **Modularization**
- Split the monolithic project into **multi-module Maven** structure:
  - `security-service` – Core logic for sensors and alarms
  - `image-service` – Mocked image recognition module
  - Parent project with shared build config

### 🧪 2. **Unit Testing**
- Wrote unit tests using **JUnit 5** and **Mockito**
- Created mocks for external dependencies (e.g., `ImageService`)
- Used `@Mock`, `@InjectMocks`, and parameterized tests
- Achieved good code coverage for critical logic (sensor handling, alarm rules)

### 🧼 3. **Static Analysis**
- Integrated **SpotBugs** for code quality checks
- Analyzed and resolved common issues like null handling, performance warnings, etc.

### 🧹 4. **Refactoring for Testability**
- Introduced interfaces (e.g., `ImageServiceInterface`) to allow mocking
- Removed tight coupling between services
- Improved method structure and separation of concerns

---

## 🛠 Technologies Used

- Java 11+
- Maven (multi-module)
- JUnit 5
- Mockito
- SpotBugs
- IntelliJ IDEA
