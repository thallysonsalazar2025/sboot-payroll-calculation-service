package br.com.payroll.calculation.domain.model;

import java.math.BigDecimal;

public record IncomeTaxReductionRule(
        BigDecimal fullExemptionIncomeLimit,
        BigDecimal partialReductionIncomeLimit,
        BigDecimal maxReduction,
        BigDecimal linearReductionConstant,
        BigDecimal linearReductionRate
) {
}
