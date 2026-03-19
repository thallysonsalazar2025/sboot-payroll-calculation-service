package br.com.payroll.calculation.api;

import br.com.payroll.calculation.application.PayrollTaxCalculationService;
import br.com.payroll.calculation.infrastructure.rules.InMemoryBrazilianTaxRuleCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertBigDecimalEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PayrollTaxCalculationComponentIT {

    private PayrollTaxCalculationComponent component;

    @BeforeEach
    void setUp() {
        component = new PayrollTaxCalculationComponent(new PayrollTaxCalculationService(new InMemoryBrazilianTaxRuleCatalog()));
    }

    @Test
    void shouldExecuteIntegratedInputOutputFlow() {
        PayrollTaxCalculationResponse response = component.calculate(new PayrollTaxCalculationRequest(
                new PayrollTaxCalculationRequest.CompanyPayload("Empresa XPTO", "12345678000199", null),
                new PayrollTaxCalculationRequest.EmployeePayload("João da Silva", "12345678900", 0),
                new BigDecimal("8000.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                null,
                LocalDate.of(2026, 2, 1)
        ));

        assertBigDecimalEquals("921.52", response.inssAmount());
        assertBigDecimalEquals("870.87", response.irrfAmount());
        assertBigDecimalEquals("640.00", response.fgtsAmount());
        assertBigDecimalEquals("6207.61", response.netSalary());
        assertTrue(response.simplifiedDeductionApplied());
    }

    @Test
    void shouldRejectIncompleteInputAtComponentBoundary() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> component.calculate(new PayrollTaxCalculationRequest(
                null,
                new PayrollTaxCalculationRequest.EmployeePayload("João da Silva", "12345678900", 0),
                new BigDecimal("8000.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                null,
                LocalDate.of(2026, 2, 1)
        )));

        assertTrue(exception.getMessage().contains("empresa"));
    }
}
