package br.com.payroll.calculation.domain.model;

import java.math.BigDecimal;

public record CompanyInfo(
        String legalName,
        String taxIdentifier,
        BigDecimal fgtsRate
) {
}
