package br.com.payroll.calculation.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TaxTables(
        LocalDate effectiveFrom,
        List<ProgressiveTaxBracket> inssBrackets,
        BigDecimal inssCeiling,
        List<ProgressiveTaxBracket> irrfBrackets,
        BigDecimal dependentDeduction,
        BigDecimal simplifiedMonthlyDeduction,
        BigDecimal defaultFgtsRate,
        IncomeTaxReductionRule incomeTaxReductionRule
) {
}
