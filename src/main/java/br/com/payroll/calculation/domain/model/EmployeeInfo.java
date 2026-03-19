package br.com.payroll.calculation.domain.model;

public record EmployeeInfo(
        String fullName,
        String taxIdentifier,
        int dependents
) {
}
