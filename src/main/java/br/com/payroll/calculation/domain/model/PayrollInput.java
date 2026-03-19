package br.com.payroll.calculation.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollInput(
        CompanyInfo company,
        EmployeeInfo employee,
        BigDecimal grossSalary,
        BigDecimal taxableBenefits,
        BigDecimal alimonyDeduction,
        BigDecimal privatePensionDeduction,
        Boolean simplifiedDeductionPreferred,
        LocalDate competenceDate
) {

    public BigDecimal totalTaxableIncome() {
        return grossSalary.add(taxableBenefits);
    }
}
