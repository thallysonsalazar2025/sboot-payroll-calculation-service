package br.com.payroll.calculation.domain.model;

import java.math.BigDecimal;

public record ProgressiveTaxBracket(
        BigDecimal upperLimit,
        BigDecimal rate,
        BigDecimal fixedDeduction
) {
}
