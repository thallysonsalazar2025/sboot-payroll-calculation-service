package br.com.payroll.calculation.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollTaxCalculationRequest(
        CompanyPayload company,
        EmployeePayload employee,
        BigDecimal grossSalary,
        BigDecimal taxableBenefits,
        BigDecimal alimonyDeduction,
        BigDecimal privatePensionDeduction,
        Boolean simplifiedDeductionPreferred,
        LocalDate competenceDate
) {
    public record CompanyPayload(
            String legalName,
            String taxIdentifier,
            BigDecimal fgtsRate
    ) {
    }

    public record EmployeePayload(
            String fullName,
            String taxIdentifier,
            int dependents
    ) {
    }
}
