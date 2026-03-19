package br.com.payroll.calculation.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollTaxCalculationResponse(
        LocalDate competenceDate,
        BigDecimal taxableIncome,
        BigDecimal inssBase,
        BigDecimal inssAmount,
        BigDecimal irrfBase,
        BigDecimal irrfAmount,
        BigDecimal fgtsBase,
        BigDecimal fgtsAmount,
        BigDecimal totalEmployeeDeductions,
        BigDecimal netSalary,
        boolean simplifiedDeductionApplied
) {
}
