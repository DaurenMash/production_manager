package com.prodman.test.repository;

import com.prodman.test.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, String> {
    List<TestResult> findByTestId(String testId);
    List<TestResult> findByEmployeeId(String employeeId);
}